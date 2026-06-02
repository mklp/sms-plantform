package com.zhongway.sms.core.service.impl;

import com.zhongway.sms.core.client.CmppSmsClient;
import com.zhongway.sms.core.client.SmsClient;
import com.zhongway.sms.core.model.SmsSubmitRequest;
import com.zhongway.sms.core.model.SmsSubmitResponse;
import com.zhongway.sms.core.model.DeliveryReport;
import com.zhongway.sms.core.service.SmsSendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 短信发送服务实现类
 */
@Service
public class SmsSendServiceImpl implements com.zhongway.sms.core.service.SmsSendService {
    private static final Logger logger = LoggerFactory.getLogger(SmsSendServiceImpl.class);

    /**
     * 通道客户端缓存 (通道 ID -> 客户端实例)
     */
    private final Map<Long, SmsClient> clientCache = new ConcurrentHashMap<>();

    @Override
    public SmsSubmitResponse sendSms(SmsSubmitRequest request) {
        logger.info("开始发送短信，msgId={}, phone={}, channelId={}", 
            request.getMsgId(), request.getDestPhones(), request.getChannelId());

        try {
            // 获取通道客户端
            SmsClient client = getClient(request.getChannelId());
            if (client == null) {
                logger.error("未找到通道客户端，channelId={}", request.getChannelId());
                return createErrorResponse(request.getMsgId(), "CHANNEL_NOT_FOUND", "通道不存在");
            }

            // 发送短信
            SmsSubmitResponse response = client.submit(request);
            
            if (response.getSuccess()) {
                logger.info("短信发送成功，msgId={}", request.getMsgId());
            } else {
                logger.warn("短信发送失败，msgId={}, respCode={}, respMsg={}", 
                    request.getMsgId(), response.getRespCode(), response.getRespMsg());
            }
            
            return response;

        } catch (Exception e) {
            logger.error("短信发送异常，msgId={}", request.getMsgId(), e);
            return createErrorResponse(request.getMsgId(), "SEND_EXCEPTION", e.getMessage());
        }
    }

    @Override
    public List<SmsSubmitResponse> batchSendSms(List<SmsSubmitRequest> requests) {
        logger.info("批量发送短信，总数={}", requests.size());
        
        // 按通道分组发送
        Map<Long, List<SmsSubmitRequest>> groupedByChannel = requests.stream()
            .collect(java.util.stream.Collectors.groupingBy(SmsSubmitRequest::getChannelId));
        
        List<SmsSubmitResponse> allResponses = new java.util.ArrayList<>();
        
        for (Map.Entry<Long, List<SmsSubmitRequest>> entry : groupedByChannel.entrySet()) {
            Long channelId = entry.getKey();
            List<SmsSubmitRequest> channelRequests = entry.getValue();
            
            try {
                SmsClient client = getClient(channelId);
                if (client != null) {
                    allResponses.addAll(client.batchSubmit(channelRequests));
                } else {
                    // 通道不可用，为每个请求创建错误响应
                    for (SmsSubmitRequest req : channelRequests) {
                        allResponses.add(createErrorResponse(req.getMsgId(), 
                            "CHANNEL_NOT_FOUND", "通道不存在"));
                    }
                }
            } catch (Exception e) {
                logger.error("批量发送异常，channelId={}", channelId, e);
                for (SmsSubmitRequest req : channelRequests) {
                    allResponses.add(createErrorResponse(req.getMsgId(), 
                        "BATCH_SEND_EXCEPTION", e.getMessage()));
                }
            }
        }
        
        return allResponses;
    }

    @Override
    public SmsSubmitResponse sendByTemplate(Long tenantId, String phone, String templateId, List<String> params) {
        logger.info("模板短信发送，tenantId={}, phone={}, templateId={}", tenantId, phone, templateId);
        
        SmsSubmitRequest request = new SmsSubmitRequest();
        request.setMsgId(generateMsgId());
        request.setTenantId(tenantId);
        request.setDestPhones(java.util.Collections.singletonList(phone));
        request.setTemplateId(templateId);
        request.setTemplateParams(params);
        request.setContent(buildContentFromTemplate(templateId, params));
        
        return sendSms(request);
    }

    @Override
    public void handleDeliveryReport(DeliveryReport report) {
        logger.info("处理状态报告，msgId={}, phone={}, statusCode={}", 
            report.getMsgId(), report.getPhoneNumber(), report.getStatusCode());
        // TODO: 更新数据库中的发送记录状态
        // TODO: 如果配置了回调 URL，调用租户的回调接口
    }

    @Override
    public SmsClient getClient(Long channelId) {
        return clientCache.computeIfAbsent(channelId, this::createClientForChannel);
    }

    /**
     * 为指定通道创建客户端
     */
    private SmsClient createClientForChannel(Long channelId) {
        logger.info("为通道创建客户端，channelId={}", channelId);
        
        CmppSmsClient client = new CmppSmsClient();
        
        // 示例配置，实际应从 sms_channel 表读取
        String host = "192.168.1.100";
        int port = 7890;
        String enterpriseId = "TEST_ENT";
        String sharedSecret = "test_secret";
        
        try {
            client.init(host, port, enterpriseId, sharedSecret);
            return client;
        } catch (Exception e) {
            logger.error("创建通道客户端失败，channelId={}", channelId, e);
            return null;
        }
    }

    /**
     * 生成全局唯一消息 ID
     */
    private String generateMsgId() {
        return "SMS_" + System.currentTimeMillis() + "_" + 
               java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    /**
     * 根据模板 ID 和参数构建短信内容
     */
    private String buildContentFromTemplate(String templateId, List<String> params) {
        // TODO: 从数据库查询模板内容
        String templateContent = "您的验证码是%s，有效期%s分钟";
        
        if (params != null && params.size() >= 2) {
            return String.format(templateContent, params.get(0), params.get(1));
        }
        
        return "您的验证码是 123456，有效期 5 分钟";
    }

    /**
     * 创建错误响应
     */
    private SmsSubmitResponse createErrorResponse(String msgId, String respCode, String respMsg) {
        SmsSubmitResponse response = new SmsSubmitResponse();
        response.setMsgId(msgId);
        response.setSuccess(false);
        response.setRespCode(respCode);
        response.setRespMsg(respMsg);
        return response;
    }
}
