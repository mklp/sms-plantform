package com.example.sms.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 添加静态资源处理器，用于文件下载等
        registry.addResourceHandler("/downloads/**")
                .addResourceLocations("file:/workspace/sms-platform/sms-web/downloads/");
    }
}
