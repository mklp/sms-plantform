package com.example.sms.core.model;

import java.io.Serializable;
import java.util.List;

/**
 * 短信发送请求模型
 */
public class SmsSubmitRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 消息 ID (全局唯一)
     */
    private String msgId;

    /**
     * 租户 ID
     */
    private Long tenantId;

    /**
     * 通道 ID
     */
    private Long channelId;

    /**
     * 接收手机号列表
     */
    private List<String> destPhones;

    /**
     * 短信内容
     */
    private String content;

    /**
     * 签名
     */
    private String signature;

    /**
     * 模板 ID (如使用模板发送)
     */
    private String templateId;

    /**
     * 模板参数列表
     */
    private List<String> templateParams;

    /**
     * 优先级 (1-10, 越高越优先)
     */
    private Integer priority = 5;

    /**
     * 定时发送时间 (为空表示立即发送)
     */
    private Long scheduleTime;

    /**
     * 是否需要状态报告
     */
    private Boolean needReport = true;

    // Getters and Setters
    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

    public List<String> getDestPhones() {
        return destPhones;
    }

    public void setDestPhones(List<String> destPhones) {
        this.destPhones = destPhones;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public List<String> getTemplateParams() {
        return templateParams;
    }

    public void setTemplateParams(List<String> templateParams) {
        this.templateParams = templateParams;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Long getScheduleTime() {
        return scheduleTime;
    }

    public void setScheduleTime(Long scheduleTime) {
        this.scheduleTime = scheduleTime;
    }

    public Boolean getNeedReport() {
        return needReport;
    }

    public void setNeedReport(Boolean needReport) {
        this.needReport = needReport;
    }
}
