package com.zhongway.sms.web.controller.admin;

import com.zhongway.sms.web.dto.request.SendRecordQueryRequest;
import com.zhongway.sms.web.dto.response.ApiResponse;
import com.zhongway.sms.web.dto.response.SendRecordResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 发送记录查询 API 控制器
 * 
 * 提供短信发送记录查询接口（支持冷热数据分离查询）
 */
@Slf4j
@RestController
@RequestMapping("/admin/v1/records")
@RequiredArgsConstructor
@Tag(name = "记录查询", description = "短信发送记录查询接口")
public class SendRecordController {

    // TODO: 注入记录查询服务
    // private final SendRecordService sendRecordService;

    @GetMapping
    @Operation(summary = "查询发送记录", description = "分页查询短信发送记录（支持冷热数据分离）")
    public ApiResponse<List<SendRecordResponse>> listRecords(
            @Parameter(description = "租户 ID", example = "tenant_001")
            @RequestParam(required = false) String tenantId,
            
            @Parameter(description = "手机号", example = "13800138000")
            @RequestParam(required = false) String phone,
            
            @Parameter(description = "外部订单号", example = "order_202401010001")
            @RequestParam(required = false) String outOrderId,
            
            @Parameter(description = "通道 ID", example = "1")
            @RequestParam(required = false) Long channelId,
            
            @Parameter(description = "发送状态", example = "DELIVERED")
            @RequestParam(required = false) String submitStatus,
            
            @Parameter(description = "开始时间", example = "2024-01-01T00:00:00")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            
            @Parameter(description = "结束时间", example = "2024-01-01T23:59:59")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            
            @Parameter(description = "是否查询热表 (默认 true)", example = "true")
            @RequestParam(defaultValue = "true") Boolean queryHot,
            
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") Integer pageNum,
            
            @Parameter(description = "每页数量", example = "20")
            @RequestParam(defaultValue = "20") Integer pageSize) {
        
        log.info("查询发送记录：phone={}, startTime={}, endTime={}, queryHot={}", 
                phone, startTime, endTime, queryHot);
        
        // 模拟响应
        List<SendRecordResponse> list = new ArrayList<>();
        
        for (int i = 0; i < 5; i++) {
            SendRecordResponse record = new SendRecordResponse();
            record.setId(1234567890L + i);
            record.setTenantId(tenantId != null ? tenantId : "tenant_001");
            record.setPhone(phone != null ? phone : "1380013800" + i);
            record.setContent("【签名】您的验证码是 123456");
            record.setOutOrderId("order_" + System.currentTimeMillis());
            record.setChannelId(1L);
            record.setChannelName("移动网关 1");
            record.setSubmitStatus("DELIVERED");
            record.setDeliverStatus("0");
            record.setMsgId("msg_" + System.nanoTime());
            record.setBizType("VERIFY_CODE");
            record.setSmsCount(1);
            record.setSubmitTime(LocalDateTime.now().minusMinutes(i));
            record.setDeliverTime(LocalDateTime.now().minusMinutes(i).plusSeconds(5));
            list.add(record);
        }
        
        return ApiResponse.success(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询记录详情", description = "根据记录 ID 查询详细信息")
    public ApiResponse<SendRecordResponse> getRecord(
            @Parameter(description = "记录 ID", required = true) 
            @PathVariable Long id) {
        
        log.info("查询记录详情：id={}", id);
        
        // 模拟响应
        SendRecordResponse record = new SendRecordResponse();
        record.setId(id);
        record.setTenantId("tenant_001");
        record.setPhone("13800138000");
        record.setContent("【签名】您的验证码是 123456");
        record.setSubmitStatus("DELIVERED");
        record.setDeliverStatus("0");
        record.setSubmitTime(LocalDateTime.now());
        record.setDeliverTime(LocalDateTime.now().plusSeconds(3));
        
        return ApiResponse.success(record);
    }

    @GetMapping("/export")
    @Operation(summary = "导出发送记录", description = "导出指定条件的发送记录为 CSV/Excel")
    public ApiResponse<String> exportRecords(
            @ModelAttribute SendRecordQueryRequest request) {
        
        log.info("导出发送记录：startTime={}, endTime={}", 
                request.getStartTime(), request.getEndTime());
        
        // TODO: 调用服务层导出数据，返回下载链接
        
        // 模拟响应
        String downloadUrl = "/downloads/sms_records_20240101.csv";
        return ApiResponse.success("导出成功，下载地址：" + downloadUrl, downloadUrl);
    }
}
