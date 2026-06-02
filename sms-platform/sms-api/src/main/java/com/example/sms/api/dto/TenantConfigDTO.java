package com.example.sms.api.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 企业租户配置 DTO
 */
@Data
public class TenantConfigDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 租户 ID
     */
    private Long tenantId;
    
    /**
     * 租户名称
     */
    @NotBlank(message = "租户名称不能为空")
    private String tenantName;
    
    /**
     * 联系人
     */
    private String contactPerson;
    
    /**
     * 联系电话
     */
    private String contactPhone;
    
    /**
     * 每日发送限额
     */
    @NotNull(message = "每日发送限额不能为空")
    private Integer dailyLimit;
    
    /**
     * 每月发送限额
     */
    private Integer monthlyLimit;
    
    /**
     * 单条短信最大长度
     */
    private Integer maxSmsLength;
    
    /**
     * 默认签名
     */
    private String defaultSignature;
    
    /**
     * 回调 URL (状态报告接收地址)
     */
    private String callbackUrl;
    
    /**
     * 是否启用 IP 白名单
     */
    private Boolean enableIpWhitelist = false;
    
    /**
     * IP 白名单列表 (逗号分隔)
     */
    private String ipWhitelist;
    
    /**
     * 备注
     */
    private String remark;
}
