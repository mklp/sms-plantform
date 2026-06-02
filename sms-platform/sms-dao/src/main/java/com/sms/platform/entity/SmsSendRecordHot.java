package com.sms.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 短信发送记录热表实体 (最近 7 天高频数据)
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sms_send_record_hot")
public class SmsSendRecordHot implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 租户 ID
     */
    @TableField("tenant_id")
    private Long tenantId;

    /**
     * 消息 ID (全局唯一)
     */
    @TableField("message_id")
    private String messageId;

    /**
     * 外部订单号 (用于幂等控制)
     */
    @TableField("out_order_id")
    private String outOrderId;

    /**
     * 接收手机号
     */
    @TableField("phone_number")
    private String phoneNumber;

    /**
     * 短信内容
     */
    @TableField("content")
    private String content;

    /**
     * 短信签名
     */
    @TableField("signature")
    private String signature;

    /**
     * 完整短信 (含签名)
     */
    @TableField("full_content")
    private String fullContent;

    /**
     * 模板 ID
     */
    @TableField("template_id")
    private Long templateId;

    /**
     * 模板参数 (JSON 格式)
     */
    @TableField(value = "template_params", typeHandler = org.apache.ibatis.type.JdbcType.OTHER)
    private Object templateParams;

    /**
     * 业务类型
     */
    @TableField("biz_type")
    private String bizType;

    /**
     * 通道 ID
     */
    @TableField("channel_id")
    private Long channelId;

    /**
     * 协议类型
     */
    @TableField("protocol_type")
    private String protocolType;

    /**
     * 计费条数
     */
    @TableField("charge_count")
    private Integer chargeCount;

    /**
     * 单价
     */
    @TableField("unit_price")
    private BigDecimal unitPrice;

    /**
     * 总费用
     */
    @TableField("total_fee")
    private BigDecimal totalFee;

    /**
     * 发送状态：WAITING, SENDING, SUCCESS, FAILED
     */
    @TableField("send_status")
    private String sendStatus;

    /**
     * 提交时间
     */
    @TableField("submit_time")
    private LocalDateTime submitTime;

    /**
     * 发送时间
     */
    @TableField("send_time")
    private LocalDateTime sendTime;

    /**
     * 状态报告时间
     */
    @TableField("report_time")
    private LocalDateTime reportTime;

    /**
     * 状态报告内容
     */
    @TableField("report_content")
    private String reportContent;

    /**
     * 送达状态：DELIVERED, FAILED, UNKNOWN
     */
    @TableField("delivered_status")
    private String deliveredStatus;

    /**
     * 错误码
     */
    @TableField("error_code")
    private String errorCode;

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 重试次数
     */
    @TableField("retry_count")
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    @TableField("max_retry_count")
    private Integer maxRetryCount;

    /**
     * 是否已回调
     */
    @TableField("is_callback")
    private Boolean isCallback;

    /**
     * 回调时间
     */
    @TableField("callback_time")
    private LocalDateTime callbackTime;

    /**
     * 回调状态
     */
    @TableField("callback_status")
    private String callbackStatus;

    /**
     * 扩展数据 (JSON 格式)
     */
    @TableField(value = "ext_data", typeHandler = org.apache.ibatis.type.JdbcType.OTHER)
    private Object extData;

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
     * 逻辑删除标识
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
