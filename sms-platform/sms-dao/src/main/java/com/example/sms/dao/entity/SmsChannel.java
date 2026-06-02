package com.example.sms.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 短信通道配置表
 * 配置与运营商网关连接的通道信息
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sms_channel")
public class SmsChannel implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键 ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 通道编码
     */
    @TableField("channel_code")
    private String channelCode;
    
    /**
     * 通道名称
     */
    @TableField("channel_name")
    private String channelName;
    
    /**
     * 协议类型：CMPP、SMGP、SGIP、SMPP
     */
    @TableField("protocol_type")
    private String protocolType;
    
    /**
     * 运营商：MOBILE-移动，UNICOM-联通，TELECOM-电信
     */
    @TableField("operator")
    private String operator;
    
    /**
     * 网关主机地址
     */
    @TableField("gateway_host")
    private String gatewayHost;
    
    /**
     * 网关端口
     */
    @TableField("gateway_port")
    private Integer gatewayPort;
    
    /**
     * 企业 ID/账号
     */
    @TableField("enterprise_id")
    private String enterpriseId;
    
    /**
     * 共享密钥
     */
    @TableField("shared_secret")
    private String sharedSecret;
    
    /**
     * 服务类型
     */
    @TableField("service_type")
    private String serviceType;
    
    /**
     * 接入类型：0-普通，1-直连
     */
    @TableField("access_type")
    private Integer accessType;
    
    /**
     * 最大连接数
     */
    @TableField("max_connections")
    private Integer maxConnections;
    
    /**
     * 每秒发送限制 (QPS)
     */
    @TableField("qps_limit")
    private Integer qpsLimit;
    
    /**
     * 每日发送限额
     */
    @TableField("daily_limit")
    private Integer dailyLimit;
    
    /**
     * 当前已发送数量
     */
    @TableField("today_sent_count")
    private Integer todaySentCount;
    
    /**
     * 权重 (用于负载均衡)
     */
    @TableField("weight")
    private Integer weight;
    
    /**
     * 状态：ACTIVE-启用，DISABLED-禁用
     */
    @TableField("status")
    private String status;
    
    /**
     * 心跳间隔 (秒)
     */
    @TableField("heartbeat_interval")
    private Integer heartbeatInterval;
    
    /**
     * 超时时间 (毫秒)
     */
    @TableField("timeout_ms")
    private Integer timeoutMs;
    
    /**
     * 重试次数
     */
    @TableField("retry_count")
    private Integer retryCount;
    
    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
    
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 逻辑删除标志
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
