package com.msb.hjy.ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置类
 * <p>
 * 自定义 RedisTemplate，Key 使用 String 序列化（方便查看），
 * Value 使用 Jackson2JsonRedisSerializer JSON 序列化，
 * 用于存储对话历史、用户上下文等数据。
 */
@Configuration
public class RedisConfig {

    /**
     * 配置 RedisTemplate Bean
     * <p>
     * Key 用 StringRedisSerializer（人类可读），
     * Value 用 Jackson2JsonRedisSerializer（支持对象序列化）。
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = 
            new Jackson2JsonRedisSerializer<>(Object.class);
        
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        template.afterPropertiesSet();
        
        return template;
    }
}
