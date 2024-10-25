package cn.net.explorer.controller.kafka;

import cn.net.explorer.connector.KafkaConnector;
import cn.net.explorer.domain.eneity.BrokerInfo;
import cn.net.explorer.domain.request.ValidationGroup;
import cn.net.explorer.domain.request.kafka.ConfigRequest;
import cn.net.explorer.domain.response.ApiResponse;
import cn.net.explorer.domain.dto.kafka.ConfigDto;
import cn.net.explorer.exception.BusinessException;
import cn.net.explorer.service.BrokerService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/kafka/config")
public class ConfigController {

    @Resource
    private BrokerService brokerService;
    @Resource
    private KafkaConnector kafkaConnector;

    /**
     *  查看broker、topic的配置信息
     * {"brokerId":1,"name":"topic-24101201","type":"TOPIC"}
     */
    @PostMapping("/describeConfigs")
    public ApiResponse<List<ConfigDto>> describeTopicConfigs(@RequestBody @Validated(ValidationGroup.select.class) ConfigRequest request) {
        BrokerInfo brokerInfo = brokerService.lambdaQuery().eq(BrokerInfo::getId, request.getBrokerId()).oneOpt().orElseThrow(() -> new BusinessException("数据异常"));
        List<ConfigDto> configResponses = kafkaConnector.describeConfigs(brokerInfo, request.getName(), request.getType());
        return ApiResponse.ok(configResponses);
    }


    /**
     * 新增修改、删除配置信息
     * {"brokerId":1,"name":"topic-24101201","type": "TOPIC","opType":"SET","configs":{"file.delete.delay.ms":"30000"}}
     */
    @PostMapping("/incrementalAlterConfigs")
    public ApiResponse<?> incrementalAlterTopicConfigs(@RequestBody @Validated(ValidationGroup.save.class) ConfigRequest request) {
        BrokerInfo brokerInfo = brokerService.lambdaQuery().eq(BrokerInfo::getId, request.getBrokerId()).oneOpt().orElseThrow(() -> new BusinessException("数据异常"));
        kafkaConnector.incrementalAlterConfigs(brokerInfo, request.getName(), request.getType(), request.getOpType(), request.getConfigs());
        return ApiResponse.ok();
    }
}
