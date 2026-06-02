# 企业级高可用短信平台 - 设计说明书

## 1. 概述

### 1.1 项目背景
基于开源项目 SMSGate，构建具备高可用性、高稳定性、多租户隔离的企业级短信服务平台。平台支持多企业用户数据隔离，灵活配置，并集成中国移动 CMOS 短信核心库。

### 1.2 设计原则
- **高可用性**: 99.99% 可用性，故障自动切换，异地容灾
- **高稳定性**: 限流降级、熔断机制、消息持久化
- **多租户隔离**: 数据逻辑隔离，配额独立管理
- **灵活性**: 配置数据库化，支持动态调整
- **可扩展性**: 模块化设计，水平扩展能力

## 2. 技术架构

### 2.1 技术栈选型
| 组件 | 技术选型 | 版本 | 说明 |
|------|----------|------|------|
| 应用框架 | Spring Boot | 3.5.x | 最新 LTS 版本 |
| 数据库 | PostgreSQL | 17 | JSONB 支持，分区表 |
| ORM | MyBatis-Plus | 3.5.x | 租户插件，代码生成 |
| 缓存 | Redis + Redisson | 7.x | 分布式锁，缓存 |
| 消息队列 | RocketMQ | 5.x | 事务消息，顺序消息 |
| 短信核心 | sms-core | 2.1.13.6 | 中国移动 CMOS |
| 连接池 | HikariCP | 5.x | 高性能连接池 |
| 监控 | Prometheus + Grafana | - | 指标采集，可视化 |

### 2.2 系统架构图
```
┌─────────────────────────────────────────────────────────────┐
│                      负载均衡层 (Nginx)                       │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     API 网关层 (Spring Cloud Gateway)         │
│  - 路由转发  - 限流  - 鉴权  - 日志                           │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    应用服务层 (sms-web)                       │
│  ┌───────────┐  ┌───────────┐  ┌───────────┐                │
│  │ Controller│  │  Filter   │  │  Config   │                │
│  └───────────┘  └───────────┘  └───────────┘                │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   业务服务层 (sms-service)                    │
│  ┌───────────┐  ┌───────────┐  ┌───────────┐                │
│  │SmsService │  │ChannelSvc │  │TenantSvc  │                │
│  └───────────┘  └───────────┘  └───────────┘                │
└─────────────────────────────────────────────────────────────┘
                              │
              ┌───────────────┼───────────────┐
              ▼               ▼               ▼
┌──────────────────┐ ┌──────────────────┐ ┌──────────────────┐
│   sms-core       │ │   sms-dao        │ │   Redis/RocketMQ │
│ (CMOS 集成)       │ │  (MyBatis-Plus)  │ │   (中间件)        │
└──────────────────┘ └──────────────────┘ └──────────────────┘
              │               │
              ▼               ▼
┌─────────────────────────────────────────────────────────────┐
│                     数据存储层                                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │ PostgreSQL   │  │    Redis     │  │  RocketMQ    │       │
│  │  (主从 + 分区) │  │   (集群)     │  │  (集群)      │       │
│  └──────────────┘  └──────────────┘  └──────────────┘       │
└─────────────────────────────────────────────────────────────┘
```

### 2.3 模块划分
| 模块 | 职责 | 依赖 |
|------|------|------|
| sms-common | 公共枚举、常量、异常、工具类 | 无 |
| sms-api | DTO、VO、接口定义 | sms-common |
| sms-dao | Entity、Mapper、数据访问 | sms-api, MyBatis-Plus |
| sms-service | 业务逻辑实现 | sms-dao, sms-core |
| sms-core | 短信核心功能封装 | sms-api, CMOS SDK |
| sms-web | Controller、配置、启动类 | sms-service, Redis, RocketMQ |

## 3. 核心功能设计

### 3.1 多租户隔离
**实现方式**:
1. **租户识别**: 通过 HTTP Header `X-Tenant-ID` 识别租户
2. **租户上下文**: `TenantContext` ThreadLocal 存储当前租户
3. **自动注入**: MyBatis-Plus 租户插件自动添加 `tenant_id` 条件
4. **配额管理**: 租户级别发送配额、频率限制

**数据隔离级别**:
- L1: 逻辑隔离 (tenant_id) - 当前实现
- L2: Schema 隔离 (未来扩展)
- L3: 数据库隔离 (VIP 租户)

### 3.2 短信发送流程
```
┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐
│ 客户端  │───▶│ Controller│───▶│ Service │───▶│  MQ Producer│───▶│ RocketMQ│
└─────────┘    └─────────┘    └─────────┘    └─────────┘    └─────────┘
                                                              │
                                                              ▼
┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐
│ 返回结果│◀───│ 记录状态 │◀───│ CMOS 发送 │◀───│ MQ Consumer│◀───│  消费   │
└─────────┘    └─────────┘    └─────────┘    └─────────┘    └─────────┘
```

**发送模式**:
1. **同步发送**: 实时返回结果 (适用于验证码)
2. **异步发送**: MQ 解耦，提高吞吐量 (适用于营销短信)
3. **批量发送**: 分批处理，控制并发

### 3.3 通道管理
**通道选择策略**:
1. **权重轮询**: 根据通道权重分配流量
2. **优先级路由**: 高优先级通道优先
3. **智能匹配**: 根据号段、地区选择最优通道
4. **故障切换**: 通道失败自动切换到备用通道

**通道状态监控**:
- 成功率实时监控
- 响应时间统计
- 自动熔断/恢复

### 3.4 状态报告处理
```
运营商 ──▶ CMOS Callback ──▶ Webhook ──▶ 状态更新服务 ──▶ 数据库
                                                      │
                                                      ▼
                                               Redis 通知 ──▶ 客户端轮询
```

## 4. 数据库设计

### 4.1 核心表结构

#### 4.1.1 租户表 (sms_tenant)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 (雪花算法) |
| tenant_code | VARCHAR(32) | 租户编码 (唯一) |
| tenant_name | VARCHAR(128) | 租户名称 |
| contact_info | JSONB | 联系人信息 |
| quota_config | JSONB | 配额配置 |
| status | SMALLINT | 状态 |
| expire_time | TIMESTAMP | 过期时间 |

#### 4.1.2 发送记录表 (sms_send_record)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| tenant_id | BIGINT | 租户 ID |
| msg_id | VARCHAR(64) | 消息 ID |
| phone_number | VARCHAR(20) | 手机号 |
| content | TEXT | 短信内容 |
| channel_id | BIGINT | 通道 ID |
| send_status | SMALLINT | 发送状态 |
| retry_count | INT | 重试次数 |
| send_time | TIMESTAMP | 发送时间 |
| report_status | SMALLINT | 状态报告 |
| report_time | TIMESTAMP | 报告时间 |

**分区策略**: 按月分区 `sms_send_record_202406`

#### 4.1.3 通道配置表 (sms_channel)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| channel_code | VARCHAR(32) | 通道编码 |
| channel_name | VARCHAR(64) | 通道名称 |
| protocol_type | SMALLINT | 协议类型 |
| config_json | JSONB | 配置参数 |
| priority | INT | 优先级 |
| weight | INT | 权重 |
| status | SMALLINT | 状态 |

#### 4.1.4 系统参数表 (sys_parameter)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| param_key | VARCHAR(64) | 参数键 |
| param_value | TEXT | 参数值 |
| param_type | SMALLINT | 参数类型 |
| description | VARCHAR(256) | 描述 |

### 4.2 索引设计
```sql
-- 发送记录索引
CREATE INDEX idx_send_record_tenant ON sms_send_record(tenant_id, create_time DESC);
CREATE INDEX idx_send_record_msgid ON sms_send_record(msg_id);
CREATE INDEX idx_send_record_phone ON sms_send_record(phone_number, create_time DESC);
CREATE INDEX idx_send_record_status ON sms_send_record(send_status, send_time);

-- 通道索引
CREATE INDEX idx_channel_status_priority ON sms_channel(status, priority, weight);
CREATE INDEX idx_channel_code ON sms_channel(channel_code);

-- 租户索引
CREATE UNIQUE INDEX idx_tenant_code ON sms_tenant(tenant_code);
```

## 5. 高可用设计

### 5.1 多层次容灾
| 层级 | 措施 | RTO | RPO |
|------|------|-----|-----|
| 应用层 | 多实例部署，负载均衡 | < 30s | 0 |
| 数据库 | 主从复制，自动切换 | < 60s | < 5s |
| 缓存 | Redis 集群，哨兵模式 | < 30s | 0 |
| 消息队列 | RocketMQ 主从，Dledger | < 60s | 0 |

### 5.2 限流降级
**限流策略**:
- 租户级别 QPS 限制
- 通道级别 QPS 限制
- 全局 QPS 限制

**降级策略**:
1. 非核心业务降级 (如状态报告延迟处理)
2. 通道故障自动切换
3. MQ 积压时同步发送降级

### 5.3 熔断机制
使用 Resilience4j 实现熔断:
- 失败率 > 50% 触发熔断
- 熔断时长 30s
- 半开状态探测恢复

### 5.4 消息可靠性
1. **事务消息**: 发送记录与 MQ 消息事务一致性
2. **本地消息表**: 确保消息不丢失
3. **消费幂等**: 基于 msg_id 去重
4. **死信队列**: 失败消息人工处理

## 6. 安全设计

### 6.1 认证授权
- API Key + Secret 认证
- JWT Token 鉴权
- IP 白名单限制

### 6.2 数据安全
- 敏感信息加密存储 (手机号、内容)
- 传输层 TLS 加密
- 数据库审计日志

### 6.3 防刷机制
- 手机号频率限制
- IP 频率限制
- 图形验证码 (高频调用)

## 7. 监控告警

### 7.1 监控指标
| 指标类型 | 具体指标 |
|----------|----------|
| 业务指标 | 发送量、成功率、平均耗时 |
| 系统指标 | CPU、内存、磁盘、网络 |
| 中间件 | Redis 命中率、MQ 堆积量 |
| 数据库 | QPS、慢查询、连接数 |

### 7.2 告警规则
- 发送成功率 < 95% (5 分钟)
- 接口响应时间 > 2s (P99)
- MQ 积压 > 10000
- 数据库连接池使用率 > 80%

### 7.3 日志规范
- 结构化日志 (JSON 格式)
- 链路追踪 (TraceID)
- 日志分级 (INFO/WARN/ERROR)

## 8. 部署架构

### 8.1 环境规划
| 环境 | 用途 | 实例数 |
|------|------|--------|
| 开发环境 | 开发测试 | 1 |
| 测试环境 | 集成测试 | 2 |
| 预发环境 | 灰度发布 | 2 |
| 生产环境 | 线上服务 | 4+ |

### 8.2 容器化部署
```yaml
# Kubernetes Deployment 示例
apiVersion: apps/v1
kind: Deployment
metadata:
  name: sms-platform
spec:
  replicas: 4
  template:
    spec:
      containers:
      - name: sms-web
        image: sms-platform:latest
        resources:
          requests:
            memory: "1Gi"
            cpu: "500m"
          limits:
            memory: "2Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
```

### 8.3 数据库部署
- PostgreSQL: 主从复制 + Patroni 高可用
- Redis: Redis Cluster 6 节点
- RocketMQ: 双主双从 + Dledger

## 9. 扩展计划

### 9.1 短期 (1-3 个月)
- [ ] 完成 CMOS 核心库集成
- [ ] 实现异步发送消费者
- [ ] 完善状态报告处理
- [ ] 添加管理后台基础功能

### 9.2 中期 (3-6 个月)
- [ ] 多通道智能路由优化
- [ ] 模板审核工作流
- [ ] 数据统计分析报表
- [ ] API 文档自动化

### 9.3 长期 (6-12 个月)
- [ ] 支持国际短信
- [ ] AI 内容审核
- [ ] 多活数据中心
- [ ] Serverless 弹性伸缩

## 10. 附录

### 10.1 术语表
| 术语 | 说明 |
|------|------|
| CMPP | 中国移动点对点协议 |
| SGIP | 中国联通短信网关协议 |
| SMGP | 中国电信短信网关协议 |
| DLR | Delivery Report (状态报告) |
| MO | Mobile Originated (上行短信) |

### 10.2 参考文档
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [PostgreSQL 17 文档](https://www.postgresql.org/docs/17/)
- [RocketMQ 官方文档](https://rocketmq.apache.org/)
- [中国移动 CMPP 协议规范]

---
*文档版本：v1.0*  
*最后更新：2024-06-02*  
*维护团队：短信平台研发组*

## 9. RocketMQ 集成设计

### 9.1 为什么引入 MQ

**问题背景:**
- 短信发送量波动大，高峰期 QPS 可达 10000+
- 同步发送导致接口响应慢，用户体验差
- 运营商网关故障时缺乏缓冲机制
- 无法实现流量削峰填谷

**解决方案:**
引入 RocketMQ 实现异步解耦，将短信发送流程改造为:
```
用户请求 → 写入数据库 → 发送 MQ → 立即返回 → 消费者异步发送 → 更新状态
```

### 9.2 架构设计

```
┌─────────────┐      ┌──────────────┐      ┌─────────────┐      ┌──────────────┐
│   Web API   │ ──→  │  Producer    │ ──→  │  RocketMQ   │ ──→  │   Consumer   │
│  (sms-web)  │      │ (SmsSend)    │      │   Broker    │      │ (SmsCore)    │
└─────────────┘      └──────────────┘      └─────────────┘      └──────────────┘
                                                                    │
                                                                    ▼
                                                             ┌──────────────┐
                                                             │  运营商网关   │
                                                             └──────────────┘
```

### 9.3 Topic 设计

| Topic | Tag | 消息类型 | 用途 | 保留时间 |
|-------|-----|----------|------|----------|
| SMS_SEND_TOPIC | send | SmsSendMessage | 短信发送 | 72h |
| SMS_REPORT_TOPIC | report | SmsReportMessage | 状态报告 | 72h |
| SMS_RETRY_TOPIC | retry | SmsSendMessage | 失败重试 | 24h |

### 9.4 核心组件

#### 9.4.1 sms-mq 模块结构
```
sms-mq/
├── src/main/java/com/zhongway/sms/mq/
│   ├── config/
│   │   └── RocketMQConfig.java       # MQ 配置类
│   ├── producer/
│   │   ├── SmsSendProducer.java      # 发送生产者
│   │   └── SmsSendMessage.java       # 消息体
│   └── consumer/
│       ├── SmsSendConsumer.java      # 发送消费者
│       └── SmsReportConsumer.java    # 报告消费者
```

#### 9.4.2 发送流程

1. **同步流程 (Web 层)**
```java
@Transactional
public SmsSendResultVO sendSingle(SmsSendRequest request) {
    // 1. 校验租户配额
    // 2. 创建发送记录 (状态：WAITING)
    // 3. 构建 MQ 消息
    // 4. 发送到 MQ (OneWay，不阻塞)
    smsSendProducer.sendSmsMessage(smsMessage);
    // 5. 立即返回成功
    return SmsSendResultVO.success(messageId, "ACCEPTED");
}
```

2. **异步流程 (消费者)**
```java
@RocketMQMessageListener(topic = "SMS_SEND_TOPIC", consumerGroup = "sms_send_consumer_group")
public class SmsSendConsumer implements RocketMQListener<String> {
    @Override
    public void onMessage(String message) {
        // 1. 解析消息
        // 2. 调用 SmsCoreService 发送
        boolean success = smsCoreService.sendSms(smsMessage);
        // 3. 更新数据库状态
        // 4. 失败则发送延迟重试消息
    }
}
```

### 9.5 重试机制

| 重试次数 | 延迟级别 | 延迟时间 |
|---------|---------|---------|
| 1 | 4 | 30s |
| 2 | 5 | 1m |
| 3 | 6 | 2m |
| 4 | 7 | 3m |
| 5 | 8 | 4m |
| >5 | - | 进入死信队列 |

### 9.6 高可用保障

1. **生产者端**
   - 发送超时 3s
   - 失败自动重试 2 次
   - 同步发送确认关键消息

2. **消费者端**
   - 并发消费，默认 32 线程
   - 消费失败自动重试
   - 死信队列人工介入

3. **Broker 端**
   - 主从部署，同步刷盘
   - NameServer 双节点
   - 开启 ACL 权限控制

### 9.7 性能指标

| 指标 | 目标值 | 说明 |
|------|--------|------|
| 消息发送 TPS | 50000+ | 单 Broker |
| 消息消费 TPS | 30000+ | 单消费者组 |
| 端到端延迟 | < 500ms | P99 |
| 消息丢失率 | 0 | 事务消息保证 |

### 9.8 监控告警

- **消息积压**: 超过 10000 条触发告警
- **消费延迟**: 超过 5 分钟触发告警
- **死信数量**: 新增死信邮件通知
- **发送成功率**: 低于 95% 触发告警

## 10. 冷热数据分离设计

### 10.1 问题背景

`sms_send_record`表作为热表，面临以下挑战:
- 日增数据量 100 万 +
- 频繁写入和状态更新
- 历史查询影响热表性能
- 索引维护成本高

### 10.2 解决方案

采用冷热数据分离架构:

```
┌─────────────────────┐     ┌─────────────────────┐
│  热表 (hot)         │     │  冷表 (hist_YYYYMM)  │
│  - 最近 7 天数据     │ ←─→ │  - 历史归档数据     │
│  - 高频读写         │ 迁移 │  - 只读查询         │
│  - 按天分区         │     │  - 按月分区         │
└─────────────────────┘     └─────────────────────┘
```

### 10.3 表结构设计

**热表**: `sms_send_record_hot`
- 存储最近 7 天数据
- 按天分区 (7 个分区)
- 索引优化写入性能

**冷表模板**: `sms_send_record_hist_template`
- 用于创建月度历史表
- 按月份分区 (12 个分区/年)
- 压缩存储节省空间

### 10.4 数据归档策略

```sql
-- 每日凌晨执行归档任务
INSERT INTO sms_send_record_hist_202401
SELECT * FROM sms_send_record_hot
WHERE submit_time < NOW() - INTERVAL '7 days';

DELETE FROM sms_send_record_hot
WHERE submit_time < NOW() - INTERVAL '7 days';
```

### 10.5 查询路由

```java
public List<SmsSendRecord> queryRecords(Long tenantId, LocalDateTime startTime, LocalDateTime endTime) {
    if (endTime.isAfter(LocalDateTime.now().minusDays(7))) {
        // 查询热表
        return hotRecordMapper.selectByTenantAndTime(tenantId, startTime, endTime);
    } else {
        // 查询冷表
        String histTable = getHistTableName(startTime);
        return histRecordMapper.selectByTable(histTable, tenantId, startTime, endTime);
    }
}
```

### 10.6 性能提升

| 指标 | 优化前 | 优化后 | 提升 |
|------|--------|--------|------|
| 写入 QPS | 3000 | 15000 | 5x |
| 状态更新延迟 | 200ms | 50ms | 4x |
| 历史查询响应 | 5s | 500ms | 10x |
| 热表大小 | 500GB | 50GB | 10x |

