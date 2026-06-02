package com.zhongway.sms.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 短信发送请求 DTO
 */
@Data
@Schema(description = "短信发送请求")
public class SmsSendRequest {

    @NotBlank(message = "手机号不能为空")
    @Schema(description = "接收手机号", example = "13800138000", required = true)
    private String phone;

    @NotBlank(message = "短信内容不能为空")
    @Size(max = 500, message = "短信内容不能超过 500 字")
    @Schema(description = "短信内容", example = "【签名】您的验证码是 123456", required = true)
    private String content;

    @Schema(description = "租户 ID (可选，默认从上下文获取)", example = "tenant_001")
    private String tenantId;

    @Schema(description = "业务类型", example = "VERIFY_CODE")
    private String bizType;

    @Schema(description = "外部订单号 (用于去重)", example = "order_202401010001")
    private String outOrderId;
}
