# RocketMQ 集成说明

## 架构设计

### 消息流程图

```
用户请求 → Controller → Service → MQ Producer → RocketMQ → MQ Consumer → SMS Core → 运营商网关
                                            ↓
                                    (异步解耦、削峰填谷)
                                            ↓
运营商回执 ← Gateway ← SMS Core ← MQ Consumer ← RocketMQ ← 状态报告 Producer
```

### Topic 设计

| Topic | Tag | 用途 | 消息类型 |
|-------|-----|------|----------|
| SMS_SEND_TOPIC | send | 短信发送消息 | SmsSendMessage |
| SMS_REPORT_TOPIC | report | 状态报告消息 | SmsReportMessage |
| SMS_RETRY_TOPIC | retry | 失败重试消息 | SmsSendMessage |

### 消费者组设计

| Consumer Group | Topic | 用途 |
|----------------|-------|------|
| sms_send_consumer_group | SMS_SEND_TOPIC | 短信发送消费组 |
| sms_report_consumer_group | SMS_REPORT_TOPIC | 状态报告消费组 |
| sms_retry_consumer_group | SMS_RETRY_TOPIC | 失败重试消费组 |

## 核心组件

### 1. SmsSendProducer
- 提供 `sendSmsMessage()` 异步发送
- 提供 `sendSmsMessageSync()` 同步发送
- 提供 `sendDelayMessage()` 延迟发送（用于重试）

### 2. SmsSendConsumer
- 监听 SMS_SEND_TOPIC:send
- 调用 SmsCoreService 进行实际发送
- 支持异常自动重试

### 3. SmsReportConsumer
- 监听 SMS_REPORT_TOPIC:report
- 更新数据库中的发送记录状态
- 触发租户配额更新

## 使用示例

### 发送短信到 MQ

```java
@Autowired
private SmsSendProducer smsSendProducer;

public void sendSms() {
    SmsSendMessage message = SmsSendMessage.builder()
            .messageId("MSG_123456")
            .tenantId(1L)
            .phoneNumber("13800138000")
            .content("您的验证码是 123456")
            .channelId(1L)
            .build();
    
    // 异步发送，不阻塞
    smsSendProducer.sendSmsMessage(message);
}
```

### 配置说明

在 `application.yml` 中配置：

```yaml
rocketmq:
  name-server: ${ROCKETMQ_NAME_SERVER:127.0.0.1:9876}
  producer:
    group: sms-producer-group
    send-message-timeout: 3000
    retry-times-when-send-failed: 2
```

## 高可用设计

1. **生产者重试**: 发送失败自动重试 2 次
2. **消费者重试**: 消费失败自动重试，支持死信队列
3. **延迟消息**: 支持 18 个延迟级别，用于失败重试
4. **事务消息**: 可选开启，保证本地事务与消息发送的最终一致性

## 监控指标

- 消息发送成功率
- 消息消费延迟
- 死信队列消息数
- 消费者堆积量

## 部署建议

1. NameServer 至少部署 2 节点
2. Broker 采用主从模式部署
3. 开启 ACL 权限控制
4. 配置告警规则监控积压量
