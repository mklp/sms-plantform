package com.example.sms.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 短信发送状态枚举
 */
@Getter
@AllArgsConstructor
public enum SmsSendStatus {
    
    WAITING("WAITING", "待发送", 0),
    SENDING("SENDING", "发送中", 1),
    SUCCESS("SUCCESS", "发送成功", 2),
    FAILED("FAILED", "发送失败", 3),
    RETRYING("RETRYING", "重试中", 4),
    EXPIRED("EXPIRED", "已过期", 5),
    CANCELLED("CANCELLED", "已取消", 6);
    
    private final String code;
    private final String description;
    private final int value;
    
    public static SmsSendStatus fromValue(int value) {
        for (SmsSendStatus status : values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        return WAITING;
    }
    
    public static SmsSendStatus fromCode(String code) {
        for (SmsSendStatus status : values()) {
            if (status.getCode().equalsIgnoreCase(code)) {
                return status;
            }
        }
        return WAITING;
    }
}
