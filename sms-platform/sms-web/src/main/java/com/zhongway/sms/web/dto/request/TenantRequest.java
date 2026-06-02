package com.zhongway.sms.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 租户管理请求 DTO
 */
@Data
@Schema(description = "租户管理请求")
public class TenantRequest {

    @Schema(description = "租户 ID (更新时必填)", example = "tenant_001")
    private String tenantId;

    @NotBlank(message = "租户名称不能为空")
    @Schema(description = "租户名称", example = "某某企业", required = true)
    private String tenantName;

    @Schema(description = "联系人", example = "张三")
    private String contactPerson;

    @Schema(description = "联系电话", example = "13800138000")
    private String contactPhone;

    @Schema(description = "每日发送配额", example = "10000")
    private Long dailyQuota;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled = true;

    @Schema(description = "备注", example = "VIP 客户")
    private String remark;
}
