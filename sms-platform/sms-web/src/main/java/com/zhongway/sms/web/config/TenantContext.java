package com.zhongway.sms.web.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 租户上下文 - 使用 ThreadLocal 存储当前请求的租户信息
 * 
 * @author SMS Platform Team
 */
public class TenantContext {

    private static final Logger logger = LoggerFactory.getLogger(TenantContext.class);

    /**
     * ThreadLocal 存储当前租户 ID
     */
    private static final ThreadLocal<String> TENANT_ID_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 设置当前租户 ID
     * 
     * @param tenantId 租户 ID
     */
    public static void setCurrentTenantId(String tenantId) {
        if (tenantId == null || tenantId.trim().isEmpty()) {
            logger.warn("Attempted to set null or empty tenant ID");
            TENANT_ID_THREAD_LOCAL.remove();
        } else {
            TENANT_ID_THREAD_LOCAL.set(tenantId);
            logger.debug("Set tenant ID: {}", tenantId);
        }
    }

    /**
     * 获取当前租户 ID
     * 
     * @return 租户 ID，如果未设置则返回 null
     */
    public static String getCurrentTenantId() {
        return TENANT_ID_THREAD_LOCAL.get();
    }

    /**
     * 清除当前租户 ID
     * 必须在请求结束后调用，防止内存泄漏
     */
    public static void clear() {
        String tenantId = TENANT_ID_THREAD_LOCAL.get();
        if (tenantId != null) {
            logger.debug("Cleared tenant ID: {}", tenantId);
        }
        TENANT_ID_THREAD_LOCAL.remove();
    }

    /**
     * 检查是否已设置租户 ID
     * 
     * @return true-已设置，false-未设置
     */
    public static boolean hasTenantId() {
        String tenantId = TENANT_ID_THREAD_LOCAL.get();
        return tenantId != null && !tenantId.trim().isEmpty();
    }
}
