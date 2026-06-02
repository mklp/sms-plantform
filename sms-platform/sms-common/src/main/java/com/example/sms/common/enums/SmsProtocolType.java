package com.example.sms.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 短信协议类型枚举
 * 支持 CMPP、SMGP、SGIP、SMPP 等主流短信协议
 */
@Getter
@AllArgsConstructor
public enum SmsProtocolType {
    
    CMPP("CMPP", "中国移动 CMPP 协议", 1),
    SMGP("SMGP", "中国电信 SMGP 协议", 2),
    SGIP("SGIP", "中国联通 SGIP 协议", 3),
    SMPP("SMPP", "国际通用 SMPP 协议", 4),
    HTTP("HTTP", "HTTP REST API", 5);
    
    private final String code;
    private final String description;
    private final int value;
    
    public static SmsProtocolType fromValue(int value) {
        for (SmsProtocolType type : values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown protocol type: " + value);
    }
    
    public static SmsProtocolType fromCode(String code) {
        for (SmsProtocolType type : values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown protocol type: " + code);
    }
}
