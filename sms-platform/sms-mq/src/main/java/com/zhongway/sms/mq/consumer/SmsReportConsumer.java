package com.zhongway.sms.mq.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * 短信状态报告消费者
 * 负责从 MQ 消费运营商返回的状态报告并更新数据库
 * 
 * @author SMS Platform Team
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "SMS_REPORT_TOPIC",
        consumerGroup = "sms_report_consumer_group",
        selectorExpression = "report"
)
public class SmsReportConsumer implements RocketMQListener<String> {

    private final ObjectMapper objectMapper;

    public SmsReportConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        log.info("SmsReportConsumer initialized");
    }

    @Override
    public void onMessage(String message) {
        try {
            log.info("Received SMS report message: {}", message);
            
            // TODO: 解析状态报告消息
            // SmsReportMessage reportMessage = objectMapper.readValue(message, SmsReportMessage.class);
            
            // TODO: 根据 messageId 更新 sms_send_record 表的状态
            // smsSendRecordService.updateStatusByMessageId(reportMessage);
            
            log.info("SMS report processed successfully");
            
        } catch (Exception e) {
            log.error("Failed to process SMS report message: {}", message, e);
            throw new RuntimeException("Failed to process SMS report", e);
        }
    }
}
