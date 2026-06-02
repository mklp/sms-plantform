# 企业级高可用短信平台

基于 Spring Boot 3.5.x + PostgreSQL 17 构建的高可用、多租户短信服务平台。

## 技术栈

- **框架**: Spring Boot 3.5.x
- **数据库**: PostgreSQL 17
- **ORM**: MyBatis-Plus 3.5.x
- **缓存**: Redis + Redisson
- **消息队列**: RocketMQ 5.x
- **短信核心**: 中国移动 CMOS sms-core 2.1.13.6

## 项目结构

```
sms-platform/
├── sms-common/          # 公共模块 (枚举、常量、异常等)
├── sms-api/             # API 接口定义 (DTO、VO)
├── sms-dao/             # 数据访问层 (Entity、Mapper)
├── sms-service/         # 业务服务层
├── sms-core/            # 短信核心功能 (集成 sms-core)
└── sms-web/             # Web 应用层 (Controller、配置)
```

## 快速开始

### 1. 数据库初始化

```bash
psql -U postgres -d sms_platform -f sql/init_schema.sql
```

### 2. 配置环境变量

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=sms_platform
export DB_USERNAME=postgres
export DB_PASSWORD=postgres

export REDIS_HOST=localhost
export REDIS_PORT=6379

export ROCKETMQ_NAME_SERVER=localhost:9876
```

### 3. 启动应用

```bash
cd sms-web
mvn spring-boot:run
```

### 4. API 测试

```bash
# 发送短信
curl -X POST http://localhost:8080/sms-api/sms/send \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: 1000000000000000001" \
  -d '{
    "phoneNumber": "13800138000",
    "content": "您的验证码是 123456"
  }'

# 查询状态
curl http://localhost:8080/sms-api/sms/status/SMS_xxx \
  -H "X-Tenant-ID: 1000000000000000001"
```

## 核心特性

### 多租户隔离
- 基于 `tenant_id` 的逻辑隔离
- 自动注入租户条件，防止越权访问
- 支持租户配额管理

### 高可用设计
- 异步发送模型 (RocketMQ)
- 通道故障自动切换
- 失败重试机制
- 分布式锁防重

### 灵活配置
- 通道配置 JSONB 存储
- 系统参数数据库动态配置
- 无需重启生效

## 文档

- [设计说明](DESIGN_DOC.md)
- [数据库脚本](sql/init_schema.sql)

## License

MIT
