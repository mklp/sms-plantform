package com.zhongway.sms.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI 配置类
 * 
 * 访问地址：http://localhost:8080/swagger-ui.html
 * API 文档：http://localhost:8080/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:sms-platform}")
    private String appName;

    @Value("${server.version:1.0.0}")
    private String appVersion;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
            .info(new Info()
                .title(appName + " API")
                .version(appVersion)
                .description("企业级高可用短信平台 RESTful API 文档\n\n" +
                    "### 功能模块\n" +
                    "- **短信发送**: 单发、批量、模板发送\n" +
                    "- **通道管理**: 通道配置、状态监控\n" +
                    "- **租户管理**: 企业用户管理、配额管理\n" +
                    "- **记录查询**: 发送记录、状态报告\n" +
                    "- **系统管理**: 参数配置、字典管理\n\n" +
                    "### 认证方式\n" +
                    "使用 Bearer Token 进行认证，在请求头中添加 `Authorization: Bearer <token>`")
                .contact(new Contact()
                    .name("SMS Platform Team")
                    .email("sms-support@example.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0")))
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            .components(new Components()
                .addSecuritySchemes(securitySchemeName,
                    new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT Bearer Token 认证")));
    }
}
