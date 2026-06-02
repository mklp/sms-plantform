package com.zhongway.sms.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 租户信息响应 DTO
 */
@Data
@Schema(description = "租户信息响应")
public class TenantResponse {

    @Schema(description = "租户 ID", example = "tenant_001")
    private String tenantId;

    @Schema(description = "租户名称", example = "某某企业")
    private String tenantName;

    @Schema(description = "联系人", example = "张三")
    private String contactPerson;

    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;

    @Schema(description = "每日配额", example = "10000")
    private Long dailyQuota;

    @Schema(description = "今日已用", example = "5000")
    private Long todayUsed;

    @Schema(description = "剩余配额", example = "5000")
    private Long remainingQuota;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "创建时间", example = "2024-01-01T08:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2024-01-01T09:00:00")
    private LocalDateTime updateTime;
}
