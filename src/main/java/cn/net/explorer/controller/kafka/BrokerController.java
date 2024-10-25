package cn.net.explorer.controller.kafka;

import cn.net.explorer.connector.KafkaConnector;
import cn.net.explorer.domain.eneity.BrokerInfo;
import cn.net.explorer.domain.request.kafka.BrokerRequest;
import cn.net.explorer.domain.request.kafka.ClusterRequest;
import cn.net.explorer.domain.response.ApiPageResponse;
import cn.net.explorer.domain.response.ApiResponse;
import cn.net.explorer.domain.dto.kafka.ClusterDto;
import cn.net.explorer.exception.BusinessException;
import cn.net.explorer.service.BrokerService;
import org.apache.ibatis.annotations.Delete;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

@RestController
@RequestMapping("/kafka/broker")
public class BrokerController {

    @Resource
    private BrokerService brokerService;
    @Resource
    private KafkaConnector kafkaConnector;

    @PostMapping("/add")
    public ApiResponse<?> add(@RequestBody @Valid BrokerRequest brokerRequest) {
        brokerService.add(brokerRequest);
        return ApiResponse.ok();
    }

    @GetMapping("/searchPage")
    public ApiPageResponse<?> searchPage(ClusterRequest request) {
        return ApiPageResponse.page(brokerService.searchPage(request));
    }

    @GetMapping("/detail")
    public ApiResponse<?> detail(@RequestParam @NotEmpty String id) {
        BrokerInfo brokerInfo = brokerService.lambdaQuery().eq(BrokerInfo::getId, id).oneOpt().orElseThrow(() -> new BusinessException("数据异常"));
        return ApiResponse.ok(brokerInfo);
    }

    @DeleteMapping("/deleteBatch")
    public ApiResponse<?> delete(@RequestBody @Valid @Size(min = 1) List<String> ids) {
        brokerService.removeBatchByIds(ids);
        return ApiResponse.ok();
    }

    @PostMapping("/update")
    public ApiResponse<?> update(@RequestBody ClusterRequest request) {
        brokerService.update(request);
        return ApiResponse.ok();
    }

    @GetMapping("/describeCluster")
    public ApiResponse<?> describeCluster(@RequestParam @NotEmpty String brokerId) {
        BrokerInfo brokerInfo = brokerService.lambdaQuery().eq(BrokerInfo::getId, brokerId).oneOpt().orElseThrow(() -> new BusinessException("数据异常"));
        return ApiResponse.ok(kafkaConnector.describeCluster(brokerInfo));
    }
}
