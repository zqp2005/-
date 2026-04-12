package com.msb.hjy.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class   HjyAiApplication {

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
