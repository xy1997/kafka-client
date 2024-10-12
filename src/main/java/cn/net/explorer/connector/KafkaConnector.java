package cn.net.explorer.connector;

import cn.hutool.core.util.ObjectUtil;
import cn.net.explorer.domain.eneity.BrokerInfo;
import cn.net.explorer.domain.response.kafka.TopicConfigResponse;
import cn.net.explorer.domain.response.kafka.TopicResponse;
import cn.net.explorer.exception.BusinessException;
import cn.net.explorer.util.ThrowableUtil;
import org.apache.kafka.clients.admin.*;
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
    public List<TopicResponse> listTopic(BrokerInfo broker) {
        AdminClient client = createClient(broker.getBroker(), broker.getUsername(), broker.getPassword());
        ListTopicsOptions topicsOptions = new ListTopicsOptions();
        //内部topic
        topicsOptions.listInternal(true);
        ListTopicsResult topicsResult = client.listTopics(topicsOptions);

        try {
            Collection<TopicListing> topics = topicsResult.listings().get(3000, TimeUnit.MILLISECONDS);
            return topics.stream().map(item -> new TopicResponse(item.name(), item.isInternal())).collect(Collectors.toList());
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
        AdminClient client = createClient(broker.getBroker(), broker.getUsername(), broker.getPassword());
        try {
            NewTopic newTopic = new NewTopic(topic, partition, replication)
                    .configs(configs);

            CreateTopicsResult topicsResult = client.createTopics(Stream.of(newTopic).collect(Collectors.toList()));
            topicsResult.all().get(5000, TimeUnit.MILLISECONDS);
        } catch (Throwable e) {
            logger.error("error: KafkaConnector#createTopic:{}", e.getMessage());
            logger.error(ThrowableUtil.getStackTrace(e));
            throw new BusinessException("topic创建异常:" + e.getMessage());
        } finally {
            client.close();
        }
    }


    /**
     * 删除topic主题
     *
     * @param broker kafka服务
     * @param topic  topic名称
     */
    public void deleteTopic(BrokerInfo broker, String topic) {
        AdminClient client = createClient(broker.getBroker(), broker.getUsername(), broker.getPassword());
        try {
            DeleteTopicsResult topicsResult = client.deleteTopics(Stream.of(topic).collect(Collectors.toList()));
            topicsResult.all().get(5000, TimeUnit.MILLISECONDS);
        } catch (Throwable e) {
            logger.error("error: KafkaConnector#deleteTopic:{}", e.getMessage());
            logger.error(ThrowableUtil.getStackTrace(e));
            throw new BusinessException("topic删除异常:" + e.getMessage());
        } finally {
            client.close();
        }
    }

    /**
     * 获取topic、broker的配置信息
     *
     * @param broker kafka服务
     * @param type   类型  (broker,topic)
     * @param name   名称  (broker,topic)
     */
    public List<TopicConfigResponse> describeConfigs(BrokerInfo broker, String name, ConfigResource.Type type) {
        AdminClient client = createClient(broker.getBroker(), broker.getUsername(), broker.getPassword());
        try {
            ConfigResource configResource = new ConfigResource(type, name);
            DescribeConfigsResult configsResult = client.describeConfigs(Collections.singletonList(configResource));
            Map<ConfigResource, Config> configMap = configsResult.all().get(5000, TimeUnit.MILLISECONDS);

            return configMap.values().stream()
                    .flatMap(item -> item.entries().stream()
                            .map(config -> new TopicConfigResponse(
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
            throw new BusinessException("topic获取配置异常:" + e.getMessage());
        } finally {
            client.close();
        }
    }

    /**
     * 新增、修改 broker、topic的配置
     *  @param broker    kafka服务
     * @param name      名称  (broker,topic)
     * @param type      类型  (broker,topic)
     * @param opType    新增修改、删除配置
     * @param configMap 配置详情
     * @return
     */
    public void incrementalAlterConfigs(BrokerInfo broker, String name, ConfigResource.Type type, AlterConfigOp.OpType opType, Map<String, String> configMap) {
        AdminClient client = createClient(broker.getBroker(), broker.getUsername(), broker.getPassword());
        try {
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
            throw new BusinessException("topic获取配置异常:" + e.getMessage());
        } finally {
            client.close();
        }
    }
}
