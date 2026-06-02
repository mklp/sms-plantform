package com.zhongway.sms.mq.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhongway.sms.mq.producer.SmsSendMessage;
import com.zhongway.sms.service.SmsCoreService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * 短信发送消费者
 * 负责从 MQ 消费消息并调用短信核心进行实际发送
 * 
 * @author SMS Platform Team
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "SMS_SEND_TOPIC",
        consumerGroup = "sms_send_consumer_group",
        selectorExpression = "send"
)
public class SmsSendConsumer implements RocketMQListener<String> {

    private final ObjectMapper objectMapper;
    private final SmsCoreService smsCoreService;

    public SmsSendConsumer(ObjectMapper objectMapper, SmsCoreService smsCoreService) {
        this.objectMapper = objectMapper;
        this.smsCoreService = smsCoreService;
        log.info("SmsSendConsumer initialized");
    }

    @Override
    public void onMessage(String message) {
        try {
            log.info("Received SMS send message: {}", message);
            
            // 解析消息
            SmsSendMessage smsMessage = objectMapper.readValue(message, SmsSendMessage.class);
            
            // 调用短信核心服务进行发送
            boolean success = smsCoreService.sendSms(smsMessage);
            
            if (success) {
                log.info("SMS sent successfully: messageId={}, phone={}", 
                        smsMessage.getMessageId(), smsMessage.getPhoneNumber());
            } else {
                log.error("SMS send failed: messageId={}, phone={}", 
                        smsMessage.getMessageId(), smsMessage.getPhoneNumber());
                // TODO: 这里可以触发重试逻辑或记录失败
            }
            
        } catch (Exception e) {
            log.error("Failed to process SMS message: {}", message, e);
            // 抛出异常让 MQ 框架进行重试
            throw new RuntimeException("Failed to process SMS message", e);
        }
    }
}
