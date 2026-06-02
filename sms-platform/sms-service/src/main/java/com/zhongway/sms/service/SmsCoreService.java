package com.zhongway.sms.service;

import com.zhongway.sms.mq.producer.SmsSendMessage;

/**
 * 短信核心服务接口
 * 负责与运营商网关进行实际通信
 * 
 * @author SMS Platform Team
 */
public interface SmsCoreService {

    /**
     * 发送短信
     * 
     * @param message 短信消息
     * @return 是否发送成功
     */
    boolean sendSms(SmsSendMessage message);

    /**
     * 提交短信到运营商网关
     * 
     * @param message 短信消息
     * @return 运营商返回的消息 ID
     */
    String submitToGateway(SmsSendMessage message);

    /**
     * 查询短信状态
     * 
     * @param messageId 消息 ID
     * @return 状态码
     */
    String queryStatus(String messageId);

    /**
     * 关闭通道连接
     * 
     * @param channelId 通道 ID
     */
    void closeChannel(Long channelId);

    /**
     * 初始化通道连接
     * 
     * @param channelId 通道 ID
     */
    void initChannel(Long channelId);
}
