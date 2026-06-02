package com.example.sms.api.dto;

import lombok.Builder;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.Map;

/**
 * 短信发送请求 DTO
 */
@Data
@Builder
public class SmsSendRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 租户 ID (系统自动填充，多租户隔离)
     */
    private Long tenantId;
    
    /**
     * 接收手机号
     */
    @NotBlank(message = "手机号不能为空")
    private String phoneNumber;
    
    /**
     * 短信内容
     */
    @NotBlank(message = "短信内容不能为空")
    @Size(max = 500, message = "短信内容不能超过 500 字")
    private String content;
    
    /**
     * 短信签名 (可选，如未配置则使用默认签名)
     */
    @Size(max = 50, message = "签名长度不能超过 50 个字符")
    private String signature;
    
    /**
     * 通道编码 (可选，指定发送通道)
     */
    private String channelCode;
    
    /**
     * 模板 ID (用于模板短信)
     */
    private String templateId;
    
    /**
     * 模板参数 (Key-Value 形式)
     */
    private Map<String, String> templateParams;
    
    /**
     * 定时发送时间 (可选，格式：yyyy-MM-dd HH:mm:ss)
     */
    private String scheduleTime;
    
    /**
     * 业务类型 (可选，用于分类统计)
     */
    private String bizType;
    
    /**
     * 外部订单号 (可选，用于幂等控制)
     */
    private String outOrderId;
}
