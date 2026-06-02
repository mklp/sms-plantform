package com.zhongway.sms.service;

import com.zhongway.sms.api.dto.SmsSendRequest;
import com.zhongway.sms.api.vo.SmsSendResultVO;

import java.util.List;

/**
 * 短信发送服务接口
 * 
 * @author SMS Platform Team
 */
public interface SmsSendService {

    /**
     * 单条短信发送
     * 
     * @param request 发送请求
     * @return 发送结果
     */
    SmsSendResultVO sendSingle(SmsSendRequest request);

    /**
     * 批量短信发送
     * 
     * @param requests 批量发送请求列表
     * @return 批量发送结果列表
     */
    List<SmsSendResultVO> sendBatch(List<SmsSendRequest> requests);

    /**
     * 根据模板发送短信
     * 
     * @param request 发送请求 (包含模板 ID 和参数)
     * @return 发送结果
     */
    SmsSendResultVO sendByTemplate(SmsSendRequest request);

    /**
     * 查询短信发送状态
     * 
     * @param messageId 消息 ID
     * @return 发送状态
     */
    SmsSendResultVO queryStatus(String messageId);
}
