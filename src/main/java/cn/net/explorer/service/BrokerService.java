package cn.net.explorer.service;


import cn.net.explorer.domain.eneity.BrokerInfo;
import cn.net.explorer.domain.request.kafka.BrokerRequest;
import com.baomidou.mybatisplus.extension.service.IService;

public interface BrokerService extends IService<BrokerInfo> {

    void add(BrokerRequest brokerRequest);
}
