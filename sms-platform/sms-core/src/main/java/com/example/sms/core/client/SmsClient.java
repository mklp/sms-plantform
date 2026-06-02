package com.example.sms.core.client;

import com.example.sms.core.model.SmsSubmitRequest;
import com.example.sms.core.model.SmsSubmitResponse;
import com.example.sms.core.model.DeliveryReport;

/**
 * 短信发送客户端接口
 * 用于对接中国移动 CMOS sms-core 核心库
 */
public interface SmsClient {

    /**
     * 初始化客户端
     * 
     * @param host 网关主机
     * @param port 网关端口
     * @param enterpriseId 企业 ID
     * @param sharedSecret 共享密钥
     */
    void init(String host, int port, String enterpriseId, String sharedSecret);

    /**
     * 发送短信 (单条)
     * 
     * @param request 发送请求
     * @return 发送响应
     */
    SmsSubmitResponse submit(SmsSubmitRequest request);

    /**
     * 批量发送短信
     * 
     * @param requests 发送请求列表
     * @return 响应列表
     */
    java.util.List<SmsSubmitResponse> batchSubmit(java.util.List<SmsSubmitRequest> requests);

    /**
     * 拉取状态报告
     * 
     * @return 状态报告列表
     */
    java.util.List<DeliveryReport> getDeliveryReports();

    /**
     * 拉取上行短信
     * 
     * @return 上行短信列表
     */
    java.util.List<String> getMoMessages();

    /**
     * 关闭连接
     */
    void close();

    /**
     * 检查连接是否可用
     * 
     * @return 是否可用
     */
    boolean isAvailable();
}
