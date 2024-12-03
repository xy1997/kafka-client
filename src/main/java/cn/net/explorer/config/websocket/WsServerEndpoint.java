package cn.net.explorer.config.websocket;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.net.explorer.connector.KafkaConnector;
import cn.net.explorer.domain.dto.kafka.TopicDto;
import cn.net.explorer.domain.dto.kafka.TopicPartitionDto;
import cn.net.explorer.domain.eneity.BrokerInfo;
import cn.net.explorer.service.BrokerService;
import cn.net.explorer.util.ThrowableUtil;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@ServerEndpoint("/consumer/pool/{offset}/{brokerId}/{topicName}/{partition}")
@Component
public class WsServerEndpoint {
    Logger logger = LoggerFactory.getLogger(this.getClass());


    private static final Map<String, Session> SESSION_MAP = new ConcurrentHashMap<>();

    /**
     * key: brokerId:topicName:partition
     */
    static final String MAP_FORMAT_KEY = "%s:%s:%s";

    @OnOpen
    public void onOpen(Session session, @PathParam("offset") String offset, @PathParam("brokerId") Integer brokerId, @PathParam("topicName") String topicName, @PathParam("partition") Integer partition) {
        logger.info("线程ID: {}",Thread.currentThread().getId());
        String mapKey = String.format(MAP_FORMAT_KEY, brokerId, topicName, partition);
        logger.info("[websocket]=======websocket  onOpen ==========");
        if (Objects.isNull(brokerId) || StringUtils.isEmpty(topicName)) {
            logger.info("[websocket]:准备关闭会话: sessionId:{}", session.getId());
            closeSession(session, brokerId, topicName, partition, "brokerId||topicName must not be null");
        }
        logger.info("wesocket openKey:{}",mapKey);
        // 检查是否已有活跃的会话
        Session existingSession = SESSION_MAP.put(mapKey, session);
        if (existingSession != null) {
            // 关闭旧的会话
            logger.info("[websocket]:准备关闭旧会话,sessionId:{} - Replaced by new session", existingSession.getId());
            closeSession(existingSession, brokerId, topicName, partition, "Replaced by new session");
        }
        SESSION_MAP.forEach((key,value) ->{
            logger.info("MAP-KEY: {}, MAP-VALUE: {}",key,value.getId());
        });
        consumer(session.getId(),brokerId,topicName,partition,offset);

    }

    @OnClose
    public void onClose(Session session, @PathParam("offset") String offset, @PathParam("brokerId") Integer brokerId, @PathParam("topicName") String topicName, @PathParam("partition") Integer partition) {
        String mapKey = String.format(MAP_FORMAT_KEY, brokerId, topicName, partition);
        logger.info("[websocket]:sessionId:{},offset:{},brokerId:{},topicName:{},partition:{},MAP_SIZE:{}=======websocket  onClose ==========", session.getId(), offset, brokerId, topicName, partition, SESSION_MAP.size());
        List<Session> sessionList = SESSION_MAP.values().stream().filter(item -> Objects.equals(item.getId(), session.getId())).collect(Collectors.toList());
        if(CollUtil.isNotEmpty(sessionList)){
            SESSION_MAP.remove(mapKey);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session, @PathParam("offset") String offset, @PathParam("brokerId") Integer brokerId, @PathParam("topicName") String topicName, @PathParam("partition") Integer partition) {
        logger.info("[websocket]:sessionId:{}=======websocket  onMessage ==========", session.getId());

        String mapKey = String.format(MAP_FORMAT_KEY, 1, "top1-1", 1);
        Session se = SESSION_MAP.get(mapKey);

        se.getAsyncRemote().sendText(message);
    }

    @OnError
    public void onError(Session session, Throwable error, @PathParam("offset") String offset, @PathParam("brokerId") String brokerId, @PathParam("topicName") String topicName, @PathParam("partition") Integer partition) {
        logger.info("[websocket]:sessionId:{},offset:{},brokerId:{},topicName:{},partition:{}=======websocket  onError ==========: {}", session.getId(), offset, brokerId, topicName, partition, ThrowableUtil.getStackTrace(error));
        logger.error("[websocket]:sessionId:{},offset:{},brokerId:{},topicName:{},partition:{}=======websocket  onError ==========: {}", session.getId(), offset, brokerId, topicName, partition, ThrowableUtil.getStackTrace(error));
    }

    private void closeSession(Session session, Integer brokerId, String topicName, Integer partition, String reason) {
        if (session.isOpen()) {
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, reason));
            } catch (Exception e) {
                String errorMessage = String.format("[websocket]OnClose异常, brokerId: %s,topicName: %s,partition: %s, exception: %s, reason: %s", brokerId, topicName, partition, ThrowableUtil.getStackTrace(e), reason);
                logger.info(errorMessage);
                logger.error(errorMessage);
            }
        }
    }

    /**
     * partition = -1 代表全部分区
     */
    public void consumer(String sessionId,Integer brokerId, String topicName, Integer partition, String offset) {
        BrokerService brokerService = SpringUtil.getBean(BrokerService.class);
        KafkaConnector kafkaConnector = SpringUtil.getBean(KafkaConnector.class);

        BrokerInfo brokerInfo = brokerService.lambdaQuery().eq(BrokerInfo::getId, brokerId).one();
        if (Objects.isNull(brokerInfo)) return;

        KafkaConsumer<String, String> consumer = kafkaConnector.getConsumer(brokerInfo, offset, null);
        List<TopicPartition> partitions = null;
        //查询全部分区
        if (Objects.equals(-1, partition)) {
            TopicDto topicDto = kafkaConnector.describeTopics(brokerInfo, topicName);
            List<TopicPartitionDto> partitionDtos = topicDto.getPartitions();
            partitions = partitionDtos.stream().map(item -> new TopicPartition(topicName, item.getPartition())).collect(Collectors.toList());
        } else {
            partitions = Stream.of(new TopicPartition(topicName, partition)).collect(Collectors.toList());
        }
        consumer.assign(partitions);

        //保存websocket中的seesion  map key
        String mapKey = String.format(MAP_FORMAT_KEY, brokerId, topicName, partition);
        try {
            while (CollUtil.isNotEmpty(SESSION_MAP.values().stream().filter(item -> Objects.equals(item.getId(),sessionId)).collect(Collectors.toList()))) {
                // 从分区中拉取消息（每次最多等待 100 毫秒）
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                // 处理拉取到的消息
                for (ConsumerRecord<String, String> record : records) {
                    Session session = SESSION_MAP.get(mapKey);
                    try {
                        session.getBasicRemote().sendText(record.value());
                    }catch (Exception e){
                        logger.error("[websocket]发送消息异常, mapKey: {}, exception: {}", mapKey, ThrowableUtil.getStackTrace(e));
                    }
                }
            }
        } finally {
            // 关闭消费者
            consumer.close();
        }
    }
}