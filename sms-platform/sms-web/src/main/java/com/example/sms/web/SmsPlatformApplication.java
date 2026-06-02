package com.example.sms.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 短信平台启动类
 * 
 * @author SMS Platform Team
 * @since 2025-01
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
@ComponentScan(basePackages = {"com.example.sms"})
@MapperScan("com.example.sms.dao.mapper")
public class SmsPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmsPlatformApplication.class, args);
        System.out.println("=================================================");
        System.out.println("     SMS Platform Started Successfully!");
        System.out.println("     Version: 1.0.0-SNAPSHOT");
        System.out.println("=================================================");
    }
}
