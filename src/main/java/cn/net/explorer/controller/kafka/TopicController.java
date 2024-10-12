package cn.net.explorer.controller.kafka;

import cn.net.explorer.connector.KafkaConnector;
import cn.net.explorer.domain.eneity.BrokerInfo;
import cn.net.explorer.domain.request.ValidationGroup;
import cn.net.explorer.domain.request.kafka.TopicRequest;
import cn.net.explorer.domain.response.ApiResponse;
import cn.net.explorer.domain.response.kafka.TopicResponse;
import cn.net.explorer.exception.BusinessException;
import cn.net.explorer.service.BrokerService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@RestController
@RequestMapping("/kafka/topic")
public class TopicController {

    @Resource
    private BrokerService brokerService;
    @Resource
    private KafkaConnector kafkaConnector;

    @GetMapping("/listTopic")
    public ApiResponse<List<TopicResponse>> listTopic(@NotEmpty String brokerId) {
        BrokerInfo brokerInfo = brokerService.lambdaQuery().eq(BrokerInfo::getId, brokerId).oneOpt().orElseThrow(() -> new BusinessException("数据异常"));
        List<TopicResponse> topicList = kafkaConnector.listTopic(brokerInfo);
        return ApiResponse.ok(topicList);
    }

    /**
     * {
     *     "brokerId": 1,
     *     "topicName": "topic-24101201",
     *     "partition": 1,
     *     "replication": 1,
     *     "configs": {
     *         "delete.retention.ms": "43200000"
     *     }
     * }
     */
    @PostMapping("/createTopic")
    public ApiResponse<?> createTopic(@RequestBody @Validated(ValidationGroup.save.class) TopicRequest request) {
        BrokerInfo brokerInfo = brokerService.lambdaQuery().eq(BrokerInfo::getId, request.getBrokerId()).oneOpt().orElseThrow(() -> new BusinessException("数据异常"));
        kafkaConnector.createTopic(brokerInfo, request.getTopicName(), request.getPartition(), request.getReplication(), request.getConfigs());
        return ApiResponse.ok();
    }


    /**
     *
     * {
     *     "brokerId": 1,
     *     "topicName": "topic-241012"
     * }
     */
    @PostMapping("/deleteTopic")
    public ApiResponse<?> deleteTopic(@RequestBody @Validated(ValidationGroup.delete.class) TopicRequest request) {
        BrokerInfo brokerInfo = brokerService.lambdaQuery().eq(BrokerInfo::getId, request.getBrokerId()).oneOpt().orElseThrow(() -> new BusinessException("数据异常"));
        kafkaConnector.deleteTopic(brokerInfo, request.getTopicName());
        return ApiResponse.ok();
    }




}
