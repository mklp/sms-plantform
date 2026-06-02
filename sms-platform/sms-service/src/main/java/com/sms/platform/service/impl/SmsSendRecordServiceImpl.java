package com.sms.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sms.platform.entity.SmsSendRecordHot;
import com.sms.platform.mapper.SmsSendRecordHotMapper;
import com.sms.platform.service.SmsSendRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 短信发送记录服务实现 (冷热分离架构)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsSendRecordServiceImpl implements SmsSendRecordService {

    private final SmsSendRecordHotMapper hotMapper;
    private final JdbcTemplate jdbcTemplate;

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");
    private static final int HOT_TABLE_RETENTION_DAYS = 7; // 热表保留天数

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveToHotTable(SmsSendRecordHot record) {
        hotMapper.insert(record);
        log.debug("保存发送记录到热表：messageId={}", record.getMessageId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSaveToHotTable(List<SmsSendRecordHot> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        
        // 分批插入，每批 500 条
        final int batchSize = 500;
        for (int i = 0; i < records.size(); i += batchSize) {
            int end = Math.min(i + batchSize, records.size());
            List<SmsSendRecordHot> batch = records.subList(i, end);
            
            // MyBatis-Plus 批量插入
            for (SmsSendRecordHot record : batch) {
                hotMapper.insert(record);
            }
        }
        log.info("批量保存 {} 条发送记录到热表", records.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSendStatus(String messageId, String status, String errorCode, String errorMsg) {
        SmsSendRecordHot record = hotMapper.selectById(messageId);
        if (record == null) {
            log.warn("更新状态失败，记录不存在：messageId={}", messageId);
            return;
        }

        record.setSendStatus(status);
        record.setErrorCode(errorCode);
        record.setErrorMessage(errorMsg);
        
        if ("SENDING".equals(status)) {
            record.setSendTime(LocalDateTime.now());
        } else if ("SUCCESS".equals(status) || "FAILED".equals(status)) {
            record.setReportTime(LocalDateTime.now());
        }
        
        hotMapper.updateById(record);
        log.debug("更新发送状态：messageId={}, status={}", messageId, status);
    }

    @Override
    public SmsSendRecordHot getByMessageId(String messageId) {
        // 先查热表
        LambdaQueryWrapper<SmsSendRecordHot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SmsSendRecordHot::getMessageId, messageId);
        return hotMapper.selectOne(wrapper);
    }

    @Override
    public SmsSendRecordHot getByOutOrderId(String outOrderId) {
        // 先查热表
        LambdaQueryWrapper<SmsSendRecordHot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SmsSendRecordHot::getOutOrderId, outOrderId);
        return hotMapper.selectOne(wrapper);
    }

    @Override
    public List<SmsSendRecordHot> queryRecentRecords(Long tenantId, LocalDateTime startTime, 
                                                      LocalDateTime endTime, int page, int size) {
        Page<SmsSendRecordHot> mpPage = new Page<>(page, size);
        
        LambdaQueryWrapper<SmsSendRecordHot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SmsSendRecordHot::getTenantId, tenantId);
        
        if (startTime != null) {
            wrapper.ge(SmsSendRecordHot::getSubmitTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(SmsSendRecordHot::getSubmitTime, endTime);
        }
        
        wrapper.orderByDesc(SmsSendRecordHot::getSubmitTime);
        
        Page<SmsSendRecordHot> resultPage = hotMapper.selectPage(mpPage, wrapper);
        return resultPage.getRecords();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int archiveOldData(int daysAgo) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(daysAgo);
        
        // 1. 查询需要归档的数据
        LambdaQueryWrapper<SmsSendRecordHot> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.lt(SmsSendRecordHot::getSubmitTime, cutoffTime);
        List<SmsSendRecordHot> toArchive = hotMapper.selectList(queryWrapper);
        
        if (toArchive.isEmpty()) {
            log.info("没有需要归档的数据");
            return 0;
        }

        // 2. 按月份分组归档
        int archivedCount = 0;
        for (SmsSendRecordHot record : toArchive) {
            String tableName = getHistoryTableName(record.getSubmitTime());
            
            // 动态插入到对应月份的历史表
            String sql = String.format(
                "INSERT INTO %s (id, tenant_id, message_id, out_order_id, phone_number, content, signature, " +
                "full_content, template_id, template_params, biz_type, channel_id, protocol_type, " +
                "charge_count, unit_price, total_fee, send_status, submit_time, send_time, report_time, " +
                "report_content, delivered_status, error_code, error_message, retry_count, is_callback, " +
                "callback_time, callback_status, ext_data, create_time, update_time, archived_time) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?, ?, NOW())",
                tableName
            );
            
            try {
                jdbcTemplate.update(sql,
                    record.getId(),
                    record.getTenantId(),
                    record.getMessageId(),
                    record.getOutOrderId(),
                    record.getPhoneNumber(),
                    record.getContent(),
                    record.getSignature(),
                    record.getFullContent(),
                    record.getTemplateId(),
                    record.getTemplateParams() != null ? record.getTemplateParams().toString() : "{}",
                    record.getBizType(),
                    record.getChannelId(),
                    record.getProtocolType(),
                    record.getChargeCount(),
                    record.getUnitPrice(),
                    record.getTotalFee(),
                    record.getSendStatus(),
                    record.getSubmitTime(),
                    record.getSendTime(),
                    record.getReportTime(),
                    record.getReportContent(),
                    record.getDeliveredStatus(),
                    record.getErrorCode(),
                    record.getErrorMessage(),
                    record.getRetryCount(),
                    record.getIsCallback(),
                    record.getCallbackTime(),
                    record.getCallbackStatus(),
                    record.getExtData() != null ? record.getExtData().toString() : "{}",
                    record.getCreateTime(),
                    record.getUpdateTime()
                );
                archivedCount++;
            } catch (Exception e) {
                log.error("归档数据失败：tableName={}, messageId={}", tableName, record.getMessageId(), e);
            }
        }

        // 3. 从热表删除已归档数据
        if (archivedCount > 0) {
            LambdaQueryWrapper<SmsSendRecordHot> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.lt(SmsSendRecordHot::getSubmitTime, cutoffTime);
            int deletedCount = hotMapper.delete(deleteWrapper);
            log.info("归档完成：成功{}条，从热表删除{}条", archivedCount, deletedCount);
        }

        return archivedCount;
    }

    @Override
    public List<SmsSendRecordHot> queryHistoryRecords(Long tenantId, LocalDateTime startTime, 
                                                       LocalDateTime endTime, int page, int size) {
        // 根据时间范围确定查询哪些历史表
        List<String> tableNames = getHistoryTableNames(startTime, endTime);
        List<SmsSendRecordHot> allRecords = new ArrayList<>();
        
        for (String tableName : tableNames) {
            try {
                // 动态查询历史表
                String sql = String.format(
                    "SELECT * FROM %s WHERE tenant_id = ? AND submit_time >= ? AND submit_time <= ? " +
                    "ORDER BY submit_time DESC LIMIT ? OFFSET ?",
                    tableName
                );
                
                // 这里简化处理，实际应该使用 RowMapper 映射
                // 由于涉及动态表名和复杂映射，建议使用 MyBatis XML 或 JdbcTemplate+RowMapper
                log.debug("查询历史表：{}", tableName);
            } catch (Exception e) {
                log.warn("查询历史表失败：tableName={}", tableName, e);
            }
        }
        
        return allRecords;
    }

    /**
     * 获取历史表名 (格式：sms_send_record_hist_YYYYMM)
     */
    private String getHistoryTableName(LocalDateTime time) {
        String monthStr = time.format(MONTH_FORMATTER);
        return "sms_send_record_hist_" + monthStr;
    }

    /**
     * 获取时间范围内的所有历史表名
     */
    private List<String> getHistoryTableNames(LocalDateTime startTime, LocalDateTime endTime) {
        List<String> tableNames = new ArrayList<>();
        LocalDate current = startTime.toLocalDate().withDayOfMonth(1);
        LocalDate end = endTime.toLocalDate().withDayOfMonth(1);
        
        while (!current.isAfter(end)) {
            String tableName = "sms_send_record_hist_" + current.format(DateTimeFormatter.ofPattern("yyyyMM"));
            tableNames(tableName);
            current = current.plusMonths(1);
        }
        
        return tableNames;
    }
}
