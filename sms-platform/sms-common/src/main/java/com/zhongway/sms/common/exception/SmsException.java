package com.zhongway.sms.common.exception;

/**
 * 短信业务异常基类
 */
public class SmsException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private String errorCode;
    
    public SmsException(String message) {
        super(message);
    }
    
    public SmsException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public SmsException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public SmsException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
