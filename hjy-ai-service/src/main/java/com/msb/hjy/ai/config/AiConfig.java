package com.msb.hjy.ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * AI 服务基础配置类
 * <p>
 * 配置 RestTemplate Bean，用于与 hjy-community 主后端进行 HTTP 通信。
 * 设置了连接超时（10秒）和读取超时（30秒），避免长时间阻塞。
 */
@Configuration
public class AiConfig {

    /**
     * 创建 RestTemplate 实例，配置超时参数
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(30000);
        return new RestTemplate(factory);
    }
}