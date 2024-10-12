package cn.net.explorer.controller.kafka;

import cn.net.explorer.domain.request.kafka.BrokerRequest;
import cn.net.explorer.domain.response.ApiResponse;
import cn.net.explorer.service.BrokerService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/kafka/broker")
public class BrokerController {

    @Resource
    private BrokerService brokerService;

    @PostMapping("/add")
    public ApiResponse<?> add(@RequestBody @Valid BrokerRequest brokerRequest) {
        brokerService.add(brokerRequest);
        return ApiResponse.ok();
    }
}
