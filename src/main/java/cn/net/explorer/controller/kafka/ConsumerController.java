package cn.net.explorer.controller.kafka;

import cn.net.explorer.connector.KafkaConnector;
import cn.net.explorer.domain.eneity.BrokerInfo;
import cn.net.explorer.domain.response.ApiResponse;
import cn.net.explorer.exception.BusinessException;
import cn.net.explorer.service.BrokerService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;

@RestController
@RequestMapping("/kafka/consumer")
public class ConsumerController {

    @Resource
    private KafkaConnector kafkaConnector;
    @Resource
    private BrokerService brokerService;


    @GetMapping("/listConsumerGroups")
    @Validated
    public ApiResponse<?> listConsumerGroups(@RequestParam @NotEmpty String brokerId) {
        BrokerInfo brokerInfo = brokerService.lambdaQuery().eq(BrokerInfo::getId, brokerId).oneOpt().orElseThrow(() -> new BusinessException("数据异常"));
        return ApiResponse.ok(kafkaConnector.listConsumerGroups(brokerInfo));
    }

    @GetMapping("/listTopicInfoOfConsumer")
    @Validated
    public ApiResponse<?> listTopicInfoOfConsumer(@RequestParam @NotEmpty String brokerId, @RequestParam @NotEmpty String groupId) {
        BrokerInfo brokerInfo = brokerService.lambdaQuery().eq(BrokerInfo::getId, brokerId).oneOpt().orElseThrow(() -> new BusinessException("数据异常"));
        return ApiResponse.ok(kafkaConnector.listTopicInfoOfConsumer(brokerInfo, groupId));
    }
}
