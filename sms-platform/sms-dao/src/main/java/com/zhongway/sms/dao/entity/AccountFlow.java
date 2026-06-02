package com.zhongway.sms.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 资金流水实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sms_account_flow")
public class AccountFlow extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 流水号
     */
    private String flowNo;

    /**
     * 租户 ID
     */
    private String tenantId;

    /**
     * 业务类型：RECHARGE-充值，SEND-发送扣费，REFUND-退款
     */
    private String businessType;

    /**
     * 变动金额 (正数为增，负数为减)
     */
    private BigDecimal amount;

    /**
     * 变动后余额快照
     */
    private BigDecimal balanceSnapshot;

    /**
     * 关联业务 ID
     */
    private String relatedId;

    /**
     * 备注
     */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
