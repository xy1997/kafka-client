package cn.net.explorer.controller.kafka;

import cn.net.explorer.connector.KafkaConnector;
import cn.net.explorer.domain.eneity.BrokerInfo;
import cn.net.explorer.domain.request.kafka.ProducerRequest;
import cn.net.explorer.domain.response.ApiResponse;
import cn.net.explorer.exception.BusinessException;
import cn.net.explorer.service.BrokerService;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewPartitionReassignment;
import org.apache.kafka.clients.admin.NewPartitions;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/kafka/producer")
public class ProducerController {

    @Autowired
    private KafkaConnector kafkaConnector;
    @Autowired
    private BrokerService brokerService;

    @PostMapping("/sendMessage")
    @Validated
    public ApiResponse<?> sendMessage(@Validated @RequestBody ProducerRequest producerRequest) {
        BrokerInfo brokerInfo = brokerService.lambdaQuery().eq(BrokerInfo::getId, producerRequest.getBrokerId()).oneOpt().orElseThrow(() -> new BusinessException("数据异常"));
        kafkaConnector.sendMessage(brokerInfo, producerRequest.getTopic(),producerRequest.getPartition(),producerRequest.getMessage());
        return ApiResponse.ok();
    }


    public static void main1(String[] args) throws Exception {
        // 配置 Kafka AdminClient
        Properties props = new Properties();
        // 替换为你的 Kafka 集群地址
        props.put("bootstrap.servers", "154.8.215.228:19092,154.8.215.228:29092");

        try (AdminClient adminClient = AdminClient.create(props)) {
            // 指定要扩容的主题和目标分区数
            // 替换为你的主题名称
            String topicName = "top-000";
            // 扩容后的分区数
            int newPartitionCount = 2;

            // 创建分区扩容请求
            Map<String, NewPartitions> newPartitionsMap = Collections.singletonMap(
                    topicName, NewPartitions.increaseTo(newPartitionCount)
            );

            // 执行扩容操作
            adminClient.createPartitions(newPartitionsMap).all().get();
            System.out.println("分区扩容成功！");
        }
    }


    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.put("bootstrap.servers", "154.8.215.228:19092,154.8.215.228:29092");

        try (AdminClient adminClient = AdminClient.create(props)) {
            String topicName = "top-000";
            int desiredReplicationFactor = 3; // 期望的副本数量

            // 获取当前集群的 Broker 列表
            Collection<Node> brokers = adminClient.describeCluster().nodes().get();
            List<Integer> brokerIds = brokers.stream().map(Node::id).collect(Collectors.toList());

            // 获取主题分区信息
            TopicDescription topicDescription = adminClient.describeTopics(Collections.singletonList(topicName))
                    .all().get().get(topicName);

            // 构建新的副本分配
            Map<TopicPartition, Optional<NewPartitionReassignment>> reassignmentMap = new HashMap<>();
            for (TopicPartitionInfo partitionInfo : topicDescription.partitions()) {
                int partition = partitionInfo.partition();
                // 按循环方式分配 Broker
                List<Integer> newReplicas = new ArrayList<>();
                for (int i = 0; i < desiredReplicationFactor; i++) {
                    newReplicas.add(brokerIds.get((partition + i) % brokerIds.size()));
                }

                reassignmentMap.put(
                        new TopicPartition(topicName, partition),
                        Optional.of(new NewPartitionReassignment(newReplicas))
                );
            }

            // 提交重新分配请求
            adminClient.alterPartitionReassignments(reassignmentMap).all().get();
            System.out.println("分区副本重新分配成功！");
        }
    }
}
