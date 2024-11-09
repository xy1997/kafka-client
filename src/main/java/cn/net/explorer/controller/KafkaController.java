package cn.net.explorer.controller;

import cn.net.explorer.connector.KafkaConnector;
import cn.net.explorer.domain.dto.kafka.TopicDto;
import cn.net.explorer.domain.dto.kafka.TopicPartitionDto;
import cn.net.explorer.domain.eneity.BrokerInfo;
import cn.net.explorer.exception.BusinessException;
import cn.net.explorer.service.BrokerService;
import com.alibaba.fastjson.JSON;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
public class KafkaController {

    //earliest和latest
    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", "192.168.0.186:19092,192.168.0.186:29092,192.168.0.186:39092");
        props.setProperty("auto.offset.reset", "latest");
        props.setProperty("max.partition.fetch.bytes","1048576");
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        TopicPartition partition0 = new TopicPartition("top1-1", 0);
        List<TopicPartition> partitions = Stream.of(partition0).collect(Collectors.toList());

        consumer.assign(partitions);


       // 不断从指定的分区中拉取消息
        try {
            while (true) {
                // 从分区中拉取消息（每次最多等待 100 毫秒）
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                // 处理拉取到的消息
                for (ConsumerRecord<String, String> record : records) {
                    System.out.println("topic = " + record.topic() + ", partition = " + record.partition() + ", Offset = " + record.offset() + ", Key = " + record.key()+"; value = " + record.value());
                }
            }
        } finally {
            // 关闭消费者
            consumer.close();
        }

    }

    @GetMapping("/pool")
    public void pool(){
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", "192.168.0.186:19092,192.168.0.186:29092,192.168.0.186:39092");
        props.setProperty("auto.offset.reset", "earliest");
        props.setProperty("max.partition.fetch.bytes","1048576");
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        TopicPartition partition0 = new TopicPartition("top1-1", 0);
        List<TopicPartition> partitions = Stream.of(partition0).collect(Collectors.toList());

        consumer.assign(partitions);


        // 不断从指定的分区中拉取消息
        try {
            while (true) {
                // 从分区中拉取消息（每次最多等待 100 毫秒）
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                // 处理拉取到的消息
                for (ConsumerRecord<String, String> record : records) {
                    System.out.println("topic = " + record.topic() + ", partition = " + record.partition() + ", Offset = " + record.offset() + ", Key = " + record.key()+"; value = " + record.value());
                }
            }
        } finally {
            // 关闭消费者
            consumer.close();
        }
    }


    @Resource
    private KafkaConnector kafkaConnector;
    @Resource
    private BrokerService brokerService;

    @GetMapping("/pool1")
    public void pool1(){
        BrokerInfo brokerInfo = brokerService.lambdaQuery().eq(BrokerInfo::getId, 1).oneOpt().orElseThrow(() -> new BusinessException("数据异常"));

        TopicDto topicDto = kafkaConnector.describeTopics(brokerInfo, "top1-1");
        List<TopicPartitionDto> partitionDtos = topicDto.getPartitions();
        List<TopicPartition> partitions = partitionDtos.stream().map(item -> new TopicPartition("top1-1", item.getPartition())).collect(Collectors.toList());

        Properties props = new Properties();
        props.setProperty("bootstrap.servers", "192.168.0.186:19092,192.168.0.186:29092,192.168.0.186:39092");
        props.setProperty("auto.offset.reset", "earliest");
        props.setProperty("max.partition.fetch.bytes","1048576");
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.assign(partitions);

        // 不断从指定的分区中拉取消息
        try {
            while (true) {
                // 从分区中拉取消息（每次最多等待 100 毫秒）
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                // 处理拉取到的消息
                for (ConsumerRecord<String, String> record : records) {
                    System.out.println("=======================================");
                    System.out.println("topic = " + record.topic() + ", partition = " + record.partition() + ", Offset = " + record.offset() + ", Key = " + record.key()+"; value = " + record.value());
                }
            }
        } finally {
            // 关闭消费者
            consumer.close();
        }




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
}
