package com.zhongway.sms.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 租户计费策略实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sms_tenant_pricing")
public class TenantPricing extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 租户 ID
     */
    private String tenantId;

    /**
     * 通道代码
     */
    private String channelCode;

    /**
     * 签名名称
     */
    private String signatureName;

    /**
     * 模板类型：NORMAL-普通，MARKETING-营销
     */
    private String templateType;

    /**
     * 单条价格 (元)
     */
    private BigDecimal pricePerSms;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 是否启用
     */
    private Boolean isActive;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
