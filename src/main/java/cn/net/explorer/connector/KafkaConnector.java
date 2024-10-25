package cn.net.explorer.connector;

import cn.hutool.core.util.ObjectUtil;
import cn.net.explorer.domain.dto.kafka.*;
import cn.net.explorer.domain.eneity.BrokerInfo;
import cn.net.explorer.domain.response.kafka.*;
import cn.net.explorer.exception.BusinessException;
import cn.net.explorer.util.ThrowableUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.ConsumerGroupState;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.ConfigResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class KafkaConnector {
    private static final Logger logger = LoggerFactory.getLogger(KafkaConnector.class);

    public AdminClient createClient(String bootstrapServers, String userName, String password) {
        Properties properties = new Properties();
        properties.setProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "5000");
        properties.setProperty(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, "5000");
        if (ObjectUtil.isAllNotEmpty(userName, password)) {
            properties.put("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required username=" + userName + " password=" + password + ";");
            properties.put("security.protocol", "SASL_PLAINTEXT");
            properties.put("sasl.mechanism", "PLAIN");
        }
        return AdminClient.create(properties);
    }

    /**
     * 获取主题列表
     */
    public List<TopicDto> listTopic(BrokerInfo broker) {
        AdminClient client = createClient(broker.getBroker(), broker.getUsername(), broker.getPassword());

        ListTopicsOptions topicsOptions = new ListTopicsOptions();
        //内部topic
        topicsOptions.listInternal(true);
        ListTopicsResult topicsResult = client.listTopics(topicsOptions);

        try {
            Collection<TopicListing> topics = topicsResult.listings().get(3000, TimeUnit.MILLISECONDS);
            return topics.stream().map(item -> new TopicDto(item.name(), item.isInternal(), null)).collect(Collectors.toList());
        } catch (Throwable e) {
            logger.error("error: KafkaConnector#listTopic:{}", e.getMessage());
            logger.error(ThrowableUtil.getStackTrace(e));
        } finally {
            client.close();
        }
        return null;
    }

    /**
     * 创建topic主题
     *
     * @param broker      kafka服务
     * @param topic       主题名
     * @param partition   主题主分区
     * @param replication 主题副本数
     * @param configs     主题的相关配置
     */
    public void createTopic(BrokerInfo broker, String topic, Integer partition, Short replication, Map<String, String> configs) {
        try (AdminClient client = createClient(broker.getBroker(), broker.getUsername(), broker.getPassword())) {
            NewTopic newTopic = new NewTopic(topic, partition, replication)
                    .configs(configs);

            CreateTopicsResult topicsResult = client.createTopics(Stream.of(newTopic).collect(Collectors.toList()));
            topicsResult.all().get(5000, TimeUnit.MILLISECONDS);
        } catch (Throwable e) {
            logger.error("error: KafkaConnector#createTopic:{}", e.getMessage());
            logger.error(ThrowableUtil.getStackTrace(e));
            throw new BusinessException("topic创建异常:" + e.getMessage());
        }
    }


    /**
     * 删除topic主题
     *
     * @param broker kafka服务
     * @param topic  topic名称
     */
    public void deleteTopic(BrokerInfo broker, String topic) {
        try (AdminClient client = createClient(broker.getBroker(), broker.getUsername(), broker.getPassword())) {
            DeleteTopicsResult topicsResult = client.deleteTopics(Stream.of(topic).collect(Collectors.toList()));
            topicsResult.all().get(5000, TimeUnit.MILLISECONDS);
        } catch (Throwable e) {
            logger.error("error: KafkaConnector#deleteTopic:{}", e.getMessage());
            logger.error(ThrowableUtil.getStackTrace(e));
            throw new BusinessException("topic删除异常:" + e.getMessage());
        }
    }


    public TopicDto describeTopics(BrokerInfo broker, String topic) {
        try (AdminClient client = createClient(broker.getBroker(), broker.getUsername(), broker.getPassword())) {
            DescribeTopicsResult topicsResult = client.describeTopics(Collections.singletonList(topic));
            Map<String, TopicDescription> topicDescriptionMap = topicsResult.all().get(5000, TimeUnit.MILLISECONDS);
            TopicDescription topicDescription = topicDescriptionMap.get(topic);

            List<TopicPartitionDto> partitions = topicDescription.partitions().stream().map(item -> {
                TopicPartitionDto partition = new TopicPartitionDto();

                //分区号
                partition.setPartition(item.partition());

                //分区leader节点
                Node leader = item.leader();
                TopicPartitionDto.Node leaderNode = new TopicPartitionDto.Node(leader.id(), leader.host(), leader.port());
                partition.setLeader(leaderNode);

                //分区副本节点
                List<TopicPartitionDto.Node> replicasNodes = item.replicas().stream().map(replica ->
                        new TopicPartitionDto.Node(replica.id(), replica.host(), replica.port())
                ).collect(Collectors.toList());
                partition.setReplicas(replicasNodes);

                //分区与leader进行同步的副本节点
                List<TopicPartitionDto.Node> isrNode = item.isr().stream().map(isr ->
                        new TopicPartitionDto.Node(isr.id(), isr.host(), isr.port())).collect(Collectors.toList());
                partition.setIsr(isrNode);

                return partition;
            }).collect(Collectors.toList());

            TopicDto topicResponse = new TopicDto();
            topicResponse.setName(topicDescription.name());
            topicResponse.setIsInternal(topicDescription.isInternal());
            topicResponse.setPartitions(partitions);
            return topicResponse;
        } catch (Throwable e) {
            logger.error("error: KafkaConnector#listTopicPartitions:{}", e.getMessage());
            logger.error(ThrowableUtil.getStackTrace(e));
            throw new BusinessException("topic查看分区异常:" + e.getMessage());
        }
    }

    /**
     * 获取消费者组信息
     *
     * @param broker kafka服务
     */
    public List<ConsumerGroupDto> listConsumerGroups(BrokerInfo broker) {

        try (AdminClient client = createClient(broker.getBroker(), broker.getUsername(), broker.getPassword())) {
            // 获取所有消费者组
            ListConsumerGroupsResult result = client.listConsumerGroups();
            // 同步获取消费者组列表
            Collection<ConsumerGroupListing> consumerGroups = result.all().get();

            //查询消费者组的详情
            DescribeConsumerGroupsResult describeResult = client.describeConsumerGroups(consumerGroups.stream().map(ConsumerGroupListing::groupId).collect(Collectors.toList()));
            Map<String, ConsumerGroupDescription> consumerGroupDescriptionMap = describeResult.all().get();
            return consumerGroups.stream().map(item -> {
                ConsumerGroupDescription description = consumerGroupDescriptionMap.get(item.groupId());
                List<ConsumerGroupDto.Member> memberList = description.members().stream().map(member -> new ConsumerGroupDto.Member(member.consumerId(), member.clientId(), member.host())).collect(Collectors.toList());

                ConsumerGroupDto groupResponse = new ConsumerGroupDto();
                groupResponse.setGroupId(item.groupId());
                groupResponse.setState(item.state().orElse(ConsumerGroupState.parse("Unknown")).name());
                groupResponse.setMembers(memberList);
                return groupResponse;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("error: KafkaConnector#listConsumerGroups:{}", e.getMessage());
            logger.error(ThrowableUtil.getStackTrace(e));
            throw new BusinessException("获取消费者组信息异常:" + e.getMessage());
        }

    }

    /**
     * 获取topic、broker的配置信息
     *
     * @param broker kafka服务
     * @param type   类型  (broker,topic)
     * @param name   名称  (broker,topic)
     */
    public List<ConfigDto> describeConfigs(BrokerInfo broker, String name, ConfigResource.Type type) {
        try (AdminClient client = createClient(broker.getBroker(), broker.getUsername(), broker.getPassword())) {
            ConfigResource configResource = new ConfigResource(type, name);
            DescribeConfigsResult configsResult = client.describeConfigs(Collections.singletonList(configResource));
            Map<ConfigResource, Config> configMap = configsResult.all().get(5000, TimeUnit.MILLISECONDS);

            return configMap.values().stream()
                    .flatMap(item -> item.entries().stream()
                            .map(config -> new ConfigDto(
                                    config.name(),
                                    config.value(),
                                    config.source(),
                                    config.isSensitive(),
                                    config.isReadOnly(),
                                    config.synonyms(),
                                    config.type(),
                                    config.documentation())))
                    .collect(Collectors.toList());
        } catch (Throwable e) {
            logger.error("error: KafkaConnector#describeConfigs:{}", e.getMessage());
            logger.error(ThrowableUtil.getStackTrace(e));
            throw new BusinessException("获取配置异常:" + e.getMessage());
        }
    }

    /**
     * 新增、修改 broker、topic的配置
     *
     * @param broker    kafka服务
     * @param name      名称  (broker,topic)
     * @param type      类型  (broker,topic)
     * @param opType    新增修改、删除配置
     * @param configMap 配置详情
     */
    public void incrementalAlterConfigs(BrokerInfo broker, String name, ConfigResource.Type type, AlterConfigOp.OpType opType, Map<String, String> configMap) {
        try (AdminClient client = createClient(broker.getBroker(), broker.getUsername(), broker.getPassword())) {
            ConfigResource resource = new ConfigResource(type, name);
            List<AlterConfigOp> alterConfigOps = configMap.keySet().stream().map(key -> {
                ConfigEntry configEntry = new ConfigEntry(key, configMap.get(key));
                return new AlterConfigOp(configEntry, opType);
            }).collect(Collectors.toList());

            // 将配置应用到资源
            Map<ConfigResource, Collection<AlterConfigOp>> configs = new HashMap<>();
            configs.put(resource, alterConfigOps);
            client.incrementalAlterConfigs(configs).all().get();
        } catch (Throwable e) {
            logger.error("error: KafkaConnector#incrementalAlterConfigs:{}", e.getMessage());
            logger.error(ThrowableUtil.getStackTrace(e));
            throw new BusinessException("获取配置异常:" + e.getMessage());
        }
    }

    public List<JSONObject> listTopicInfoOfConsumer(BrokerInfo brokerInfo, String groupId) {
        try (AdminClient adminClient = createClient(brokerInfo.getBroker(), brokerInfo.getUsername(), brokerInfo.getPassword())) {
            //获取消费者组在各个Topic分区的偏移量信息
            Map<TopicPartition, OffsetAndMetadata> consumerGroupOffsetMap = adminClient.listConsumerGroupOffsets(groupId).partitionsToOffsetAndMetadata().get(5, TimeUnit.SECONDS);

            //获取到指定的消费者组groupId的详情
            DescribeConsumerGroupsResult consumerGroupsResult = adminClient.describeConsumerGroups(Collections.singletonList(groupId));
            ConsumerGroupDescription consumerGroup = consumerGroupsResult.all().get(5, TimeUnit.SECONDS).get(groupId);

            // 获取订阅的主题信息
            List<TopicPartition> topicPartitions = consumerGroup.members().stream()
                    .flatMap(member -> member.assignment().topicPartitions().stream())
                    .map(item -> new TopicPartition(item.topic(), item.partition()))
                    .collect(Collectors.toList());
            //获取消费者实例
            KafkaConsumer<String, String> consumer = getConsumer(brokerInfo, "earliest", groupId);
            //主题的开始、结束的偏移量
            Map<TopicPartition, Long> beginOffsetMap = consumer.beginningOffsets(topicPartitions);
            Map<TopicPartition, Long> endOffsetMap = consumer.endOffsets(topicPartitions);

            return topicPartitions.stream().map(topicPartition -> {
                JSONObject result = new JSONObject();
                //主题、分区
                result.put("topic", topicPartition.topic());
                result.put("partition", topicPartition.partition());
                //当前消费者组 对分区提交的偏移量信息
                OffsetAndMetadata offsetAndMetadata = consumerGroupOffsetMap.get(topicPartition);
                result.put("offset", offsetAndMetadata.offset());
                //分区的开始偏移量 和 最后偏移量
                result.put("beginOffset", beginOffsetMap.get(topicPartition));
                result.put("endOffset", endOffsetMap.get(topicPartition));
                result.put("lag", endOffsetMap.get(topicPartition) - offsetAndMetadata.offset());
                return result;
            }).collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("error: KafkaConnector#listTopicInfoOfConsumer:{}", e.getMessage());
            logger.error(ThrowableUtil.getStackTrace(e));
            throw new BusinessException("获取消费者主题偏移量异常:" + e.getMessage());
        }
    }

    /**
     * 获取消费者信息
     *
     * @param brokerInfo  broker 信息
     * @param offsetReset earliest 从TOPIC分区内的头消费、latest 从TOPIC分区内的尾消费
     */
    public KafkaConsumer<String, String> getConsumer(BrokerInfo brokerInfo, String offsetReset, String groupId) {
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", brokerInfo.getBroker());
        props.setProperty("auto.offset.reset", offsetReset);
        props.setProperty("max.partition.fetch.bytes", "1048576");
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        if (StringUtils.isNotEmpty(groupId)) {
            props.setProperty("group.id", groupId);
        }
        return new KafkaConsumer<>(props);
    }


    /**
     * 获取broker信息
     */
    public ClusterDto describeCluster(BrokerInfo brokerInfo) {
        try (AdminClient client = createClient(brokerInfo.getBroker(), brokerInfo.getUsername(), brokerInfo.getPassword())) {
            DescribeClusterResult clusterResult = client.describeCluster();
            String clusterId = clusterResult.clusterId().get(5, TimeUnit.SECONDS);
            Node node = clusterResult.controller().get(5, TimeUnit.SECONDS);

            Collection<Node> nodes = clusterResult.nodes().get(5, TimeUnit.SECONDS);
            List<ClusterDto.Node> nodeList = nodes.stream().map(item -> new ClusterDto.Node(item.host(), item.port(),(Objects.equals(node.host(),item.host()) && Objects.equals(node.port(),item.port()) ))).collect(Collectors.toList());

            ClusterDto cluster = new ClusterDto();
            cluster.setClusterId(clusterId);
            cluster.setHost(node.host());
            cluster.setPort(node.port());
            cluster.setNodes(nodeList);
            return cluster;
        } catch (Exception e) {
            logger.error("error: KafkaConnector#describeCluster:{}", e.getMessage());
            logger.error(ThrowableUtil.getStackTrace(e));
            throw new BusinessException("获取broker异常:" + e.getMessage());
        }
    }
}
