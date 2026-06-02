package com.zhongway.sms.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 短信状态报告状态枚举
 */
public enum SmsReportStatus {
    
    /**
     * 等待报告
     */
    PENDING("PENDING", "等待报告"),
    
    /**
     * 送达成功
     */
    SUCCESS("DELIVRD", "送达成功"),
    
    /**
     * 送达失败
     */
    FAILED("UNDELIV", "送达失败"),
    
    /**
     * 过期
     */
    EXPIRED("EXPIRED", "过期"),
    
    /**
     * 删除
     */
    DELETED("DELETED", "删除"),
    
    /**
     * 未知状态
     */
    UNKNOWN("UNKNOWN", "未知状态");

    @EnumValue
    private final String code;
    
    private final String description;

    SmsReportStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    @JsonValue
    public String getValue() {
        return code;
    }

    /**
     * 根据代码获取枚举
     */
    public static SmsReportStatus fromCode(String code) {
        if (code == null) {
            return UNKNOWN;
        }
        for (SmsReportStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return UNKNOWN;
    }
}
