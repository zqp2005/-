package com.msb.hjy.mcp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * 高德接口 HTTP 客户端配置
 * <p>
 * 连接超时 5 秒、读取超时 10 秒，避免高德接口异常时阻塞 MCP 工具调用。
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate amapRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);   // 连接超时 5 秒
        factory.setReadTimeout(10000);     // 读取超时 10 秒
        return new RestTemplate(factory);
    }
}
