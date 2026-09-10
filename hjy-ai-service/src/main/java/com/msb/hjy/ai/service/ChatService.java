package com.msb.hjy.ai.service;

import com.msb.hjy.ai.dto.ChatRequest;
import com.msb.hjy.ai.dto.ChatResponse;
import reactor.core.publisher.Flux;

/**
 * 聊天服务接口
 * <p>
 * 定义 AI 对话的核心业务方法，包括普通对话、指定代理类型对话、
 * 流式对话以及会话清除功能。
 */
public interface ChatService {

    /** 发起对话 */
    ChatResponse chat(ChatRequest request);

    /** 以指定代理类型发起对话 */
    ChatResponse chatWithAgent(ChatRequest request, String agentType);

    /** 流式对话（SSE），逐段返回内容 */
    Flux<String> chatStream(ChatRequest request);

    /** 清除指定会话的历史记录 */
    void clearSession(String sessionId);
}
