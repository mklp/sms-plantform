package com.example.sms.core.client;

import com.chinamobile.cmos.CmppClient;
import com.chinamobile.cmos.CmppConfig;
import com.example.sms.core.model.SmsSubmitRequest;
import com.example.sms.core.model.SmsSubmitResponse;
import com.example.sms.core.model.DeliveryReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 中国移动 CMPP 短信客户端实现
 * 基于 sms-core 2.1.13.6 封装
 */
public class CmppSmsClient implements SmsClient {
    private static final Logger logger = LoggerFactory.getLogger(CmppSmsClient.class);

    private CmppClient cmppClient;
    private CmppConfig config;
    private final AtomicBoolean connected = new AtomicBoolean(false);

    public CmppSmsClient() {
    }

    @Override
    public void init(String host, int port, String enterpriseId, String sharedSecret) {
        try {
            this.config = new CmppConfig();
            this.config.setHost(host);
            this.config.setPort(port);
            this.config.setEnterpriseId(enterpriseId);
            this.config.setSharedSecret(sharedSecret);
            
            // 配置连接参数
            this.config.setMaxConnections(10);
            this.config.setConnectTimeout(5000);
            this.config.setReadTimeout(10000);
            
            this.cmppClient = new CmppClient(this.config);
            this.cmppClient.connect();
            this.connected.set(true);
            
            logger.info("CMPP 客户端初始化成功，host={}, port={}, enterpriseId={}", host, port, enterpriseId);
        } catch (Exception e) {
            logger.error("CMPP 客户端初始化失败", e);
            this.connected.set(false);
            throw new RuntimeException("CMPP 客户端初始化失败：" + e.getMessage(), e);
        }
    }

    @Override
    public SmsSubmitResponse submit(SmsSubmitRequest request) {
        if (!isAvailable()) {
            logger.warn("CMPP 客户端不可用，msgId={}", request.getMsgId());
            return createErrorResponse(request.getMsgId(), "CLIENT_UNAVAILABLE", "客户端未连接或不可用");
        }

        try {
            // 调用 sms-core 发送接口
            // 注意：实际使用时需要根据 sms-core 的具体 API 调整
            String msgId = cmppClient.send(
                request.getDestPhones().toArray(new String[0]),
                request.getContent(),
                request.getNeedReport()
            );

            SmsSubmitResponse response = new SmsSubmitResponse();
            response.setMsgId(request.getMsgId());
            response.setSuccess(true);
            response.setRespCode("0");
            response.setRespMsg("提交成功");
            response.setChannelId(request.getChannelId());
            
            logger.info("短信提交成功，msgId={}, nativeMsgId={}", request.getMsgId(), msgId);
            return response;

        } catch (Exception e) {
            logger.error("短信提交失败，msgId={}", request.getMsgId(), e);
            return createErrorResponse(request.getMsgId(), "SUBMIT_FAILED", e.getMessage());
        }
    }

    @Override
    public List<SmsSubmitResponse> batchSubmit(List<SmsSubmitRequest> requests) {
        List<SmsSubmitResponse> responses = new ArrayList<>(requests.size());
        for (SmsSubmitRequest request : requests) {
            responses.add(submit(request));
        }
        return responses;
    }

    @Override
    public List<DeliveryReport> getDeliveryReports() {
        if (!isAvailable()) {
            logger.warn("CMPP 客户端不可用，无法拉取状态报告");
            return new ArrayList<>();
        }

        try {
            // 调用 sms-core 拉取状态报告接口
            // 注意：实际使用时需要根据 sms-core 的具体 API 调整
            List<com.chinamobile.cmos.DeliveryReport> reports = cmppClient.getDeliveryReports(100);
            
            List<DeliveryReport> result = new ArrayList<>(reports.size());
            for (com.chinamobile.cmos.DeliveryReport report : reports) {
                DeliveryReport dr = new DeliveryReport();
                dr.setMsgId(report.getMessageId());
                dr.setNativeMsgId(report.getNativeMessageId());
                dr.setPhoneNumber(report.getPhoneNumber());
                dr.setStatusCode(report.getStatusCode());
                dr.setStatusDesc(report.getStatusDescription());
                dr.setSuccess(report.isSuccess());
                result.add(dr);
            }
            
            return result;
        } catch (Exception e) {
            logger.error("拉取状态报告失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<String> getMoMessages() {
        if (!isAvailable()) {
            logger.warn("CMPP 客户端不可用，无法拉取上行短信");
            return new ArrayList<>();
        }

        try {
            // 调用 sms-core 拉取上行短信接口
            return cmppClient.getMoMessages(100);
        } catch (Exception e) {
            logger.error("拉取上行短信失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public void close() {
        if (cmppClient != null) {
            try {
                cmppClient.close();
                connected.set(false);
                logger.info("CMPP 客户端已关闭");
            } catch (Exception e) {
                logger.error("关闭 CMPP 客户端失败", e);
            }
        }
    }

    @Override
    public boolean isAvailable() {
        return connected.get() && cmppClient != null;
    }

    /**
     * 创建错误响应
     */
    private SmsSubmitResponse createErrorResponse(String msgId, String respCode, String respMsg) {
        SmsSubmitResponse response = new SmsSubmitResponse();
        response.setMsgId(msgId);
        response.setSuccess(false);
        response.setRespCode(respCode);
        response.setRespMsg(respMsg);
        return response;
    }
}
