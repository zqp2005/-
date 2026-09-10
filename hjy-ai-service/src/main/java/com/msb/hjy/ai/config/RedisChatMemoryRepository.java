package com.msb.hjy.ai.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 基于 Redis 的会话记忆存储：重启不丢、多实例共享
 * <p>
 * 每个 conversationId 一个 Redis List，key 为 ai:chat:memory:{id}，
 * 值为 JSON（role + content），窗口裁剪由 MessageWindowChatMemory 负责。
 */
@Slf4j
@Component
public class RedisChatMemoryRepository implements ChatMemoryRepository {

    private static final String KEY_PREFIX = "ai:chat:memory:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RedisChatMemoryRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public List<String> findConversationIds() {
        Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
        return keys == null ? List.of()
                : keys.stream().map(k -> k.substring(KEY_PREFIX.length())).collect(Collectors.toList());
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        List<String> jsonList = redisTemplate.opsForList().range(key(conversationId), 0, -1);
        if (jsonList == null) {
            return List.of();
        }
        return jsonList.stream().map(this::toMessage).collect(Collectors.toList());
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        String key = key(conversationId);
        redisTemplate.delete(key);
        if (messages == null || messages.isEmpty()) {
            return;
        }
        List<String> jsonList = messages.stream().map(this::toJson).collect(Collectors.toList());
        redisTemplate.opsForList().rightPushAll(key, jsonList);
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        redisTemplate.delete(key(conversationId));
    }

    private String key(String conversationId) {
        return KEY_PREFIX + conversationId;
    }

    private String toJson(Message message) {
        try {
            Map<String, String> map = new HashMap<>();
            map.put("role", message.getMessageType().getValue());
            map.put("content", message.getText());
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            log.error("消息序列化失败", e);
            return "{}";
        }
    }

    private Message toMessage(String json) {
        try {
            Map<String, String> map = objectMapper.readValue(json, Map.class);
            String role = map.getOrDefault("role", "user");
            String content = map.getOrDefault("content", "");
            return "assistant".equals(role) ? new AssistantMessage(content) : new UserMessage(content);
        } catch (Exception e) {
            log.error("消息反序列化失败: {}", json, e);
            return new UserMessage("");
        }
    }
}
