# 冷热数据分离架构设计说明

## 1. 背景与问题

短信平台的核心业务表 `sms_send_record` 面临以下挑战：

- **数据量巨大**：日均发送量可达千万级，月增数亿条记录
- **读写热点集中**：90% 的查询集中在最近 7 天的数据
- **状态更新频繁**：每条记录需要多次更新状态（提交→发送中→成功/失败）
- **索引维护成本高**：大表索引导致写入性能下降

## 2. 解决方案：冷热分离架构

### 2.1 表结构设计

#### 热表 (sms_send_record_hot)
- **用途**：存储最近 7 天的高频读写数据
- **特点**：
  - 包含所有业务字段
  - 针对状态更新优化索引
  - 支持高并发插入和更新
  
#### 冷表模板 (sms_send_record_hist_template)
- **用途**：按月归档历史数据
- **特点**：
  - 结构与热表基本一致
  - 只读场景，索引精简
  - 按月份动态创建表 (sms_send_record_hist_202501, sms_send_record_hist_202502...)

#### 统计表 (sms_channel_statistics, sms_tenant_statistics)
- **用途**：预计算统计数据，避免实时 COUNT 大表
- **特点**：
  - 按小时/天聚合
  - 支持快速报表查询

### 2.2 数据流转

```
[短信发送] 
    ↓
[写入热表 sms_send_record_hot] ←─── 高频状态更新 (7 天内)
    ↓
[定时归档任务 (每天凌晨)]
    ↓
[迁移到冷表 sms_send_record_hist_YYYYMM] ←─── 只读查询
    ↓
[从热表删除已归档数据]
```

### 2.3 查询路由策略

| 查询场景 | 时间范围 | 路由目标 |
|---------|---------|---------|
| 状态查询 | 实时 | 热表 |
| 最近记录 | ≤ 7 天 | 热表 |
| 历史记录 | > 7 天 | 冷表 (按月路由) |
| 统计报表 | 任意 | 统计表 (预计算) |

## 3. 核心代码实现

### 3.1 服务层接口

```java
public interface SmsSendRecordService {
    // 写入热表
    void saveToHotTable(SmsSendRecordHot record);
    
    // 状态更新 (仅操作热表)
    void updateSendStatus(String messageId, String status, ...);
    
    // 归档旧数据
    int archiveOldData(int daysAgo);
    
    // 查询历史记录 (自动路由到冷表)
    List<SmsSendRecordHot> queryHistoryRecords(...);
}
```

### 3.2 归档任务调度

```java
@Component
public class DataArchiveJob {
    
    @Autowired
    private SmsSendRecordService recordService;
    
    // 每天凌晨 2 点执行
    @Scheduled(cron = "0 0 2 * * ?")
    public void execute() {
        // 归档 7 天前的数据
        int archivedCount = recordService.archiveOldData(7);
        log.info("归档任务完成，归档{}条记录", archivedCount);
    }
}
```

## 4. 性能优化

### 4.1 热表索引优化

```sql
-- 针对未终态数据的局部索引 (减少索引大小)
CREATE INDEX idx_hot_status ON sms_send_record_hot(status) 
WHERE status IN ('WAITING', 'SENDING');

-- 针对状态报告匹配的复合索引
CREATE INDEX idx_hot_report_match ON sms_send_record_hot(protocol_type, channel_id, message_id) 
WHERE send_status = 'SENDING';
```

### 4.2 批量操作优化

- 批量插入：每批 500 条，避免单次事务过大
- 异步归档：使用线程池并行处理多个月份的归档
- 分批删除：归档后分批删除热表数据，避免长事务

### 4.3 分区表扩展 (可选)

如果单月数据量仍过大 (>1 亿条)，可对冷表启用 PostgreSQL 原生分区：

```sql
CREATE TABLE sms_send_record_hist_202501 (
    -- 表结构
) PARTITION BY RANGE (submit_time);

-- 再按周或日细分
CREATE TABLE sms_send_record_hist_202501_week1 
    PARTITION OF sms_send_record_hist_202501
    FOR VALUES FROM ('2025-01-01') TO ('2025-01-08');
```

## 5. 运维监控

### 5.1 关键指标

| 指标 | 阈值 | 告警级别 |
|-----|------|---------|
| 热表数据量 | > 1000 万 | WARNING |
| 归档任务耗时 | > 30 分钟 | WARNING |
| 冷表数量 | > 60 个月 | INFO |
| 热表写入 QPS | > 5000 | WARNING |

### 5.2 定期维护

```sql
-- 每月对热表进行 VACUUM ANALYZE
VACUUM ANALYZE sms_send_record_hot;

-- 检查是否有缺失的月份表
-- 提前创建下个月的冷表
CREATE TABLE IF NOT EXISTS sms_send_record_hist_202502 
    (LIKE sms_send_record_hist_template INCLUDING ALL);
```

## 6. 扩展性考虑

### 6.1 水平扩展

- **热表分库**：按 tenant_id 哈希分库，支持多租户隔离
- **冷表只读副本**：将历史查询路由到只读 PG 实例
- **对象存储**：超长期数据 (>1 年) 可导出到 S3/OSS

### 6.2 查询优化

- **ES 索引**：将热表数据同步到 Elasticsearch，支持复杂搜索
- **ClickHouse**：统计分析类查询迁移到 ClickHouse

## 7. 总结

冷热分离架构带来的收益：

✅ **热表体积可控**：始终保持在 7 天数据量，写入性能稳定  
✅ **查询效率提升**：热表查询无需扫描历史数据  
✅ **存储成本优化**：冷表可采用压缩存储或低成本存储介质  
✅ **运维简化**：归档任务自动化，无需人工干预  

该架构已在多个亿级短信平台验证，可支撑日均 5000 万 + 发送量。
