package cn.net.explorer.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.socket.SocketUtil;
import cn.net.explorer.connector.KafkaConnector;
import cn.net.explorer.domain.eneity.BrokerInfo;
import cn.net.explorer.domain.request.kafka.BrokerRequest;
import cn.net.explorer.domain.request.kafka.ClusterRequest;
import cn.net.explorer.domain.response.kafka.ClusterResponse;
import cn.net.explorer.exception.BusinessException;
import cn.net.explorer.mapper.BrokerMapper;
import cn.net.explorer.service.BrokerService;
import cn.net.explorer.util.ThrowableUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

@Service
public class BrokerServiceImpl extends ServiceImpl<BrokerMapper, BrokerInfo> implements BrokerService {
    private static final Logger logger = LoggerFactory.getLogger(BrokerServiceImpl.class);

    @Resource
    private KafkaConnector kafkaConnector;

    @Override
    public void add(BrokerRequest brokerRequest) {
        Long count = this.lambdaQuery().eq(BrokerInfo::getName, brokerRequest.getName()).count();
        if (count > 0) throw new BusinessException("已存在名称: " + brokerRequest.getName());
        //telnet
        connectTest(brokerRequest.getBroker());

        BrokerInfo broker = new BrokerInfo();
        broker.setBroker(brokerRequest.getBroker());
        broker.setRemark(brokerRequest.getRemark());
        broker.setName(brokerRequest.getName());
        broker.setPassword(brokerRequest.getPassword());
        broker.setUsername(brokerRequest.getUsername());
        this.save(broker);
    }

    @Override
    public IPage<ClusterResponse> searchPage(ClusterRequest request) {
        Page<BrokerInfo> pageOf = Page.of(request.getPage(), request.getLimit());
        return this.lambdaQuery().select(BrokerInfo::getId, BrokerInfo::getName, BrokerInfo::getBroker, BrokerInfo::getCreatedTime)
                .like(StringUtils.isNotBlank(request.getBroker()), BrokerInfo::getBroker, request.getBroker())
                .orderByDesc(BrokerInfo::getCreatedTime)
                .page(pageOf).convert(item -> Convert.convert(ClusterResponse.class, item));
    }

    @Override
    public void update(ClusterRequest request) {
        Long count = this.lambdaQuery().eq(BrokerInfo::getName, request.getName()).ne(BrokerInfo::getId, request.getId()).count();
        if (count > 0) throw new BusinessException("已存在名称: " + request.getName());
        //telnet
        connectTest(request.getBroker());

        this.lambdaUpdate()
                .set(BrokerInfo::getName, request.getName())
                .set(BrokerInfo::getBroker, request.getBroker())
                .set(BrokerInfo::getUsername, request.getUsername())
                .set(BrokerInfo::getPassword, request.getPassword())
                .set(BrokerInfo::getRemark,request.getRemark())
                .eq(BrokerInfo::getId, request.getId())
                .update();
    }

    public void connectTest(String brokers) {
        List<String> errorHost = new ArrayList<>();
        for (String broker : StringUtils.split(brokers, ",")) {
            String[] brokerItems = StringUtils.split(broker, ":");
            try {
                SocketUtil.connect(new InetSocketAddress(brokerItems[0], Integer.parseInt(brokerItems[1])), 3000);
            } catch (Exception e) {
                errorHost.add(broker);
                logger.error("添加kafka的broker信息出错: {}", e.getMessage());
                logger.error(ThrowableUtil.getStackTrace(e));
            }
        }
        if (CollUtil.isNotEmpty(errorHost)) throw new BusinessException("broker连接超时" + JSON.toJSONString(errorHost));
    }
}
