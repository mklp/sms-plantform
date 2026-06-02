package com.example.sms.web.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类
 * 配置分页插件和多租户插件
 * 
 * @author SMS Platform Team
 */
@Configuration
public class MybatisPlusConfig {

    @Value("${tenant.isolation-enabled:true}")
    private Boolean tenantIsolationEnabled;

    /**
     * MyBatis-Plus 拦截器配置
     * - 分页插件
     * - 多租户插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 1. 多租户插件 (如果启用)
        if (Boolean.TRUE.equals(tenantIsolationEnabled)) {
            TenantLineInnerInterceptor tenantInterceptor = new TenantLineInnerInterceptor();
            tenantInterceptor.setTenantLineHandler(new SmsTenantLineHandler());
            interceptor.addInnerInterceptor(tenantInterceptor);
        }

        // 2. 分页插件
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.POSTGRE_SQL);
        paginationInterceptor.setMaxLimit(500L); // 限制最大查询条数，防止全表扫描
        interceptor.addInnerInterceptor(paginationInterceptor);

        return interceptor;
    }

    /**
     * 多租户处理器实现
     */
    public static class SmsTenantLineHandler implements com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler {

        @Override
        public Expression getTenantId() {
            // 从 ThreadLocal 中获取当前租户 ID
            String tenantId = TenantContext.getCurrentTenantId();
            if (tenantId != null && !tenantId.isEmpty()) {
                return new StringValue(tenantId);
            }
            // 如果没有租户 ID，返回 null (不添加租户条件，适用于系统级操作)
            return null;
        }

        @Override
        public boolean ignoreTable(String tableName) {
            // 以下表不需要多租户过滤
            // sys_dict_config: 系统字典配置表 (全局共享)
            // sms_channel: 通道配置表 (全局共享，但发送记录会关联租户)
            // sys_operation_log: 操作日志表 (按租户存储但查询时可能需要跨租户)
            return "sys_dict_config".equalsIgnoreCase(tableName) 
                || "sms_channel".equalsIgnoreCase(tableName);
        }
    }
}
