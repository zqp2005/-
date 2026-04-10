package com.msb.hjy.ai.config;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.embeddings.TextEmbedding;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AiConfig {

    @Value("${hjy.ai.api-key}")
    private String apiKey;

    @Value("${hjy.ai.model:qwen-plus}")
    private String model;

    @Value("${hjy.ai.temperature:0.7}")
    private Double temperature;

    @Value("${hjy.ai.max-tokens:2000}")
    private Integer maxTokens;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public Generation generation() {
        return new Generation();
    }

    @Bean
    public TextEmbedding textEmbedding() {
        return new TextEmbedding();
    }

    @Bean
    public String dashscopeApiKey() {
        return apiKey;
    }

    @Bean
    public String chatModel() {
        return model;
    }
}
