package com.zhongway.sms.api.vo;

import lombok.Builder;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 短信发送结果 VO
 */
@Data
@Builder
public class SmsSendResultVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 消息 ID
     */
    private String messageId;
    
    /**
     * 外部订单号
     */
    private String outOrderId;
    
    /**
     * 接收手机号
     */
    private String phoneNumber;
    
    /**
     * 发送状态：SUCCESS, FAILED, WAITING, SENDING
     */
    private String status;
    
    /**
     * 状态描述
     */
    private String statusDesc;
    
    /**
     * 计费条数
     */
    private Integer chargeCount;
    
    /**
     * 提交时间
     */
    private LocalDateTime submitTime;
    
    /**
     * 发送时间
     */
    private LocalDateTime sendTime;
    
    /**
     * 状态报告时间
     */
    private LocalDateTime reportTime;
    
    /**
     * 错误码 (发送失败时)
     */
    private String errorCode;
    
    /**
     * 错误信息 (发送失败时)
     */
    private String errorMessage;
    
    /**
     * 构建成功结果
     */
    public static SmsSendResultVO success(String messageId, String status) {
        return SmsSendResultVO.builder()
                .messageId(messageId)
                .status(status)
                .statusDesc("发送成功")
                .submitTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * 构建失败结果
     */
    public static SmsSendResultVO fail(String errorMessage) {
        return SmsSendResultVO.builder()
                .status("FAILED")
                .statusDesc("发送失败")
                .errorMessage(errorMessage)
                .errorCode("SYSTEM_ERROR")
                .submitTime(LocalDateTime.now())
                .build();
    }
}
