package com.msb.hjy.ai.service;

import com.msb.hjy.ai.agent.SystemPrompt;
import com.msb.hjy.ai.config.AiProperties;
import com.msb.hjy.ai.dto.ChatRequest;
import com.msb.hjy.ai.dto.ChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    private final DashScopeService dashScopeService;
    private final AiProperties aiProperties;

    public ChatServiceImpl(DashScopeService dashScopeService, AiProperties aiProperties) {
        this.dashScopeService = dashScopeService;
        this.aiProperties = aiProperties;
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

            String systemPrompt = getSystemPrompt(agentType);
            String userMessage = buildUserMessage(request);

            String response = dashScopeService.chat(
                    request.getSessionId(),
                    userMessage,
                    systemPrompt
            );

            log.info("AI响应 - sessionId: {}, response: {}", request.getSessionId(), response);

            return ChatResponse.success(request.getSessionId(), response);

        } catch (Exception e) {
            log.error("聊天处理异常 - sessionId: {}, error: {}", request.getSessionId(), e.getMessage(), e);
            return ChatResponse.error(request.getSessionId(), "AI服务暂时无法响应: " + e.getMessage());
        }
    }

    @Override
    public void clearSession(String sessionId) {
        dashScopeService.clearSession(sessionId);
        log.info("清除会话历史 - sessionId: {}", sessionId);
    }

    private String getSystemPrompt(String agentType) {
        return switch (agentType) {
            case "property" -> SystemPrompt.PROPERTY_ASSISTANT;
            case "customer_service" -> SystemPrompt.CUSTOMER_SERVICE;
            case "data_analysis" -> SystemPrompt.DATA_ANALYSIS;
            default -> SystemPrompt.CUSTOMER_SERVICE;
        };
    }

    private String buildUserMessage(ChatRequest request) {
        return String.format("【用户信息】\n用户名: %s\n用户ID: %s\n\n【用户消息】\n%s",
                request.getDisplayName(),
                request.getUserId() != null ? request.getUserId() : "游客",
                request.getMessage());
    }
}
