package cn.net.explorer.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.socket.SocketUtil;
import cn.net.explorer.connector.KafkaConnector;
import cn.net.explorer.domain.eneity.BrokerInfo;
import cn.net.explorer.domain.request.BrokerRequest;
import cn.net.explorer.exception.BusinessException;
import cn.net.explorer.mapper.BrokerMapper;
import cn.net.explorer.service.BrokerService;
import cn.net.explorer.util.ThrowableUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.net.Socket;
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

        List<String> errorHost = new ArrayList<>();
        for (String broker : StringUtils.split(brokerRequest.getBroker(), ",")) {
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

        BrokerInfo broker = new BrokerInfo();
        broker.setBroker(brokerRequest.getBroker());
        broker.setRemark(brokerRequest.getRemark());
        broker.setName(brokerRequest.getName());
        broker.setPassword(brokerRequest.getPassword());
        broker.setUsername(brokerRequest.getUsername());
        this.save(broker);
    }

    public static void main(String[] args) {
        Socket connect = SocketUtil.connect(new InetSocketAddress("192.168.0.71", 19092), 3000);
    }
}
