package com.msb.hjy.ai.common.constant;

/**
 * Redis 键值生成器
 * <p>
 * 统一管理 Redis 中各种数据的 Key 格式，确保 Key 命名规范且不冲突。
 * 所有 Key 均以 "ai:" 为前缀，按功能模块分层命名。
 */
public class RedisKeys {

    /** 生成聊天会话的 Redis Key */
    public static String chatSession(String sessionId) {
        return AiConstants.Session.SESSION_PREFIX + sessionId;
    }

    /** 生成对话历史记录的 Redis Key */
    public static String conversationHistory(String sessionId) {
        return AiConstants.Session.SESSION_PREFIX + sessionId + ":history";
    }

    /** 生成用户上下文信息的 Redis Key */
    public static String userContext(Long userId) {
        return "ai:user:context:" + userId;
    }

    /** 生成知识库缓存的 Redis Key */
    public static String knowledgeCache(String category) {
        return "ai:knowledge:" + category;
    }
}
