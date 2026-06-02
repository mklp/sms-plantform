package com.example.sms.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 企业租户实体表
 * 多租户数据隔离核心表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sms_tenant")
public class SmsTenant implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 租户编码 (唯一标识)
     */
    @TableField("tenant_code")
    private String tenantCode;
    
    /**
     * 租户名称
     */
    @TableField("tenant_name")
    private String tenantName;
    
    /**
     * 联系人
     */
    @TableField("contact_person")
    private String contactPerson;
    
    /**
     * 联系电话
     */
    @TableField("contact_phone")
    private String contactPhone;
    
    /**
     * 联系邮箱
     */
    @TableField("contact_email")
    private String contactEmail;
    
    /**
     * 每日发送限额
     */
    @TableField("daily_limit")
    private Integer dailyLimit;
    
    /**
     * 每月发送限额
     */
    @TableField("monthly_limit")
    private Integer monthlyLimit;
    
    /**
     * 当日已发送数量
     */
    @TableField("today_sent_count")
    private Integer todaySentCount;
    
    /**
     * 当月已发送数量
     */
    @TableField("month_sent_count")
    private Integer monthSentCount;
    
    /**
     * 默认签名
     */
    @TableField("default_signature")
    private String defaultSignature;
    
    /**
     * 回调 URL
     */
    @TableField("callback_url")
    private String callbackUrl;
    
    /**
     * 是否启用 IP 白名单
     */
    @TableField("enable_ip_whitelist")
    private Boolean enableIpWhitelist;
    
    /**
     * IP 白名单列表
     */
    @TableField("ip_whitelist")
    private String ipWhitelist;
    
    /**
     * 状态：ACTIVE-正常，DISABLED-禁用，EXPIRED-已过期，AUDITING-审核中
     */
    @TableField("status")
    private String status;
    
    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
    
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 逻辑删除标志：0-未删除，1-已删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
