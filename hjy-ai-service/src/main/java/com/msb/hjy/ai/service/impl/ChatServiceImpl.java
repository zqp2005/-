package com.msb.hjy.ai.service.impl;

import com.msb.hjy.ai.dto.ChatRequest;
import com.msb.hjy.ai.dto.ChatResponse;
import com.msb.hjy.ai.prompt.PromptTemplate;
import com.msb.hjy.ai.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final PromptTemplate promptTemplate;
    private static final Set<String> GREETING_KEYWORDS = Set.of("你好", "hi", "hello", "在吗", "您好", "嗨", "hey");

    public ChatServiceImpl(ChatClient chatClient, PromptTemplate promptTemplate) {
        this.chatClient = chatClient;
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
            if (message == null || message.trim().isEmpty()) {
                return ChatResponse.success(request.getSessionId(), PromptTemplate.GREETING);
            }

            String lowerMessage = message.trim().toLowerCase();
            if (GREETING_KEYWORDS.contains(lowerMessage)) {
                return ChatResponse.success(request.getSessionId(), PromptTemplate.GREETING + "\n\n" + PromptTemplate.HELP_PROMPT);
            }

            if (lowerMessage.contains("帮助") || lowerMessage.contains("能做什么") || lowerMessage.contains("有什么服务")) {
                return ChatResponse.success(request.getSessionId(), PromptTemplate.HELP_PROMPT);
            }

            String userMessage = buildUserMessage(request);
            
            String response = chatClient.prompt()
                    .user(userMessage)
                    .call()
                    .content();

            log.info("AI响应 - sessionId: {}, response: {}", request.getSessionId(), response);

            return ChatResponse.success(request.getSessionId(), response);

        } catch (Exception e) {
            log.error("聊天处理异常 - sessionId: {}, error: {}", request.getSessionId(), e.getMessage(), e);
            return ChatResponse.error(request.getSessionId(), "AI服务暂时无法响应: " + e.getMessage());
        }
    }

    @Override
    public void clearSession(String sessionId) {
        log.info("清除会话历史 - sessionId: {}", sessionId);
    }

    private String buildUserMessage(ChatRequest request) {
        return String.format("【用户信息】\n用户名: %s\n用户ID: %s\n\n【用户消息】\n%s",
                request.getDisplayName(),
                request.getUserId() != null ? request.getUserId() : "游客",
                request.getMessage());
    }
}