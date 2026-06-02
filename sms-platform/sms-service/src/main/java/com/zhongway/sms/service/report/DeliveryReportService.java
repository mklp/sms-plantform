package com.zhongway.sms.service.report;

import com.zhongway.sms.core.model.DeliveryReport;
import com.zhongway.sms.dao.entity.SmsSendRecord;
import com.zhongway.sms.dao.mapper.SmsSendRecordMapper;
import com.zhongway.sms.common.enums.SmsReportStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 状态报告处理服务
 */
@Service
public class DeliveryReportService {
    private static final Logger logger = LoggerFactory.getLogger(DeliveryReportService.class);

    private final SmsSendRecordMapper sendRecordMapper;

    public DeliveryReportService(SmsSendRecordMapper sendRecordMapper) {
        this.sendRecordMapper = sendRecordMapper;
    }

    /**
     * 处理状态报告
     * 
     * @param report 状态报告
     */
    @Transactional(rollbackFor = Exception.class)
    public void processReport(DeliveryReport report) {
        logger.info("处理状态报告，msgId={}, phone={}, statusCode={}", 
            report.getMsgId(), report.getPhoneNumber(), report.getStatusCode());

        // 根据消息 ID 查找发送记录
        SmsSendRecord record = sendRecordMapper.selectByMsgId(report.getMsgId());
        if (record == null) {
            logger.warn("未找到对应的发送记录，msgId={}", report.getMsgId());
            return;
        }

        // 更新发送记录状态
        updateSendRecord(record, report);

        // 如果租户配置了回调 URL，触发回调
        // TODO: 调用租户回调
    }

    /**
     * 批量处理状态报告
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchProcessReports(java.util.List<DeliveryReport> reports) {
        for (DeliveryReport report : reports) {
            try {
                processReport(report);
            } catch (Exception e) {
                logger.error("处理状态报告失败，msgId={}", report.getMsgId(), e);
            }
        }
    }

    /**
     * 更新发送记录
     */
    private void updateSendRecord(SmsSendRecord record, DeliveryReport report) {
        // 设置状态报告状态
        String reportStatus = report.getSuccess() ? 
            SmsReportStatus.SUCCESS.getCode() : SmsReportStatus.FAILED.getCode();
        
        record.setReportStatus(reportStatus);
        record.setReportTime(java.time.LocalDateTime.now());
        record.setNativeMsgId(report.getNativeMsgId());
        record.setStatusCode(report.getStatusCode());
        
        // 更新数据库
        sendRecordMapper.updateById(record);
        
        logger.info("发送记录已更新，msgId={}, reportStatus={}", 
            record.getMsgId(), reportStatus);
    }

    /**
     * 解析运营商状态码
     * 
     * @param statusCode 运营商状态码
     * @return 状态描述
     */
    public String parseStatusCode(String statusCode) {
        if (statusCode == null) {
            return "未知状态";
        }
        
        // CMPP 协议状态码解析
        switch (statusCode) {
            case "DELIVRD":
                return "送达";
            case "EXPIRED":
                return "过期";
            case "DELETED":
                return "删除";
            case "UNDELIV":
                return "无法送达";
            case "ACCEPTD":
                return "已接受";
            case "UNKNOWN":
                return "未知";
            case "REJECTD":
                return "拒绝";
            default:
                return "未知状态：" + statusCode;
        }
    }
}
