package com.msb.hjy.ai.config;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Collections;

/** Stores one short-lived repair draft per authenticated conversation. */
@Component
public class PendingRepairConfirmationStore {
    private static final String KEY_PREFIX = "ai:repair:pending:";
    private static final Duration TTL = Duration.ofMinutes(10);
    private static final DefaultRedisScript<String> CONSUME_SCRIPT = new DefaultRedisScript<>(
            "local v=redis.call('GET',KEYS[1]); if v then redis.call('DEL',KEYS[1]); end; return v",
            String.class);

    private final StringRedisTemplate redisTemplate;

    public PendingRepairConfirmationStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(String conversationId, String input) {
        redisTemplate.opsForValue().set(key(conversationId), input, TTL);
    }

    public String consume(String conversationId) {
        return redisTemplate.execute(CONSUME_SCRIPT, Collections.singletonList(key(conversationId)));
    }

    public void delete(String conversationId) {
        redisTemplate.delete(key(conversationId));
    }

    private String key(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            throw new IllegalArgumentException("缺少会话标识");
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(conversationId.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(digest.length * 2);
            for (byte value : digest) result.append(String.format("%02x", value));
            return KEY_PREFIX + result;
        } catch (Exception e) {
            throw new IllegalStateException("生成会话标识失败", e);
        }
    }
}
