package cn.net.explorer.controller;

import com.alibaba.fastjson.JSON;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.ConfigResource;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
public class KafkaController {

    public static void main(String[] args) throws Exception {


        // System.out.println("结束消费"+(System.currentTimeMillis() - startMillis));
    }

    public static void count() throws Exception {
        AdminClient client = createAdminClient();
        DescribeTopicsResult topicsResult = client.describeTopics(Stream.of("GA1400-FACE-RECORD-34020000001190000003").collect(Collectors.toList()));
        Map<String, TopicDescription> stringTopicDescriptionMap = topicsResult.all().get();
        System.out.println(JSON.toJSONString(stringTopicDescriptionMap));

        KafkaConsumer<String, String> consumer = getConsumer("GA1400-FACE-RECORD-34020000001190000003");
        Map<TopicPartition, Long> beginOffsetsMap = consumer.beginningOffsets(Stream.of(new TopicPartition("GA1400-FACE-RECORD-34020000001190000003", 0), new TopicPartition("GA1400-FACE-RECORD-34020000001190000003", 1), new TopicPartition("GA1400-FACE-RECORD-34020000001190000003", 2)).collect(Collectors.toList()));
        Map<TopicPartition, Long> endOffsetsMap = consumer.endOffsets(Stream.of(new TopicPartition("GA1400-FACE-RECORD-34020000001190000003", 0), new TopicPartition("GA1400-FACE-RECORD-34020000001190000003", 1), new TopicPartition("GA1400-FACE-RECORD-34020000001190000003", 2)).collect(Collectors.toList()));

        System.out.println("============");
        System.out.println(JSON.toJSONString(beginOffsetsMap));
        System.out.println("------------");
        System.out.println(JSON.toJSONString(endOffsetsMap));
    }


    public static KafkaConsumer<String, String> getConsumer(String topic) {
        // Properties props = getCommonProperties(sourceInfo);
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", "192.168.0.71:9092");
        props.setProperty("group.id", "offset-explorer");

        //props.setProperty("fetch.max.bytes", "104857600");
        // props.setProperty("max.partition.fetch.bytes","20971520");

        // props.setProperty("group.id", "tttt");
        props.setProperty("enable.auto.commit", "true");
        props.setProperty("auto.commit.interval.ms", "1000");
        props.setProperty("auto.offset.reset", "earliest");
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        consumer.subscribe(Collections.singleton(topic));
        return consumer;
    }


    /**
     * topic的config  https://blog.csdn.net/Smallc0de/article/details/109298347
     */
    public static void topicConfigs() throws Exception {
        AdminClient client = createAdminClient();


        ConfigResource configResource = new ConfigResource(ConfigResource.Type.TOPIC, "CJD-10000876-DDK");
        DescribeConfigsResult configsResult = client.describeConfigs(Stream.of(configResource).collect(Collectors.toList()));

        Map<ConfigResource, KafkaFuture<Config>> futureMap = configsResult.values();
        Collection<KafkaFuture<Config>> values = futureMap.values();
        for (KafkaFuture<Config> future : values) {
            Config config = future.get();
            Collection<ConfigEntry> entries = config.entries();
            for (ConfigEntry ce : entries) {
                System.out.println(ce.name() + ":" + ce.value() + "  isDefault: " + ce.isDefault());
            }
        }

    }

    /**
     * 获取topic的分区详情
     */
    public static void topicInfo() throws Exception {
        AdminClient client = createAdminClient();
        //   DescribeTopicsResult topicsResult = client.describeTopics(Stream.of("GA1400-MOTOR-VEHICLE-34020000001190000003").collect(Collectors.toList()));
        DescribeTopicsResult topicsResult = client.describeTopics(Stream.of("CJD-10000876-DDK").collect(Collectors.toList()));
        Map<String, KafkaFuture<TopicDescription>> values = topicsResult.values();
        values.values().forEach(topicDescription -> {
            try {
                TopicDescription td = topicDescription.get();

                System.out.println("========1===========");
                System.out.println(td);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        Map<String, TopicDescription> stringTopicDescriptionMap = topicsResult.all().get();
        Collection<TopicDescription> values1 = stringTopicDescriptionMap.values();

        System.out.println("=========2===========");
        System.out.println(values1);
        //partition:  主题的分区
        //leader: 主题的分区所在的leader节点
        //replicas: 主题的分区所在的副本节点
        //ISR: "in-sync replicas" 的缩写，表示与当前领导者副本同步的所有副本
    }

    public static void listBrokers() throws Exception {
        AdminClient client = createAdminClient();
        DescribeClusterResult cluster = client.describeCluster();
        //node是全部节点
        Collection<Node> nodes = cluster.nodes().get();
        //clusterID  唯一ID
        String clusterId = cluster.clusterId().get();
        //node是主节点
        Node node = cluster.controller().get();
        System.out.println("===========");
    }

    public static AdminClient createAdminClient() {
        Properties prop = new Properties();
        prop.setProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.0.71:9092");
        // prop.setProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "150.158.45.140:19092,150.158.45.140:29092");
        prop.setProperty(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "2000");
        prop.setProperty(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, "2000");
/*        prop.put("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required username="
                + userName + " password=" + password + ";");
        prop.put("security.protocol", "SASL_PLAINTEXT");
        prop.put("sasl.mechanism", "PLAIN");*/
        return AdminClient.create(prop);
    }

    /**
     * 列出kafka的所有TOPIC
     */
    public static void listTopics() throws Exception {
        AdminClient client = createAdminClient();
        ListTopicsOptions options = new ListTopicsOptions();

        // 列出内部的Topic  __consumer_offsets是内部topic
        options.listInternal(false);

        ListTopicsResult listTopicsResult = client.listTopics(options);
        Collection<TopicListing> topicListings = listTopicsResult.listings().get();

        System.out.println("======================size: " + topicListings.size());
        System.out.println(topicListings);
    }
}
