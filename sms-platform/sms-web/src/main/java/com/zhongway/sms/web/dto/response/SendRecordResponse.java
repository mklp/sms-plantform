package com.zhongway.sms.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 发送记录响应 DTO
 */
@Data
@Schema(description = "发送记录响应")
public class SendRecordResponse {

    @Schema(description = "记录 ID", example = "1234567890")
    private Long id;

    @Schema(description = "租户 ID", example = "tenant_001")
    private String tenantId;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "短信内容", example = "【签名】您的验证码是 123456")
    private String content;

    @Schema(description = "外部订单号", example = "order_202401010001")
    private String outOrderId;

    @Schema(description = "通道 ID", example = "1")
    private Long channelId;

    @Schema(description = "通道名称", example = "移动网关 1")
    private String channelName;

    @Schema(description = "提交状态", example = "DELIVERED")
    private String submitStatus;

    @Schema(description = "运营商返回码", example = "0")
    private String deliverStatus;

    @Schema(description = "消息 ID (运营商返回)", example = "msg_abc123")
    private String msgId;

    @Schema(description = "业务类型", example = "VERIFY_CODE")
    private String bizType;

    @Schema(description = "短信条数", example = "1")
    private Integer smsCount;

    @Schema(description = "提交时间", example = "2024-01-01T10:00:00")
    private LocalDateTime submitTime;

    @Schema(description = "送达时间", example = "2024-01-01T10:00:05")
    private LocalDateTime deliverTime;

    @Schema(description = "失败原因", example = "")
    private String failReason;
}
