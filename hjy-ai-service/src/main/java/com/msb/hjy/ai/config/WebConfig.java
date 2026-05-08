package com.msb.hjy.ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Web 配置类 - 跨域资源共享（CORS）配置
 * <p>
 * 配置全局 CORS 策略，允许前端（Vue，端口 80）跨域访问 AI 服务（端口 8090），
 * 支持所有来源、请求头和请求方法。
 */
@Configuration
public class WebConfig {

    /**
     * 创建 CORS 过滤器，允许跨域请求
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
