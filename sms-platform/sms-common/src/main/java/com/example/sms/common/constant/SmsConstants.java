package com.example.sms.common.constant;

/**
 * 系统常量定义
 */
public final class SmsConstants {
    
    private SmsConstants() {
        throw new IllegalStateException("Constant class");
    }
    
    /**
     * 默认分页大小
     */
    public static final int DEFAULT_PAGE_SIZE = 20;
    
    /**
     * 最大分页大小
     */
    public static final int MAX_PAGE_SIZE = 100;
    
    /**
     * 短信内容最大长度 (单条)
     */
    public static final int SMS_MAX_LENGTH = 140;
    
    /**
     * 长短信最大分段数
     */
    public static final int MAX_LONG_SMS_PARTS = 10;
    
    /**
     * 短信签名最大长度
     */
    public static final int SIGNATURE_MAX_LENGTH = 20;
    
    /**
     * 手机号正则表达式 (中国大陆)
     */
    public static final String PHONE_REGEX = "^1[3-9]\\d{9}$";
    
    /**
     * Redis Key 前缀
     */
    public static final String REDIS_KEY_PREFIX = "sms:platform:";
    
    /**
     * RocketMQ Topic 命名
     */
    public static final String MQ_TOPIC_SMS_SEND = "SMS_SEND_TOPIC";
    public static final String MQ_TOPIC_SMS_DELIVERY = "SMS_DELIVERY_TOPIC";
    public static final String MQ_TOPIC_SMS_REPLY = "SMS_REPLY_TOPIC";
    
    /**
     * 消息重试次数
     */
    public static final int DEFAULT_RETRY_COUNT = 3;
    
    /**
     * 消息重试间隔 (秒)
     */
    public static final int DEFAULT_RETRY_INTERVAL_SECONDS = 60;
    
    /**
     * 分布式锁过期时间 (秒)
     */
    public static final int LOCK_EXPIRE_SECONDS = 30;
    
    /**
     * 租户隔离 Header 名称
     */
    public static final String TENANT_ID_HEADER = "X-Tenant-ID";
    
    /**
     * 数据库字段 - 逻辑删除
     */
    public static final String DELETED_FLAG = "deleted";
    public static final int NOT_DELETED = 0;
    public static final int DELETED = 1;
}
