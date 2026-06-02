package com.example.sms.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 企业租户状态枚举
 */
@Getter
@AllArgsConstructor
public enum TenantStatus {
    
    ACTIVE("ACTIVE", "正常", 1),
    DISABLED("DISABLED", "禁用", 2),
    EXPIRED("EXPIRED", "已过期", 3),
    AUDITING("AUDITING", "审核中", 4);
    
    private final String code;
    private final String description;
    private final int value;
}
