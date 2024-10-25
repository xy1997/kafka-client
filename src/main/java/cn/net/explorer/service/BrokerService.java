package cn.net.explorer.service;


import cn.net.explorer.domain.eneity.BrokerInfo;
import cn.net.explorer.domain.request.kafka.BrokerRequest;
import cn.net.explorer.domain.request.kafka.ClusterRequest;
import cn.net.explorer.domain.response.kafka.ClusterResponse;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

public interface BrokerService extends IService<BrokerInfo> {

    void add(BrokerRequest brokerRequest);

    IPage<ClusterResponse> searchPage(ClusterRequest request);

    void update(ClusterRequest request);
}
