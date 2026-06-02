package com.zhongway.sms.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 租户账户实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sms_tenant_account")
public class TenantAccount extends BaseEntity {

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 租户 ID
     */
    private String tenantId;

    /**
     * 现金余额 (元)
     */
    private BigDecimal cashBalance;

    /**
     * 赠送余额 (元)
     */
    private BigDecimal giftBalance;

    /**
     * 套餐包剩余条数
     */
    private Long packageBalance;

    /**
     * 累计充值金额
     */
    private BigDecimal totalRecharge;

    /**
     * 累计消费金额
     */
    private BigDecimal totalConsume;

    /**
     * 状态：1-正常，0-停用，-1-欠费
     */
    private Short status;

    /**
     * 乐观锁版本号
     */
    @Version
    private Long version;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
