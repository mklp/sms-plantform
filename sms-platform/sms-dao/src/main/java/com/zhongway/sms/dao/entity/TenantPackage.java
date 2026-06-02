package com.zhongway.sms.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 租户套餐包实例实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sms_tenant_package")
public class TenantPackage extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 租户 ID
     */
    private String tenantId;

    /**
     * 套餐产品 ID
     */
    private Long packageProductId;

    /**
     * 购买总条数
     */
    private Long totalCount;

    /**
     * 剩余条数
     */
    private Long remainingCount;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 状态：1-有效，0-过期/用完
     */
    private Short status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
