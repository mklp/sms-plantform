package com.example.sms.core.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 短信发送响应模型
 */
public class SmsSubmitResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 消息 ID
     */
    private String msgId;

    /**
     * 提交是否成功
     */
    private Boolean success;

    /**
     * 响应码 (运营商返回)
     */
    private String respCode;

    /**
     * 响应消息
     */
    private String respMsg;

    /**
     * 通道 ID
     */
    private Long channelId;

    /**
     * 通道名称
     */
    private String channelName;

    /**
     * 提交时间
     */
    private LocalDateTime submitTime;

    /**
     * 计费条数
     */
    private Integer chargeCount = 1;

    public SmsSubmitResponse() {
    }

    public SmsSubmitResponse(String msgId, Boolean success, String respCode, String respMsg) {
        this.msgId = msgId;
        this.success = success;
        this.respCode = respCode;
        this.respMsg = respMsg;
    }

    // Getters and Setters
    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getRespCode() {
        return respCode;
    }

    public void setRespCode(String respCode) {
        this.respCode = respCode;
    }

    public String getRespMsg() {
        return respMsg;
    }

    public void setRespMsg(String respMsg) {
        this.respMsg = respMsg;
    }

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public LocalDateTime getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(LocalDateTime submitTime) {
        this.submitTime = submitTime;
    }

    public Integer getChargeCount() {
        return chargeCount;
    }

    public void setChargeCount(Integer chargeCount) {
        this.chargeCount = chargeCount;
    }
}
