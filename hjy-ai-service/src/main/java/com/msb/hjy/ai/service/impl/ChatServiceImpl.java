package com.msb.hjy.ai.service.impl;

import com.msb.hjy.ai.dto.ChatRequest;
import com.msb.hjy.ai.dto.ChatResponse;
import com.msb.hjy.ai.prompt.PromptTemplate;
import com.msb.hjy.ai.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 聊天服务实现类
 * <p>
 * 处理 AI 对话核心逻辑：问候语识别、帮助指令匹配、用户消息组装、
 * 调用 ChatClient 获取 AI 回复，以及会话历史管理。
 */
@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final PromptTemplate promptTemplate;

    /** 问候语关键词集合，用于快速匹配打招呼场景 */
    private static final Set<String> GREETING_KEYWORDS = Set.of("你好", "hi", "hello", "在吗", "您好", "嗨", "hey");

    public ChatServiceImpl(ChatClient chatClient, ChatMemory chatMemory, PromptTemplate promptTemplate) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
        this.promptTemplate = promptTemplate;
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        return chatWithAgent(request, request.getAgentType());
    }

    @Override
    public ChatResponse chatWithAgent(ChatRequest request, String agentType) {
        try {
            log.info("处理聊天请求 - sessionId: {}, agentType: {}, message: {}",
                    request.getSessionId(), agentType, request.getMessage());

            String message = request.getMessage();
            // 空消息返回问候语
            if (message == null || message.trim().isEmpty()) {
                return ChatResponse.success(request.getSessionId(), PromptTemplate.GREETING);
            }

            // 问候语匹配：返回问候 + 帮助提示
            String lowerMessage = message.trim().toLowerCase();
            if (GREETING_KEYWORDS.contains(lowerMessage)) {
                return ChatResponse.success(request.getSessionId(), PromptTemplate.GREETING + "\n\n" + PromptTemplate.HELP_PROMPT);
            }

            // 帮助指令匹配：返回服务列表
            if (lowerMessage.contains("帮助") || lowerMessage.contains("能做什么") || lowerMessage.contains("有什么服务")) {
                return ChatResponse.success(request.getSessionId(), PromptTemplate.HELP_PROMPT);
            }

            // 组装带用户信息的消息，调用 AI 对话
            String userMessage = buildUserMessage(request);

            String response = chatClient.prompt()
                    .user(userMessage)
                    .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, request.getSessionId()))
                    .call()
                    .content();

            log.info("AI响应 - sessionId: {}, response: {}", request.getSessionId(), response);

            return ChatResponse.success(request.getSessionId(), response);

        } catch (Exception e) {
            log.error("聊天处理异常 - sessionId: {}, error: {}", request.getSessionId(), e.getMessage(), e);
            return ChatResponse.error(request.getSessionId(), "AI服务暂时无法响应，请稍后重试。");
        }
    }

    @Override
    public void clearSession(String sessionId) {
        chatMemory.clear(sessionId);
        log.info("清除会话历史 - sessionId: {}", sessionId);
    }

    /**
     * 构建带用户信息的消息体，供 AI 参考用户身份
     */
    private String buildUserMessage(ChatRequest request) {
        return String.format("【用户信息】\n用户名: %s\n用户ID: %s\n\n【用户消息】\n%s",
                request.getDisplayName(),
                request.getUserId() != null ? request.getUserId() : "游客",
                request.getMessage());
    }
}