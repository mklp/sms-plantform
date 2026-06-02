package com.example.sms.web.controller;

import com.example.sms.api.dto.SmsSendRequest;
import com.example.sms.api.vo.SmsSendResultVO;
import com.example.sms.common.exception.SmsException;
import com.example.sms.common.exception.TenantException;
import com.example.sms.service.SmsSendService;
import com.example.sms.web.config.TenantContext;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 短信发送 REST API 控制器
 * 
 * @author SMS Platform Team
 */
@RestController
@RequestMapping("/sms")
public class SmsController {

    private static final Logger logger = LoggerFactory.getLogger(SmsController.class);

    private final SmsSendService smsSendService;

    public SmsController(SmsSendService smsSendService) {
        this.smsSendService = smsSendService;
    }

    /**
     * 单条短信发送
     * 
     * POST /sms/send
     * 
     * @param request 发送请求
     * @return 发送结果
     */
    @PostMapping("/send")
    public ResponseEntity<SmsSendResultVO> sendSms(@Valid @RequestBody SmsSendRequest request) {
        logger.info("Received single SMS send request from tenant: {}", TenantContext.getCurrentTenantId());
        
        try {
            SmsSendResultVO result = smsSendService.sendSingle(request);
            logger.info("SMS sent successfully, messageId: {}", result.getMessageId());
            return ResponseEntity.ok(result);
        } catch (TenantException e) {
            logger.warn("Tenant validation failed: {}", e.getMessage());
            return ResponseEntity.status(403).body(SmsSendResultVO.fail(e.getMessage()));
        } catch (SmsException e) {
            logger.error("SMS send failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(SmsSendResultVO.fail(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during SMS send", e);
            return ResponseEntity.internalServerError()
                    .body(SmsSendResultVO.fail("System error, please try again later"));
        }
    }

    /**
     * 批量短信发送
     * 
     * POST /sms/batch
     * 
     * @param requests 批量发送请求列表
     * @return 批量发送结果
     */
    @PostMapping("/batch")
    public ResponseEntity<List<SmsSendResultVO>> sendBatchSms(@Valid @RequestBody List<SmsSendRequest> requests) {
        logger.info("Received batch SMS send request from tenant: {}, count: {}", 
                TenantContext.getCurrentTenantId(), requests.size());
        
        try {
            List<SmsSendResultVO> results = smsSendService.sendBatch(requests);
            long successCount = results.stream().filter(r -> "SUCCESS".equals(r.getStatus())).count();
            logger.info("Batch SMS completed: total={}, success={}", requests.size(), successCount);
            return ResponseEntity.ok(results);
        } catch (TenantException e) {
            logger.warn("Tenant validation failed: {}", e.getMessage());
            return ResponseEntity.status(403).body(List.of(SmsSendResultVO.fail(e.getMessage())));
        } catch (SmsException e) {
            logger.error("Batch SMS send failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(List.of(SmsSendResultVO.fail(e.getMessage())));
        } catch (Exception e) {
            logger.error("Unexpected error during batch SMS send", e);
            return ResponseEntity.internalServerError()
                    .body(List.of(SmsSendResultVO.fail("System error, please try again later")));
        }
    }

    /**
     * 根据模板发送短信
     * 
     * POST /sms/template
     * 
     * @param request 发送请求 (包含模板 ID 和参数)
     * @return 发送结果
     */
    @PostMapping("/template")
    public ResponseEntity<SmsSendResultVO> sendByTemplate(@Valid @RequestBody SmsSendRequest request) {
        logger.info("Received template SMS send request from tenant: {}", TenantContext.getCurrentTenantId());
        
        try {
            SmsSendResultVO result = smsSendService.sendByTemplate(request);
            logger.info("Template SMS sent successfully, messageId: {}", result.getMessageId());
            return ResponseEntity.ok(result);
        } catch (TenantException e) {
            logger.warn("Tenant validation failed: {}", e.getMessage());
            return ResponseEntity.status(403).body(SmsSendResultVO.fail(e.getMessage()));
        } catch (SmsException e) {
            logger.error("Template SMS send failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(SmsSendResultVO.fail(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during template SMS send", e);
            return ResponseEntity.internalServerError()
                    .body(SmsSendResultVO.fail("System error, please try again later"));
        }
    }

    /**
     * 查询短信发送状态
     * 
     * GET /sms/status/{messageId}
     * 
     * @param messageId 消息 ID
     * @return 发送状态
     */
    @GetMapping("/status/{messageId}")
    public ResponseEntity<SmsSendResultVO> queryStatus(@PathVariable String messageId) {
        logger.info("Querying SMS status for messageId: {}", messageId);
        
        try {
            SmsSendResultVO result = smsSendService.queryStatus(messageId);
            return ResponseEntity.ok(result);
        } catch (SmsException e) {
            logger.error("Query status failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(SmsSendResultVO.fail(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during status query", e);
            return ResponseEntity.internalServerError()
                    .body(SmsSendResultVO.fail("System error, please try again later"));
        }
    }
}
