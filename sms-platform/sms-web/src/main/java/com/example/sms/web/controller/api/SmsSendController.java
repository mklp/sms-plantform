package com.example.sms.web.controller.api;

import com.example.sms.web.dto.request.SmsSendRequest;
import com.example.sms.web.dto.request.SmsBatchSendRequest;
import com.example.sms.web.dto.response.ApiResponse;
import com.example.sms.web.dto.response.SmsSendResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 短信发送 API 控制器
 * 
 * 提供短信发送相关的 RESTful 接口
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/sms")
@RequiredArgsConstructor
@Tag(name = "短信发送", description = "短信发送相关接口")
public class SmsSendController {

    // TODO: 注入短信发送服务
    // private final SmsSendService smsSendService;

    @PostMapping("/send")
    @Operation(summary = "单条短信发送", description = "发送单条短信到指定手机号")
    public ApiResponse<SmsSendResponse> sendSms(@Valid @RequestBody SmsSendRequest request) {
        log.info("收到短信发送请求：phone={}, bizType={}", request.getPhone(), request.getBizType());
        
        // TODO: 调用服务层发送短信
        // SmsSendResponse response = smsSendService.send(request);
        
        // 模拟响应
        SmsSendResponse response = new SmsSendResponse();
        response.setRecordId(1234567890L);
        response.setPhone(request.getPhone());
        response.setSubmitStatus("SUCCESS");
        response.setSubmitTime(java.time.LocalDateTime.now());
        
        return ApiResponse.success(response);
    }

    @PostMapping("/batch/send")
    @Operation(summary = "批量短信发送", description = "批量发送短信到多个手机号（最多 100 个）")
    public ApiResponse<List<SmsSendResponse>> batchSendSms(@Valid @RequestBody SmsBatchSendRequest request) {
        log.info("收到批量短信发送请求：phones={}, bizType={}", request.getPhones().size(), request.getBizType());
        
        // TODO: 调用服务层批量发送短信
        // List<SmsSendResponse> responses = smsSendService.batchSend(request);
        
        // 模拟响应
        List<SmsSendResponse> responses = request.getPhones().stream().map(phone -> {
            SmsSendResponse response = new SmsSendResponse();
            response.setRecordId(System.nanoTime());
            response.setPhone(phone);
            response.setSubmitStatus("SUCCESS");
            response.setSubmitTime(java.time.LocalDateTime.now());
            return response;
        }).toList();
        
        return ApiResponse.success(responses);
    }

    @GetMapping("/status/{recordId}")
    @Operation(summary = "查询发送状态", description = "根据记录 ID 查询短信发送状态")
    public ApiResponse<SmsSendResponse> getSmsStatus(
            @Parameter(description = "发送记录 ID", required = true) 
            @PathVariable Long recordId) {
        log.info("查询短信发送状态：recordId={}", recordId);
        
        // TODO: 调用服务层查询状态
        // SmsSendResponse response = smsSendService.getStatus(recordId);
        
        // 模拟响应
        SmsSendResponse response = new SmsSendResponse();
        response.setRecordId(recordId);
        response.setSubmitStatus("DELIVERED");
        response.setMsgId("msg_" + recordId);
        
        return ApiResponse.success(response);
    }
}
