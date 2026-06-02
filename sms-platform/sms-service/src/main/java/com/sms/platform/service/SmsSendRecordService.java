package com.sms.platform.service;

import com.sms.platform.entity.SmsSendRecordHot;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 短信发送记录服务接口 (冷热分离架构)
 */
public interface SmsSendRecordService {

    /**
     * 保存发送记录到热表
     */
    void saveToHotTable(SmsSendRecordHot record);

    /**
     * 批量保存发送记录到热表
     */
    void batchSaveToHotTable(List<SmsSendRecordHot> records);

    /**
     * 更新发送状态 (仅操作热表)
     */
    void updateSendStatus(String messageId, String status, String errorCode, String errorMsg);

    /**
     * 根据消息 ID 查询记录 (优先查热表)
     */
    SmsSendRecordHot getByMessageId(String messageId);

    /**
     * 根据外部订单号查询记录 (优先查热表)
     */
    SmsSendRecordHot getByOutOrderId(String outOrderId);

    /**
     * 查询租户最近发送记录 (仅查热表，支持分页)
     */
    List<SmsSendRecordHot> queryRecentRecords(Long tenantId, LocalDateTime startTime, LocalDateTime endTime, int page, int size);

    /**
     * 归档热表数据到冷表
     * 定时任务调用，将 N 天前的数据迁移到历史表
     */
    int archiveOldData(int daysAgo);

    /**
     * 查询历史记录 (自动路由到对应月份的历史表)
     */
    List<SmsSendRecordHot> queryHistoryRecords(Long tenantId, LocalDateTime startTime, LocalDateTime endTime, int page, int size);
}
