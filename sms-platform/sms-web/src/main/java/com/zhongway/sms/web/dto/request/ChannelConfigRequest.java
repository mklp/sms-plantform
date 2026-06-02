package com.zhongway.sms.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 通道配置请求 DTO
 */
@Data
@Schema(description = "通道配置请求")
public class ChannelConfigRequest {

    @Schema(description = "通道 ID (更新时必填)", example = "channel_001")
    private Long id;

    @NotBlank(message = "通道名称不能为空")
    @Schema(description = "通道名称", example = "移动网关 1", required = true)
    private String channelName;

    @NotBlank(message = "运营商类型不能为空")
    @Schema(description = "运营商类型", example = "CMCC", required = true)
    private String operatorType;

    @NotNull(message = "优先级不能为空")
    @Schema(description = "优先级 (数字越小优先级越高)", example = "1", required = true)
    private Integer priority;

    @NotNull(message = "权重不能为空")
    @Schema(description = "权重 (用于负载均衡)", example = "100", required = true)
    private Integer weight;

    @Schema(description = "每日发送上限", example = "100000")
    private Long dailyLimit;

    @Schema(description = "每秒发送上限 (QPS)", example = "500")
    private Integer qpsLimit;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled = true;

    @Schema(description = "网关 IP", example = "192.168.1.100")
    private String gatewayIp;

    @Schema(description = "网关端口", example = "7890")
    private Integer gatewayPort;

    @Schema(description = "企业代码", example = "901000")
    private String enterpriseCode;

    @Schema(description = "共享密钥", example = "secret123")
    private String sharedSecret;

    @Schema(description = "备注", example = "主用通道")
    private String remark;
}
