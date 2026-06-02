package com.example.sms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.sms.api.dto.SmsSendRequest;
import com.example.sms.api.vo.SmsSendResultVO;
import com.example.sms.common.enums.SmsSendStatus;
import com.example.sms.common.exception.SmsException;
import com.example.sms.common.exception.TenantException;
import com.example.sms.dao.entity.SmsChannel;
import com.example.sms.dao.entity.SmsSendRecord;
import com.example.sms.dao.entity.SmsTenant;
import com.example.sms.dao.mapper.SmsChannelMapper;
import com.example.sms.dao.mapper.SmsSendRecordMapper;
import com.example.sms.dao.mapper.SmsTenantMapper;
import com.example.sms.service.SmsSendService;
import com.example.sms.web.config.TenantContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 短信发送服务实现类
 * 
 * @author SMS Platform Team
 */
@Service
public class SmsSendServiceImpl implements SmsSendService {

    private static final Logger logger = LoggerFactory.getLogger(SmsSendServiceImpl.class);

    private final SmsTenantMapper smsTenantMapper;
    private final SmsChannelMapper smsChannelMapper;
    private final SmsSendRecordMapper smsSendRecordMapper;

    public SmsSendServiceImpl(SmsTenantMapper smsTenantMapper,
                              SmsChannelMapper smsChannelMapper,
                              SmsSendRecordMapper smsSendRecordMapper) {
        this.smsTenantMapper = smsTenantMapper;
        this.smsChannelMapper = smsChannelMapper;
        this.smsSendRecordMapper = smsSendRecordMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SmsSendResultVO sendSingle(SmsSendRequest request) {
        logger.debug("Processing single SMS send request");
        
        // 1. 获取当前租户
        String tenantIdStr = TenantContext.getCurrentTenantId();
        if (StringUtils.isBlank(tenantIdStr)) {
            throw new TenantException("Tenant ID is required");
        }
        Long tenantId = Long.parseLong(tenantIdStr);
        
        // 2. 验证租户状态和配额
        SmsTenant tenant = validateTenantQuota(tenantId);
        
        // 3. 参数校验
        validateSendRequest(request);
        
        // 4. 生成消息 ID
        String messageId = generateMessageId();
        
        // 5. 构建完整短信内容 (签名 + 内容)
        String signature = StringUtils.isNotBlank(request.getSignature()) 
                ? request.getSignature() : tenant.getDefaultSignature();
        String fullContent = buildFullContent(signature, request.getContent());
        
        // 6. 选择通道
        SmsChannel channel = selectChannel(request.getChannelCode());
        
        // 7. 创建发送记录
        SmsSendRecord record = createSendRecord(tenantId, messageId, request, signature, fullContent, channel);
        smsSendRecordMapper.insert(record);
        
        // 8. 异步发送 (实际项目中这里应该发送到 MQ)
        // 为了演示，这里同步调用发送逻辑
        boolean sendSuccess = executeSend(record, channel);
        
        // 9. 更新记录状态
        if (sendSuccess) {
            record.setSendStatus(SmsSendStatus.SUCCESS.name());
            record.setSendTime(LocalDateTime.now());
        } else {
            record.setSendStatus(SmsSendStatus.FAILED.name());
            record.setErrorCode("SEND_FAILED");
            record.setErrorMessage("Failed to send via channel: " + channel.getChannelCode());
        }
        smsSendRecordMapper.updateById(record);
        
        // 10. 更新租户计数
        updateTenantCount(tenant);
        
        return SmsSendResultVO.success(messageId, sendSuccess ? "SUCCESS" : "FAILED");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<SmsSendResultVO> sendBatch(List<SmsSendRequest> requests) {
        logger.info("Processing batch SMS send request, count: {}", requests.size());
        
        if (requests == null || requests.isEmpty()) {
            throw new SmsException("Batch requests cannot be empty");
        }
        
        // 限制批量大小
        if (requests.size() > 100) {
            throw new SmsException("Batch size exceeds limit (max 100)");
        }
        
        return requests.stream()
                .map(this::sendSingle)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SmsSendResultVO sendByTemplate(SmsSendRequest request) {
        logger.debug("Processing template SMS send request");
        
        // 1. 获取当前租户
        String tenantIdStr = TenantContext.getCurrentTenantId();
        if (StringUtils.isBlank(tenantIdStr)) {
            throw new TenantException("Tenant ID is required");
        }
        Long tenantId = Long.parseLong(tenantIdStr);
        
        // 2. 验证模板 ID
        if (StringUtils.isBlank(request.getTemplateId())) {
            throw new SmsException("Template ID is required");
        }
        
        // TODO: 从数据库加载模板并替换变量
        // 这里简化处理，假设模板已经解析好内容
        if (StringUtils.isBlank(request.getContent())) {
            throw new SmsException("Template content is empty after parsing");
        }
        
        // 调用单发逻辑
        return sendSingle(request);
    }

    @Override
    public SmsSendResultVO queryStatus(String messageId) {
        logger.debug("Querying status for messageId: {}", messageId);
        
        if (StringUtils.isBlank(messageId)) {
            throw new SmsException("Message ID is required");
        }
        
        LambdaQueryWrapper<SmsSendRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SmsSendRecord::getMessageId, messageId);
        
        SmsSendRecord record = smsSendRecordMapper.selectOne(wrapper);
        
        if (record == null) {
            throw new SmsException("Message not found: " + messageId);
        }
        
        return SmsSendResultVO.builder()
                .messageId(messageId)
                .status(record.getSendStatus())
                .phoneNumber(record.getPhoneNumber())
                .submitTime(record.getSubmitTime())
                .sendTime(record.getSendTime())
                .reportTime(record.getReportTime())
                .errorCode(record.getErrorCode())
                .errorMessage(record.getErrorMessage())
                .build();
    }

    /**
     * 验证租户状态和配额
     */
    private SmsTenant validateTenantQuota(Long tenantId) {
        SmsTenant tenant = smsTenantMapper.selectById(tenantId);
        
        if (tenant == null) {
            throw new TenantException("Tenant not found: " + tenantId);
        }
        
        if (!"ACTIVE".equals(tenant.getStatus())) {
            throw new TenantException("Tenant status is not ACTIVE: " + tenant.getStatus());
        }
        
        // 检查每日限额
        if (tenant.getTodaySentCount() >= tenant.getDailyLimit()) {
            throw new TenantException("Daily quota exceeded");
        }
        
        // 检查每月限额
        if (tenant.getMonthSentCount() >= tenant.getMonthlyLimit()) {
            throw new TenantException("Monthly quota exceeded");
        }
        
        return tenant;
    }

    /**
     * 校验发送请求参数
     */
    private void validateSendRequest(SmsSendRequest request) {
        if (request == null) {
            throw new SmsException("Send request cannot be null");
        }
        
        if (StringUtils.isBlank(request.getPhoneNumber())) {
            throw new SmsException("Phone number is required");
        }
        
        // 手机号格式校验 (简单校验)
        if (!request.getPhoneNumber().matches("^1[3-9]\\d{9}$")) {
            throw new SmsException("Invalid phone number format");
        }
        
        if (StringUtils.isBlank(request.getContent())) {
            throw new SmsException("SMS content is required");
        }
        
        // 内容长度校验 (根据运营商要求)
        if (request.getContent().length() > 500) {
            throw new SmsException("SMS content too long (max 500 characters)");
        }
    }

    /**
     * 生成全局唯一消息 ID
     */
    private String generateMessageId() {
        return "SMS_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    /**
     * 构建完整短信内容
     */
    private String buildFullContent(String signature, String content) {
        if (StringUtils.isBlank(signature)) {
            return content;
        }
        return "[" + signature + "]" + content;
    }

    /**
     * 选择发送通道
     */
    private SmsChannel selectChannel(String channelCode) {
        SmsChannel channel;
        
        if (StringUtils.isNotBlank(channelCode)) {
            // 指定通道
            channel = smsChannelMapper.selectByCode(channelCode);
            if (channel == null) {
                throw new SmsException("Channel not found: " + channelCode);
            }
        } else {
            // 自动选择最优通道 (根据优先级、权重、健康状态)
            channel = smsChannelMapper.selectAvailableChannel();
            if (channel == null) {
                throw new SmsException("No available channel");
            }
        }
        
        if (!"ACTIVE".equals(channel.getStatus())) {
            throw new SmsException("Channel is not active: " + channel.getChannelCode());
        }
        
        return channel;
    }

    /**
     * 创建发送记录
     */
    private SmsSendRecord createSendRecord(Long tenantId, String messageId, SmsSendRequest request,
                                           String signature, String fullContent, SmsChannel channel) {
        SmsSendRecord record = new SmsSendRecord();
        record.setTenantId(tenantId);
        record.setMessageId(messageId);
        record.setOutOrderId(request.getOutOrderId());
        record.setPhoneNumber(request.getPhoneNumber());
        record.setContent(request.getContent());
        record.setSignature(signature);
        record.setFullContent(fullContent);
        record.setBizType(request.getBizType());
        record.setChannelId(channel.getId());
        record.setProtocolType(channel.getProtocolType());
        record.setChargeCount(calculateChargeCount(fullContent));
        record.setSendStatus(SmsSendStatus.WAITING.name());
        record.setSubmitTime(LocalDateTime.now());
        record.setRetryCount(0);
        record.setIsCallback(false);
        return record;
    }

    /**
     * 计算计费条数 (按 70 字一条计算)
     */
    private int calculateChargeCount(String content) {
        if (content == null || content.isEmpty()) {
            return 1;
        }
        int length = content.length();
        return (length + 69) / 70; // 向上取整
    }

    /**
     * 执行发送 (实际项目中这里会调用 sms-core 或发送到 MQ)
     */
    private boolean executeSend(SmsSendRecord record, SmsChannel channel) {
        logger.info("Executing SMS send via channel: {}, phone: {}", 
                channel.getChannelCode(), record.getPhoneNumber());
        
        try {
            // TODO: 集成 sms-core 进行实际发送
            // 这里模拟发送过程
            Thread.sleep(100); // 模拟网络延迟
            
            // 模拟发送结果 (实际应根据返回值判断)
            return true;
            
        } catch (Exception e) {
            logger.error("SMS send failed", e);
            record.setErrorCode("SEND_EXCEPTION");
            record.setErrorMessage(e.getMessage());
            return false;
        }
    }

    /**
     * 更新租户发送计数
     */
    private void updateTenantCount(SmsTenant tenant) {
        tenant.setTodaySentCount(tenant.getTodaySentCount() + 1);
        tenant.setMonthSentCount(tenant.getMonthSentCount() + 1);
        smsTenantMapper.updateById(tenant);
    }
}
