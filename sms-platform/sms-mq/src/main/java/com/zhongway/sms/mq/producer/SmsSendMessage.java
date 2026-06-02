package com.zhongway.sms.mq.producer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 短信发送消息体
 * 
 * @author SMS Platform Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsSendMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息 ID
     */
    private String messageId;

    /**
     * 租户 ID
     */
    private Long tenantId;

    /**
     * 手机号码
     */
    private String phoneNumber;

    /**
     * 短信内容
     */
    private String content;

    /**
     * 签名
     */
    private String signature;

    /**
     * 完整内容（签名 + 内容）
     */
    private String fullContent;

    /**
     * 通道 ID
     */
    private Long channelId;

    /**
     * 通道编码
     */
    private String channelCode;

    /**
     * 协议类型 (CMPP/SMGP/SGIP 等)
     */
    private String protocolType;

    /**
     * 业务类型
     */
    private String bizType;

    /**
     * 外部订单 ID
     */
    private String outOrderId;

    /**
     * 计费条数
     */
    private Integer chargeCount;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 发送时间戳
     */
    private Long sendTimestamp;
}
