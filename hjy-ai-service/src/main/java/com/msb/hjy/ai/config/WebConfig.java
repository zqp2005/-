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
public class WebConfig implements org.springframework.web.servlet.config.annotation.WebMvcConfigurer {

    /** SSE 字符串分片必须使用 UTF-8，防止默认 Latin-1 转换器把中文写成问号。 */
    @Override
    public void extendMessageConverters(java.util.List<org.springframework.http.converter.HttpMessageConverter<?>> converters) {
        for (org.springframework.http.converter.HttpMessageConverter<?> converter : converters) {
            if (converter instanceof org.springframework.http.converter.StringHttpMessageConverter text) {
                text.setDefaultCharset(java.nio.charset.StandardCharsets.UTF_8);
            }
        }
    }

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
