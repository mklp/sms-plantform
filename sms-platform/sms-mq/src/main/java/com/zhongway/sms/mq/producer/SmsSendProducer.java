package com.zhongway.sms.mq.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * 短信发送消息生产者
 * 
 * @author SMS Platform Team
 */
@Slf4j
@Component
public class SmsSendProducer {

    private final RocketMQTemplate rocketMQTemplate;
    private final ObjectMapper objectMapper;

    public static final String SEND_TOPIC = "SMS_SEND_TOPIC";
    public static final String SEND_TAG = "send";

    public SmsSendProducer(RocketMQTemplate rocketMQTemplate, ObjectMapper objectMapper) {
        this.rocketMQTemplate = rocketMQTemplate;
        this.objectMapper = objectMapper;
        log.info("SmsSendProducer initialized with topic: {}", SEND_TOPIC);
    }

    /**
     * 发送短信消息（异步）
     */
    public void sendSmsMessage(SmsSendMessage message) {
        try {
            String destination = SEND_TOPIC + ":" + SEND_TAG;
            Message<String> msg = MessageBuilder.withPayload(objectMapper.writeValueAsString(message)).build();
            
            // 异步发送，不阻塞主线程
            rocketMQTemplate.sendOneWay(destination, msg);
            
            log.info("SMS message sent asynchronously: messageId={}, phone={}", 
                    message.getMessageId(), message.getPhoneNumber());
        } catch (Exception e) {
            log.error("Failed to send SMS message to MQ: {}", message.getMessageId(), e);
            throw new RuntimeException("Failed to send message to MQ", e);
        }
    }

    /**
     * 发送短信消息（同步，需要确认发送结果）
     */
    public boolean sendSmsMessageSync(SmsSendMessage message) {
        try {
            String destination = SEND_TOPIC + ":" + SEND_TAG;
            Message<String> msg = MessageBuilder.withPayload(objectMapper.writeValueAsString(message)).build();
            
            // 同步发送，等待 broker 确认
            var sendResult = rocketMQTemplate.syncSend(destination, msg, 3000);
            
            log.info("SMS message sent synchronously: messageId={}, phone={}, result={}", 
                    message.getMessageId(), message.getPhoneNumber(), sendResult.getSendStatus());
            
            return sendResult.getSendStatus().toString().equals("SEND_OK");
        } catch (Exception e) {
            log.error("Failed to send SMS message synchronously: {}", message.getMessageId(), e);
            return false;
        }
    }

    /**
     * 发送延迟消息（用于重试）
     */
    public void sendDelayMessage(SmsSendMessage message, int delayLevel) {
        try {
            String destination = SEND_TOPIC + ":" + SEND_TAG;
            Message<String> msg = MessageBuilder.withPayload(objectMapper.writeValueAsString(message)).build();
            
            // 发送延迟消息，delayLevel: 1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
            rocketMQTemplate.syncSend(destination, msg, 3000, delayLevel);
            
            log.info("SMS delay message sent: messageId={}, delayLevel={}", 
                    message.getMessageId(), delayLevel);
        } catch (Exception e) {
            log.error("Failed to send delay SMS message: {}", message.getMessageId(), e);
        }
    }
}
