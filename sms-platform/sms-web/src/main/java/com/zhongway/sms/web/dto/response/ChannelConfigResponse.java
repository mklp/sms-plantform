package com.zhongway.sms.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 通道配置响应 DTO
 */
@Data
@Schema(description = "通道配置响应")
public class ChannelConfigResponse {

    @Schema(description = "通道 ID", example = "1")
    private Long id;

    @Schema(description = "通道名称", example = "移动网关 1")
    private String channelName;

    @Schema(description = "运营商类型", example = "CMCC")
    private String operatorType;

    @Schema(description = "优先级", example = "1")
    private Integer priority;

    @Schema(description = "权重", example = "100")
    private Integer weight;

    @Schema(description = "每日发送上限", example = "100000")
    private Long dailyLimit;

    @Schema(description = "当前已发送数量", example = "50000")
    private Long currentCount;

    @Schema(description = "QPS 限制", example = "500")
    private Integer qpsLimit;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "连接状态", example = "CONNECTED")
    private String status;

    @Schema(description = "最后心跳时间", example = "2024-01-01T10:00:00")
    private LocalDateTime lastHeartbeat;

    @Schema(description = "创建时间", example = "2024-01-01T08:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2024-01-01T09:00:00")
    private LocalDateTime updateTime;
}
