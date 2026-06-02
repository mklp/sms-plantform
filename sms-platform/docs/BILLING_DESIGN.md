# 租户额度控制设计说明

## 1. 概述

本模块实现了完整的租户计费与额度控制系统，支持多种计费模式和灵活的扣费策略，确保短信平台的商业化运营能力。

## 2. 核心功能

### 2.1 账户体系
- **现金余额**: 租户充值的真实资金，可提现（可选）
- **赠送余额**: 平台活动赠送的资金，通常不可提现
- **套餐包**: 预付费购买的短信条数包，有有效期限制

### 2.2 计费策略
- **按通道定价**: 不同短信通道（移动、联通、电信）价格不同
- **按签名定价**: 不同签名可设置不同价格
- **按模板类型**: 普通短信 vs 营销短信差异化定价
- **优先级机制**: 支持多套策略，按优先级自动匹配

### 2.3 扣费逻辑
```
扣费优先级：套餐包 > 赠送余额 > 现金余额

1. 优先从即将过期的套餐包扣减条数
2. 套餐包不足时，使用赠送余额
3. 最后使用现金余额
4. 全部不足时，发送失败并返回错误
```

## 3. 数据库设计

### 3.1 sms_tenant_account (租户账户表)
| 字段 | 类型 | 说明 |
|------|------|------|
| tenant_id | VARCHAR(64) | 租户 ID |
| cash_balance | DECIMAL(18,6) | 现金余额 |
| gift_balance | DECIMAL(18,6) | 赠送余额 |
| package_balance | BIGINT | 套餐包剩余条数 |
| total_recharge | DECIMAL(18,6) | 累计充值 |
| total_consume | DECIMAL(18,6) | 累计消费 |
| status | SMALLINT | 状态 |
| version | BIGINT | 乐观锁 |

### 3.2 sms_tenant_pricing (计费策略表)
| 字段 | 类型 | 说明 |
|------|------|------|
| tenant_id | VARCHAR(64) | 租户 ID |
| channel_code | VARCHAR(32) | 通道代码 |
| signature_name | VARCHAR(64) | 签名名称 |
| template_type | VARCHAR(32) | 模板类型 |
| price_per_sms | DECIMAL(18,6) | 单价 |
| priority | INT | 优先级 |

### 3.3 sms_tenant_package (套餐包实例表)
| 字段 | 类型 | 说明 |
|------|------|------|
| tenant_id | VARCHAR(64) | 租户 ID |
| package_product_id | BIGINT | 产品 ID |
| total_count | BIGINT | 总条数 |
| remaining_count | BIGINT | 剩余条数 |
| expire_time | TIMESTAMPTZ | 过期时间 |

### 3.4 sms_account_flow (资金流水表)
- 记录每一笔资金变动
- 支持审计和对账
- 包含余额快照

## 4. API 接口

### 4.1 查询账户余额
```
GET /api/billing/account/{tenantId}
```

### 4.2 账户充值
```
POST /api/billing/recharge
Params: tenantId, amount, isGift
```

### 4.3 试算费用
```
GET /api/billing/calculate-fee
Params: tenantId, channelCode, mobileCount, contentLength
```

### 4.4 检查余额
```
GET /api/billing/check-balance
Params: tenantId, amount
```

## 5. 关键特性

### 5.1 事务安全
- 扣费操作使用 `@Transactional` 保证原子性
- 使用 `SELECT FOR UPDATE` 防止并发扣费问题
- 乐观锁版本号防止更新丢失

### 5.2 长短信计算
- 70 字以内：1 条
- 超过 70 字：每 67 字为 1 条（向上取整）

### 5.3 套餐包过期处理
- 优先使用即将过期的套餐包
- 定时任务清理过期套餐包（待实现）

### 5.4 低余额告警
- 系统参数配置告警阈值
- 余额低于阈值时触发通知（待实现）

## 6. 扩展建议

1. **自动充值**: 对接支付网关，余额不足时自动充值
2. **信用额度**: 为优质客户提供透支额度
3. **阶梯定价**: 根据发送量设置阶梯价格
4. **账单导出**: 支持月度账单生成和导出
5. **预算控制**: 设置日/月消费上限

## 7. 使用示例

```java
// 1. 发送短信前试算费用
BigDecimal fee = billingService.calculateFee("TENANT_001", "CMPP_YD", 1, 50);

// 2. 检查余额
if (!billingService.checkBalance("TENANT_001", fee)) {
    return Result.fail("余额不足");
}

// 3. 发送成功后扣费
DeductionResult result = billingService.deductBalance(
    "TENANT_001", 
    fee, 
    "SEND", 
    sendRecord.getId()
);

if (!result.isSuccess()) {
    // 扣费失败，记录异常
    log.error("扣费失败：{}", result.getErrorMsg());
}
```

## 8. 注意事项

1. **并发控制**: 扣费必须加锁，防止超扣
2. **精度问题**: 金额使用 `BigDecimal`，保留 6 位小数
3. **幂等性**: 扣费接口需支持幂等，防止重复扣费
4. **审计日志**: 所有资金变动必须记录流水
5. **监控告警**: 实时监控扣费异常和大额充值
