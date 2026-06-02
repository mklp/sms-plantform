package com.zhongway.sms.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 发送记录查询请求 DTO
 */
@Data
@Schema(description = "发送记录查询请求")
public class SendRecordQueryRequest {

    @Schema(description = "租户 ID (可选)", example = "tenant_001")
    private String tenantId;

    @Schema(description = "手机号 (可选)", example = "13800138000")
    private String phone;

    @Schema(description = "外部订单号 (可选)", example = "order_202401010001")
    private String outOrderId;

    @Schema(description = "通道 ID (可选)", example = "1")
    private Long channelId;

    @Schema(description = "发送状态 (可选)", example = "DELIVERED")
    private String submitStatus;

    @Schema(description = "开始时间", example = "2024-01-01T00:00:00")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2024-01-01T23:59:59")
    private LocalDateTime endTime;

    @Schema(description = "是否查询热表 (默认 true，查询最近 7 天数据)", example = "true")
    private Boolean queryHot = true;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "20")
    private Integer pageSize = 20;
}
