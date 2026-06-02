package com.zhongway.sms.core.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 状态报告模型
 */
public class DeliveryReport implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 消息 ID (与发送时的 msgId 对应)
     */
    private String msgId;

    /**
     * 运营商返回的消息 ID
     */
    private String nativeMsgId;

    /**
     * 手机号
     */
    private String phoneNumber;

    /**
     * 状态码 (运营商返回)
     */
    private String statusCode;

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 送达时间
     */
    private LocalDateTime deliverTime;

    /**
     * 通道 ID
     */
    private Long channelId;

    /**
     * 是否成功送达
     */
    private Boolean success;

    // Getters and Setters
    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getNativeMsgId() {
        return nativeMsgId;
    }

    public void setNativeMsgId(String nativeMsgId) {
        this.nativeMsgId = nativeMsgId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusDesc() {
        return statusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }

    public LocalDateTime getDeliverTime() {
        return deliverTime;
    }

    public void setDeliverTime(LocalDateTime deliverTime) {
        this.deliverTime = deliverTime;
    }

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }
}
