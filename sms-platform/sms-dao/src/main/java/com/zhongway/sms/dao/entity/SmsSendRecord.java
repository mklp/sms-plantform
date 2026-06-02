package com.zhongway.sms.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 短信发送记录表
 * 存储所有短信发送的详细信息
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sms_send_record")
public class SmsSendRecord implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 租户 ID (多租户隔离关键字段)
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
     * 协议类型：CMPP、SMGP、SGIP、SMPP、HTTP
     */
    @TableField("protocol_type")
    private String protocolType;
    
    /**
     * 计费条数
     */
    @TableField("charge_count")
    private Integer chargeCount;
    
    /**
     * 发送状态：WAITING-待发送，SENDING-发送中，SUCCESS-成功，FAILED-失败，RETRYING-重试中，EXPIRED-过期，CANCELLED-已取消
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
     * 逻辑删除标志
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
