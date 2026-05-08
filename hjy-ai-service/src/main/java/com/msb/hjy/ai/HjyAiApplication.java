package com.msb.hjy.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 合家云社区 AI 智能服务 - 应用启动类
 * <p>
 * 基于 Spring Boot 3.2.5 + Spring AI 1.0.0，集成 DeepSeek 大模型，
 * 提供智能对话、报修查询、投诉处理、物业费查询等 AI 客服功能。
 * 前端通过 /ai/* 路径代理访问，运行于 8090 端口。
 */
@SpringBootApplication
@EnableConfigurationProperties
public class HjyAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(HjyAiApplication.class, args);
        System.out.println("""
            
            ╔═══════════════════════════════════════════════════════════╗
            ║                                                           ║
            ║     🏠 合家云社区 AI 智能服务                               ║
            ║     🚀 AI Service Started Successfully!                   ║
            ║     📍 http://localhost:8090                              ║
            ║     📖 Swagger: http://localhost:8090/swagger-ui.html     ║
            ║                                                           ║
            ╚═══════════════════════════════════════════════════════════╝
            """);
    }
}
