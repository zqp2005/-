package com.msb.hjy.ai.common.constant;

public class RedisKeys {

    public static String chatSession(String sessionId) {
        return AiConstants.Session.SESSION_PREFIX + sessionId;
    }

    public static String conversationHistory(String sessionId) {
        return AiConstants.Session.SESSION_PREFIX + sessionId + ":history";
    }

    public static String userContext(Long userId) {
        return "ai:user:context:" + userId;
    }

    public static String knowledgeCache(String category) {
        return "ai:knowledge:" + category;
    }
}
