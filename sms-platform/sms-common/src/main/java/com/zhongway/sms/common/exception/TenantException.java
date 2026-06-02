package com.zhongway.sms.common.exception;

/**
 * 租户相关异常
 */
public class TenantException extends SmsException {
    
    private static final long serialVersionUID = 1L;
    
    public TenantException(String message) {
        super("TENANT_ERROR", message);
    }
    
    public TenantException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    public TenantException(String message, Throwable cause) {
        super("TENANT_ERROR", message, cause);
    }
}
