package com.example.sms.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 短信发送响应 DTO
 */
@Data
@Schema(description = "短信发送响应")
public class SmsSendResponse {

    @Schema(description = "发送记录 ID", example = "1234567890")
    private Long recordId;

    @Schema(description = "外部订单号", example = "order_202401010001")
    private String outOrderId;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "提交状态", example = "SUCCESS")
    private String submitStatus;

    @Schema(description = "通道 ID", example = "channel_001")
    private String channelId;

    @Schema(description = "提交时间", example = "2024-01-01T10:00:00")
    private LocalDateTime submitTime;

    @Schema(description = "消息 ID (运营商返回)", example = "msg_abc123")
    private String msgId;
}
