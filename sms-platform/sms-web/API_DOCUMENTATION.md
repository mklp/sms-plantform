# 短信平台管理后台 API 接口文档

## 概述

本文档描述了短信平台管理后台的 RESTful API 接口规范。所有接口均支持 Swagger/OpenAPI 3.0 标准，可通过以下地址访问：

- **Swagger UI**: http://localhost:8080/sms-api/swagger-ui.html
- **API 文档 JSON**: http://localhost:8080/sms-api/v3/api-docs

## 认证方式

所有管理后台接口需要使用 Bearer Token 进行认证，在请求头中添加：
```
Authorization: Bearer <your_jwt_token>
```

## 统一响应格式

所有接口返回统一的 JSON 格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1704067200000
}
```

## API 列表

### 1. 短信发送接口 (API 侧)

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 单条发送 | POST | /api/v1/sms/send | 发送单条短信 |
| 批量发送 | POST | /api/v1/sms/batch/send | 批量发送短信（最多 100 条） |
| 查询状态 | GET | /api/v1/sms/status/{recordId} | 查询发送状态 |

### 2. 通道管理接口

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 查询列表 | GET | /admin/v1/channels | 分页查询通道列表 |
| 查询详情 | GET | /admin/v1/channels/{id} | 查询通道详情 |
| 创建通道 | POST | /admin/v1/channels | 新增通道配置 |
| 更新通道 | PUT | /admin/v1/channels/{id} | 更新通道配置 |
| 删除通道 | DELETE | /admin/v1/channels/{id} | 删除通道 |
| 切换状态 | POST | /admin/v1/channels/{id}/toggle | 启用/禁用通道 |
| 查询统计 | GET | /admin/v1/channels/{id}/stats | 查询通道统计 |

### 3. 租户管理接口

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 查询列表 | GET | /admin/v1/tenants | 分页查询租户列表 |
| 查询详情 | GET | /admin/v1/tenants/{tenantId} | 查询租户详情 |
| 创建租户 | POST | /admin/v1/tenants | 新增租户 |
| 更新租户 | PUT | /admin/v1/tenants/{tenantId} | 更新租户信息 |
| 删除租户 | DELETE | /admin/v1/tenants/{tenantId} | 删除租户 |
| 切换状态 | POST | /admin/v1/tenants/{tenantId}/toggle | 启用/禁用租户 |
| 查询统计 | GET | /admin/v1/tenants/{tenantId}/stats | 查询租户统计 |

### 4. 发送记录查询接口

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 查询列表 | GET | /admin/v1/records | 分页查询发送记录（支持冷热数据分离） |
| 查询详情 | GET | /admin/v1/records/{id} | 查询记录详情 |
| 导出记录 | GET | /admin/v1/records/export | 导出发送记录为 CSV/Excel |

## 请求参数示例

### 单条短信发送

```json
POST /api/v1/sms/send
{
  "phone": "13800138000",
  "content": "【签名】您的验证码是 123456",
  "bizType": "VERIFY_CODE",
  "outOrderId": "order_202401010001"
}
```

### 批量短信发送

```json
POST /api/v1/sms/batch/send
{
  "phones": ["13800138000", "13800138001"],
  "content": "【签名】您的验证码是 123456",
  "bizType": "NOTICE",
  "async": true
}
```

### 创建通道

```json
POST /admin/v1/channels
{
  "channelName": "移动网关 1",
  "operatorType": "CMCC",
  "priority": 1,
  "weight": 100,
  "dailyLimit": 100000,
  "qpsLimit": 500,
  "enabled": true,
  "gatewayIp": "192.168.1.100",
  "gatewayPort": 7890,
  "enterpriseCode": "901000",
  "sharedSecret": "secret123",
  "remark": "主用通道"
}
```

### 查询发送记录

```
GET /admin/v1/records?phone=13800138000&startTime=2024-01-01T00:00:00&endTime=2024-01-01T23:59:59&queryHot=true&pageNum=1&pageSize=20
```

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数校验失败 |
| 401 | 未授权 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 500 | 系统内部错误 |

## 频控限制

- 单号码每分钟最多发送 60 条
- 单号码每小时最多发送 300 条
- 单号码每天最多发送 1000 条
- 批量发送单次最多 100 条

## 数据说明

### 冷热数据分离

- **热表**: 存储最近 7 天的发送记录，支持高频读写
- **冷表**: 存储历史数据，按月归档，仅支持查询

查询时可通过 `queryHot` 参数指定查询热表或冷表。
