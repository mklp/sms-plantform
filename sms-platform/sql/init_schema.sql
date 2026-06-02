-- =====================================================
-- 企业级高可用短信平台 - PostgreSQL 17 数据库初始化脚本
-- =====================================================
-- 说明：
-- 1. 采用 tenant_id 逻辑隔离多租户数据
-- 2. 核心配置表使用 JSONB 字段存储灵活配置
-- 3. 发送记录表支持分区表扩展（按时间）
-- 4. 所有表包含 deleted 字段支持逻辑删除
-- =====================================================

-- 启用扩展 (PostgreSQL 特性)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm"; -- 用于模糊查询优化

-- =====================================================
-- 1. 系统字典配置表 (动态参数配置)
-- =====================================================
DROP TABLE IF EXISTS sys_dict_config;
CREATE TABLE sys_dict_config (
    id BIGSERIAL PRIMARY KEY,
    dict_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    dict_value TEXT NOT NULL COMMENT '配置值',
    dict_type VARCHAR(50) NOT NULL DEFAULT 'SYSTEM' COMMENT '配置类型：SYSTEM-系统，BUSINESS-业务',
    description VARCHAR(500) COMMENT '配置描述',
    is_encrypted BOOLEAN DEFAULT FALSE COMMENT '是否加密存储',
    is_editable BOOLEAN DEFAULT TRUE COMMENT '是否可编辑',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);
COMMENT ON TABLE sys_dict_config IS '系统字典配置表 - 动态参数配置';

-- 初始化系统配置
INSERT INTO sys_dict_config (dict_key, dict_value, dict_type, description) VALUES
('sms.default.retry.count', '3', 'SYSTEM', '默认重试次数'),
('sms.default.timeout.ms', '5000', 'SYSTEM', '默认超时时间 (毫秒)'),
('sms.batch.max.size', '100', 'BUSINESS', '批量发送最大条数'),
('sms.frequency.limit.per.minute', '60', 'SYSTEM', '单号码每分钟频次限制'),
('sms.frequency.limit.per.hour', '300', 'SYSTEM', '单号码每小时频次限制'),
('sms.frequency.limit.per.day', '1000', 'SYSTEM', '单号码每天频次限制'),
('channel.auto.switch.enabled', 'true', 'SYSTEM', '通道自动切换开关'),
('channel.failure.threshold', '5', 'SYSTEM', '通道故障阈值 (连续失败次数)'),
('record.retention.days', '180', 'SYSTEM', '发送记录保留天数');

-- =====================================================
-- 2. 企业租户表 (多租户核心表)
-- =====================================================
DROP TABLE IF EXISTS sms_tenant;
CREATE TABLE sms_tenant (
    id BIGINT PRIMARY KEY,
    tenant_code VARCHAR(50) NOT NULL UNIQUE COMMENT '租户编码',
    tenant_name VARCHAR(200) NOT NULL COMMENT '租户名称',
    api_key VARCHAR(100) NOT NULL UNIQUE COMMENT 'API Key',
    api_secret VARCHAR(200) NOT NULL COMMENT 'API Secret (加密存储)',
    contact_person VARCHAR(100) COMMENT '联系人',
    contact_phone VARCHAR(20) COMMENT '联系电话',
    contact_email VARCHAR(100) COMMENT '联系邮箱',
    daily_limit INTEGER DEFAULT 10000 COMMENT '每日发送限额',
    monthly_limit INTEGER DEFAULT 300000 COMMENT '每月发送限额',
    today_sent_count INTEGER DEFAULT 0 COMMENT '当日已发送数量',
    month_sent_count INTEGER DEFAULT 0 COMMENT '当月已发送数量',
    default_signature VARCHAR(50) COMMENT '默认签名',
    callback_url VARCHAR(500) COMMENT '回调 URL',
    enable_ip_whitelist BOOLEAN DEFAULT FALSE COMMENT '是否启用 IP 白名单',
    ip_whitelist TEXT COMMENT 'IP 白名单列表 (JSON 数组)',
    status VARCHAR(20) NOT NULL DEFAULT 'AUDITING' COMMENT '状态：ACTIVE, DISABLED, EXPIRED, AUDITING',
    remark TEXT COMMENT '备注',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);
COMMENT ON TABLE sms_tenant IS '企业租户表 - 多租户数据隔离核心表';

-- 创建索引
CREATE INDEX idx_tenant_code ON sms_tenant(tenant_code);
CREATE INDEX idx_api_key ON sms_tenant(api_key);
CREATE INDEX idx_tenant_status ON sms_tenant(status);

-- =====================================================
-- 3. 短信通道配置表 (支持 JSONB 灵活配置)
-- =====================================================
DROP TABLE IF EXISTS sms_channel;
CREATE TABLE sms_channel (
    id BIGINT PRIMARY KEY,
    channel_code VARCHAR(50) NOT NULL UNIQUE COMMENT '通道编码',
    channel_name VARCHAR(200) NOT NULL COMMENT '通道名称',
    protocol_type VARCHAR(20) NOT NULL COMMENT '协议类型：CMPP, SMGP, SGIP, SMPP, HTTP',
    operator VARCHAR(20) NOT NULL COMMENT '运营商：MOBILE, UNICOM, TELECOM, ALL',
    gateway_host VARCHAR(200) NOT NULL COMMENT '网关主机地址',
    gateway_port INTEGER NOT NULL COMMENT '网关端口',
    enterprise_id VARCHAR(100) NOT NULL COMMENT '企业 ID/账号',
    shared_secret VARCHAR(500) NOT NULL COMMENT '共享密钥 (加密存储)',
    service_type VARCHAR(50) COMMENT '服务类型',
    access_type SMALLINT DEFAULT 0 COMMENT '接入类型：0-普通，1-直连',
    
    -- 连接与流控配置 (也可存入 config_json)
    max_connections INTEGER DEFAULT 10 COMMENT '最大连接数',
    qps_limit INTEGER DEFAULT 100 COMMENT '每秒发送限制 (QPS)',
    daily_limit INTEGER DEFAULT 100000 COMMENT '每日发送限额',
    today_sent_count INTEGER DEFAULT 0 COMMENT '当前已发送数量',
    
    -- 负载均衡与路由
    weight INTEGER DEFAULT 100 COMMENT '权重 (用于负载均衡)',
    priority INTEGER DEFAULT 1 COMMENT '优先级 (数字越小优先级越高)',
    route_strategy VARCHAR(20) DEFAULT 'ROUND_ROBIN' COMMENT '路由策略：ROUND_ROBIN, WEIGHTED, LEAST_CONNECTIONS',
    
    -- 高级配置 (JSONB 格式，实现灵活性)
    config_json JSONB DEFAULT '{}'::jsonb COMMENT '扩展配置 (JSON 格式)',
    
    -- 状态与健康检查
    status VARCHAR(20) NOT NULL DEFAULT 'DISABLED' COMMENT '状态：ACTIVE, DISABLED',
    heartbeat_interval INTEGER DEFAULT 30 COMMENT '心跳间隔 (秒)',
    timeout_ms INTEGER DEFAULT 5000 COMMENT '超时时间 (毫秒)',
    retry_count INTEGER DEFAULT 3 COMMENT '重试次数',
    consecutive_failures INTEGER DEFAULT 0 COMMENT '连续失败次数',
    last_failure_time TIMESTAMP COMMENT '最后失败时间',
    last_heartbeat_time TIMESTAMP COMMENT '最后心跳时间',
    
    remark TEXT COMMENT '备注',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);
COMMENT ON TABLE sms_channel IS '短信通道配置表 - 支持 JSONB 灵活配置';

-- 创建索引
CREATE INDEX idx_channel_code ON sms_channel(channel_code);
CREATE INDEX idx_channel_protocol ON sms_channel(protocol_type);
CREATE INDEX idx_channel_operator ON sms_channel(operator);
CREATE INDEX idx_channel_status ON sms_channel(status);
CREATE INDEX idx_channel_priority ON sms_channel(priority);
-- GIN 索引用于 JSONB 查询
CREATE INDEX idx_channel_config_json ON sms_channel USING GIN(config_json);

-- =====================================================
-- 4. 短信模板表
-- =====================================================
DROP TABLE IF EXISTS sms_template;
CREATE TABLE sms_template (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '租户 ID',
    template_code VARCHAR(50) NOT NULL COMMENT '模板编码',
    template_name VARCHAR(200) NOT NULL COMMENT '模板名称',
    template_content TEXT NOT NULL COMMENT '模板内容 (含变量占位符)',
    variable_names TEXT[] COMMENT '变量名数组',
    audit_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '审核状态：PENDING, APPROVED, REJECTED',
    audit_remark VARCHAR(500) COMMENT '审核备注',
    audit_time TIMESTAMP COMMENT '审核时间',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE, DISABLED',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);
COMMENT ON TABLE sms_template IS '短信模板表';

-- 创建索引
CREATE INDEX idx_template_tenant ON sms_template(tenant_id);
CREATE INDEX idx_template_code ON sms_template(template_code);
CREATE INDEX idx_template_audit_status ON sms_template(audit_status);

-- =====================================================
-- 5. 短信签名表
-- =====================================================
DROP TABLE IF EXISTS sms_signature;
CREATE TABLE sms_signature (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '租户 ID',
    signature_content VARCHAR(100) NOT NULL COMMENT '签名内容',
    signature_type VARCHAR(20) DEFAULT 'NORMAL' COMMENT '签名类型：NORMAL-普通，INTERNATIONAL-国际',
    proof_materials TEXT COMMENT '证明材料 (JSON 数组，存储文件路径)',
    audit_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '审核状态：PENDING, APPROVED, REJECTED',
    audit_remark VARCHAR(500) COMMENT '审核备注',
    audit_time TIMESTAMP COMMENT '审核时间',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE, DISABLED',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);
COMMENT ON TABLE sms_signature IS '短信签名表';

-- 创建索引
CREATE INDEX idx_signature_tenant ON sms_signature(tenant_id);
CREATE INDEX idx_signature_status ON sms_signature(status);

-- =====================================================
-- 6. 短信发送记录表 (核心业务表)
-- =====================================================
DROP TABLE IF EXISTS sms_send_record;
CREATE TABLE sms_send_record (
    id BIGINT PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '租户 ID',
    message_id VARCHAR(64) NOT NULL UNIQUE COMMENT '消息 ID (全局唯一)',
    out_order_id VARCHAR(100) COMMENT '外部订单号 (用于幂等控制)',
    phone_number VARCHAR(20) NOT NULL COMMENT '接收手机号',
    content TEXT NOT NULL COMMENT '短信内容',
    signature VARCHAR(100) COMMENT '短信签名',
    full_content TEXT COMMENT '完整短信 (含签名)',
    template_id BIGINT COMMENT '模板 ID',
    template_params JSONB DEFAULT '{}'::jsonb COMMENT '模板参数 (JSON 格式)',
    biz_type VARCHAR(50) COMMENT '业务类型',
    channel_id BIGINT COMMENT '通道 ID',
    protocol_type VARCHAR(20) COMMENT '协议类型',
    
    -- 计费信息
    charge_count INTEGER DEFAULT 1 COMMENT '计费条数',
    unit_price DECIMAL(10, 6) DEFAULT 0.0 COMMENT '单价',
    total_fee DECIMAL(10, 6) DEFAULT 0.0 COMMENT '总费用',
    
    -- 状态追踪
    send_status VARCHAR(20) NOT NULL DEFAULT 'WAITING' COMMENT '发送状态',
    submit_time TIMESTAMP COMMENT '提交时间',
    send_time TIMESTAMP COMMENT '发送时间',
    report_time TIMESTAMP COMMENT '状态报告时间',
    report_content VARCHAR(500) COMMENT '状态报告内容',
    delivered_status VARCHAR(20) COMMENT '送达状态：DELIVERED, FAILED, UNKNOWN',
    
    -- 错误处理
    error_code VARCHAR(50) COMMENT '错误码',
    error_message TEXT COMMENT '错误信息',
    retry_count INTEGER DEFAULT 0 COMMENT '重试次数',
    max_retry_count INTEGER DEFAULT 3 COMMENT '最大重试次数',
    
    -- 回调处理
    is_callback BOOLEAN DEFAULT FALSE COMMENT '是否已回调',
    callback_time TIMESTAMP COMMENT '回调时间',
    callback_status VARCHAR(20) COMMENT '回调状态',
    
    -- 扩展字段
    ext_data JSONB DEFAULT '{}'::jsonb COMMENT '扩展数据 (JSON 格式)',
    
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
) PARTITION BY RANGE (create_time);
COMMENT ON TABLE sms_send_record IS '短信发送记录表 - 按时间分区';

-- 创建索引
CREATE INDEX idx_send_record_tenant ON sms_send_record(tenant_id);
CREATE INDEX idx_send_record_message_id ON sms_send_record(message_id);
CREATE INDEX idx_send_record_out_order_id ON sms_send_record(out_order_id);
CREATE INDEX idx_send_record_phone ON sms_send_record(phone_number);
CREATE INDEX idx_send_record_status ON sms_send_record(send_status);
CREATE INDEX idx_send_record_create_time ON sms_send_record(create_time);
CREATE INDEX idx_send_record_channel ON sms_send_record(channel_id);
-- GIN 索引用于 JSONB 查询
CREATE INDEX idx_send_record_template_params ON sms_send_record USING GIN(template_params);
CREATE INDEX idx_send_record_ext_data ON sms_send_record USING GIN(ext_data);

-- 创建分区表示例 (按月分区，实际使用时根据数据量调整)
-- 2025 年分区示例
CREATE TABLE sms_send_record_202501 PARTITION OF sms_send_record
    FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');
CREATE TABLE sms_send_record_202502 PARTITION OF sms_send_record
    FOR VALUES FROM ('2025-02-01') TO ('2025-03-01');
CREATE TABLE sms_send_record_202503 PARTITION OF sms_send_record
    FOR VALUES FROM ('2025-03-01') TO ('2025-04-01');
CREATE TABLE sms_send_record_202504 PARTITION OF sms_send_record
    FOR VALUES FROM ('2025-04-01') TO ('2025-05-01');
CREATE TABLE sms_send_record_202505 PARTITION OF sms_send_record
    FOR VALUES FROM ('2025-05-01') TO ('2025-06-01');
CREATE TABLE sms_send_record_202506 PARTITION OF sms_send_record
    FOR VALUES FROM ('2025-06-01') TO ('2025-07-01');

-- =====================================================
-- 7. 通道使用统计表 (用于分析与计费)
-- =====================================================
DROP TABLE IF EXISTS sms_channel_statistics;
CREATE TABLE sms_channel_statistics (
    id BIGSERIAL PRIMARY KEY,
    channel_id BIGINT NOT NULL COMMENT '通道 ID',
    stat_date DATE NOT NULL COMMENT '统计日期',
    stat_hour INTEGER COMMENT '统计小时 (0-23), NULL 表示全天汇总',
    total_count INTEGER DEFAULT 0 COMMENT '总发送数',
    success_count INTEGER DEFAULT 0 COMMENT '成功数',
    failed_count INTEGER DEFAULT 0 COMMENT '失败数',
    total_charge_count INTEGER DEFAULT 0 COMMENT '总计费条数',
    total_fee DECIMAL(10, 6) DEFAULT 0.0 COMMENT '总费用',
    avg_response_time_ms INTEGER DEFAULT 0 COMMENT '平均响应时间 (毫秒)',
    success_rate DECIMAL(5, 2) DEFAULT 0.0 COMMENT '成功率 (%)',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(channel_id, stat_date, stat_hour)
);
COMMENT ON TABLE sms_channel_statistics IS '通道使用统计表';

-- 创建索引
CREATE INDEX idx_channel_stat_channel_date ON sms_channel_statistics(channel_id, stat_date);
CREATE INDEX idx_channel_stat_date ON sms_channel_statistics(stat_date);

-- =====================================================
-- 8. 租户使用统计表
-- =====================================================
DROP TABLE IF EXISTS sms_tenant_statistics;
CREATE TABLE sms_tenant_statistics (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '租户 ID',
    stat_date DATE NOT NULL COMMENT '统计日期',
    stat_hour INTEGER COMMENT '统计小时 (0-23), NULL 表示全天汇总',
    total_count INTEGER DEFAULT 0 COMMENT '总发送数',
    success_count INTEGER DEFAULT 0 COMMENT '成功数',
    failed_count INTEGER DEFAULT 0 COMMENT '失败数',
    total_charge_count INTEGER DEFAULT 0 COMMENT '总计费条数',
    total_fee DECIMAL(10, 6) DEFAULT 0.0 COMMENT '总费用',
    success_rate DECIMAL(5, 2) DEFAULT 0.0 COMMENT '成功率 (%)',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, stat_date, stat_hour)
);
COMMENT ON TABLE sms_tenant_statistics IS '租户使用统计表';

-- 创建索引
CREATE INDEX idx_tenant_stat_tenant_date ON sms_tenant_statistics(tenant_id, stat_date);
CREATE INDEX idx_tenant_stat_date ON sms_tenant_statistics(stat_date);

-- =====================================================
-- 9. 黑名单表 (全局 + 租户级)
-- =====================================================
DROP TABLE IF EXISTS sms_blacklist;
CREATE TABLE sms_blacklist (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT COMMENT '租户 ID, NULL 表示全局黑名单',
    phone_number VARCHAR(20) NOT NULL COMMENT '手机号码',
    black_type VARCHAR(20) DEFAULT 'MANUAL' COMMENT '黑名单类型：MANUAL-手动，AUTO-自动，COMPLAINT-投诉',
    reason VARCHAR(500) COMMENT '加入原因',
    expire_time TIMESTAMP COMMENT '过期时间，NULL 表示永久',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE, EXPIRED, REMOVED',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, phone_number)
);
COMMENT ON TABLE sms_blacklist IS '黑名单表';

-- 创建索引
CREATE INDEX idx_blacklist_tenant_phone ON sms_blacklist(tenant_id, phone_number);
CREATE INDEX idx_blacklist_phone ON sms_blacklist(phone_number);
CREATE INDEX idx_blacklist_status ON sms_blacklist(status);

-- =====================================================
-- 10. 操作日志表
-- =====================================================
DROP TABLE IF EXISTS sys_operation_log;
CREATE TABLE sys_operation_log (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT COMMENT '租户 ID',
    operator VARCHAR(100) COMMENT '操作人',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型',
    operation_module VARCHAR(50) NOT NULL COMMENT '操作模块',
    request_method VARCHAR(10) COMMENT '请求方法',
    request_url VARCHAR(500) COMMENT '请求 URL',
    request_params TEXT COMMENT '请求参数',
    response_status INTEGER COMMENT '响应状态码',
    response_time_ms INTEGER COMMENT '响应时间 (毫秒)',
    ip_address VARCHAR(50) COMMENT 'IP 地址',
    user_agent TEXT COMMENT 'User-Agent',
    error_message TEXT COMMENT '错误信息',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
COMMENT ON TABLE sys_operation_log IS '系统操作日志表';

-- 创建索引
CREATE INDEX idx_oplog_tenant_time ON sys_operation_log(tenant_id, create_time);
CREATE INDEX idx_oplog_type ON sys_operation_log(operation_type);
CREATE INDEX idx_oplog_create_time ON sys_operation_log(create_time);

-- =====================================================
-- 函数与触发器：自动更新 update_time
-- =====================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 为各表添加自动更新时间触发器
CREATE TRIGGER update_sms_tenant_updated_at BEFORE UPDATE ON sms_tenant
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_sms_channel_updated_at BEFORE UPDATE ON sms_channel
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_sms_template_updated_at BEFORE UPDATE ON sms_template
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_sms_signature_updated_at BEFORE UPDATE ON sms_signature
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_sms_send_record_updated_at BEFORE UPDATE ON sms_send_record
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- 初始化测试数据
-- =====================================================
-- 插入一个测试租户
INSERT INTO sms_tenant (id, tenant_code, tenant_name, api_key, api_secret, status, daily_limit, monthly_limit)
VALUES (1000000000000000001, 'DEMO_TENANT', '演示企业', 'demo_api_key_12345', 'encrypted_secret_abcxyz', 'ACTIVE', 10000, 300000);

-- 插入一个测试通道 (CMPP 协议)
INSERT INTO sms_channel (id, channel_code, channel_name, protocol_type, operator, gateway_host, gateway_port, 
                         enterprise_id, shared_secret, status, weight, priority)
VALUES (2000000000000000001, 'CMPP_MOBILE_01', '移动 CMPP 通道 01', 'CMPP', 'MOBILE', 
        '192.168.1.100', 7890, '901234', 'encrypted_shared_secret_xyz', 'ACTIVE', 100, 1);

-- =====================================================
-- 视图：租户今日发送统计
-- =====================================================
CREATE OR REPLACE VIEW v_tenant_today_stats AS
SELECT 
    t.id AS tenant_id,
    t.tenant_code,
    t.tenant_name,
    t.daily_limit,
    t.today_sent_count,
    COALESCE(s.total_count, 0) AS actual_today_count,
    t.daily_limit - COALESCE(s.total_count, 0) AS remaining_quota
FROM sms_tenant t
LEFT JOIN (
    SELECT tenant_id, COUNT(*) AS total_count
    FROM sms_send_record
    WHERE create_time >= CURRENT_DATE
      AND deleted = 0
    GROUP BY tenant_id
) s ON t.id = s.tenant_id
WHERE t.deleted = 0;

-- =====================================================
-- 视图：通道健康状态
-- =====================================================
CREATE OR REPLACE VIEW v_channel_health AS
SELECT 
    id,
    channel_code,
    channel_name,
    protocol_type,
    operator,
    status,
    consecutive_failures,
    last_heartbeat_time,
    CASE 
        WHEN status = 'DISABLED' THEN '禁用'
        WHEN consecutive_failures >= 5 THEN '异常'
        WHEN last_heartbeat_time < NOW() - INTERVAL '2 minutes' THEN '失联'
        ELSE '健康'
    END AS health_status
FROM sms_channel
WHERE deleted = 0;

-- =====================================================
-- 授权 (根据需要调整)
-- =====================================================
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO sms_user;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO sms_user;

-- =====================================================
-- 脚本结束
-- =====================================================
