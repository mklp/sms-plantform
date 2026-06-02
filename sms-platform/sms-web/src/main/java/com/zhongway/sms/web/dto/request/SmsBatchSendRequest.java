package com.zhongway.sms.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

/**
 * 批量短信发送请求 DTO
 */
@Data
@Schema(description = "批量短信发送请求")
public class SmsBatchSendRequest {

    @NotEmpty(message = "手机号列表不能为空")
    @Size(max = 100, message = "单次批量发送最多 100 个号码")
    @Schema(description = "接收手机号列表", required = true)
    private List<String> phones;

    @NotEmpty(message = "短信内容不能为空")
    @Size(max = 500, message = "短信内容不能超过 500 字")
    @Schema(description = "短信内容", example = "【签名】您的验证码是 123456", required = true)
    private String content;

    @Schema(description = "租户 ID (可选，默认从上下文获取)", example = "tenant_001")
    private String tenantId;

    @Schema(description = "业务类型", example = "NOTICE")
    private String bizType;

    @Schema(description = "是否异步发送 (默认 true)", example = "true")
    private Boolean async = true;
}
