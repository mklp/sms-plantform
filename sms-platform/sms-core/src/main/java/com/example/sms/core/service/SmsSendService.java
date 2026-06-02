package com.example.sms.core.service;

import com.example.sms.core.client.SmsClient;
import com.example.sms.core.model.SmsSubmitRequest;
import com.example.sms.core.model.SmsSubmitResponse;
import com.example.sms.core.model.DeliveryReport;

/**
 * 短信发送服务接口
 */
public interface SmsSendService {

    /**
     * 发送单条短信
     * 
     * @param request 发送请求
     * @return 发送响应
     */
    SmsSubmitResponse sendSms(SmsSubmitRequest request);

    /**
     * 批量发送短信
     * 
     * @param requests 发送请求列表
     * @return 响应列表
     */
    java.util.List<SmsSubmitResponse> batchSendSms(java.util.List<SmsSubmitRequest> requests);

    /**
     * 根据模板发送短信
     * 
     * @param tenantId 租户 ID
     * @param phone 手机号
     * @param templateId 模板 ID
     * @param params 模板参数
     * @return 发送响应
     */
    SmsSubmitResponse sendByTemplate(Long tenantId, String phone, String templateId, java.util.List<String> params);

    /**
     * 处理状态报告
     * 
     * @param report 状态报告
     */
    void handleDeliveryReport(DeliveryReport report);

    /**
     * 获取客户端实例 (用于通道切换)
     * 
     * @param channelId 通道 ID
     * @return 客户端实例
     */
    SmsClient getClient(Long channelId);
}
