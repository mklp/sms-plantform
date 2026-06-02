package com.zhongway.sms.web.config;

import com.zhongway.sms.common.exception.SmsException;
import com.zhongway.sms.common.exception.TenantException;
import com.zhongway.sms.dao.entity.SmsTenant;
import com.zhongway.sms.dao.mapper.SmsTenantMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

/**
 * 租户过滤器
 * 从请求头中提取租户 ID 并设置到 ThreadLocal
 * 
 * @author SMS Platform Team
 */
@Component
public class TenantFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(TenantFilter.class);

    @Value("${tenant.header-name:X-Tenant-ID}")
    private String tenantHeaderName;

    @Value("${tenant.isolation-enabled:true}")
    private Boolean tenantIsolationEnabled;

    private final SmsTenantMapper smsTenantMapper;

    public TenantFilter(SmsTenantMapper smsTenantMapper) {
        this.smsTenantMapper = smsTenantMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // 如果未启用租户隔离，直接放行
        if (Boolean.FALSE.equals(tenantIsolationEnabled)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 获取请求路径
        String requestUri = request.getRequestURI();
        
        // 某些路径不需要租户验证 (如健康检查、公开 API 等)
        if (isExcludedPath(requestUri)) {
            logger.debug("Excluded path: {}", requestUri);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 从请求头获取租户 ID
            String tenantId = request.getHeader(tenantHeaderName);
            
            if (StringUtils.isBlank(tenantId)) {
                // 尝试从 API Key 推导租户 (可选功能)
                tenantId = resolveTenantFromApiKey(request);
            }

            if (StringUtils.isNotBlank(tenantId)) {
                // 验证租户是否存在且状态正常
                validateTenant(tenantId);
                
                // 设置到 ThreadLocal
                TenantContext.setCurrentTenantId(tenantId);
                logger.debug("Tenant ID set for request {}: {}", requestUri, tenantId);
            } else {
                logger.warn("No tenant ID found in request to {}", requestUri);
                // 如果没有租户 ID，可以选择抛出异常或继续 (取决于业务需求)
                // throw new TenantException("Missing tenant ID in request header: " + tenantHeaderName);
            }

            // 继续执行过滤链
            filterChain.doFilter(request, response);

        } finally {
            // 请求结束后清除 ThreadLocal，防止内存泄漏
            TenantContext.clear();
            logger.debug("Tenant context cleared for request: {}", requestUri);
        }
    }

    /**
     * 判断是否为排除路径
     */
    private boolean isExcludedPath(String requestUri) {
        // 健康检查、监控端点等不需要租户验证
        return requestUri.startsWith("/actuator/")
            || requestUri.startsWith("/sms-api/health")
            || requestUri.startsWith("/sms-api/public/")
            || "/sms-api".equals(requestUri)
            || "/".equals(requestUri);
    }

    /**
     * 从 API Key 解析租户 ID (可选实现)
     */
    private String resolveTenantFromApiKey(HttpServletRequest request) {
        String apiKey = request.getHeader("X-API-Key");
        if (StringUtils.isBlank(apiKey)) {
            return null;
        }
        
        // 根据 API Key 查询租户
        // 注意：这里为了性能可以考虑加缓存
        SmsTenant tenant = smsTenantMapper.selectByApiKey(apiKey);
        if (tenant != null && "ACTIVE".equals(tenant.getStatus())) {
            return tenant.getId().toString();
        }
        
        return null;
    }

    /**
     * 验证租户状态
     */
    private void validateTenant(String tenantId) {
        try {
            Long id = Long.parseLong(tenantId);
            SmsTenant tenant = smsTenantMapper.selectById(id);
            
            if (Objects.isNull(tenant)) {
                throw new TenantException("Tenant not found: " + tenantId);
            }
            
            if (!"ACTIVE".equals(tenant.getStatus())) {
                throw new TenantException("Tenant status is not ACTIVE: " + tenant.getStatus());
            }
            
            // 可以在这里添加更多验证逻辑
            // - 检查是否过期
            // - 检查余额是否充足
            // - 检查 IP 白名单
            
        } catch (NumberFormatException e) {
            throw new TenantException("Invalid tenant ID format: " + tenantId);
        }
    }
}
