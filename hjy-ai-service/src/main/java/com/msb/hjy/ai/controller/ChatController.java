package com.msb.hjy.ai.controller;

import com.msb.hjy.ai.agent.SystemPrompt;
import com.msb.hjy.ai.common.result.Result;
import com.msb.hjy.ai.dto.ChatRequest;
import com.msb.hjy.ai.dto.ChatResponse;
import com.msb.hjy.ai.service.ChatService;
import com.msb.hjy.ai.service.DashScopeService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/ai")
public class ChatController {

    private final ChatService chatService;
    private final DashScopeService dashScopeService;

    public ChatController(ChatService chatService, DashScopeService dashScopeService) {
        this.chatService = chatService;
        this.dashScopeService = dashScopeService;
    }

    @PostMapping("/chat")
    public Result<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        log.info("收到聊天请求 - sessionId: {}, message: {}", 
                request.getSessionId(), request.getMessage());
        
        ChatResponse response = chatService.chat(request);
        
        if (response.isSuccess()) {
            return Result.success("对话成功", response);
        } else {
            return Result.error(response.getErrorMessage());
        }
    }

    @PostMapping("/chat/{agentType}")
    public Result<ChatResponse> chatWithAgent(
            @PathVariable String agentType,
            @Valid @RequestBody ChatRequest request) {
        
        ChatResponse response = chatService.chatWithAgent(request, agentType);
        
        if (response.isSuccess()) {
            return Result.success("对话成功", response);
        } else {
            return Result.error(response.getErrorMessage());
        }
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody ChatRequest request) {
        log.info("收到流式聊天请求 - sessionId: {}, message: {}", 
                request.getSessionId(), request.getMessage());
        
        String systemPrompt = getSystemPrompt(request.getAgentType());
        
        return dashScopeService.chatStream(
                request.getSessionId(),
                request.getMessage(),
                systemPrompt
        ).map(content -> "data: " + content + "\n\n");
    }

    @DeleteMapping("/session/{sessionId}")
    public Result<Void> clearSession(@PathVariable String sessionId) {
        chatService.clearSession(sessionId);
        return Result.success("会话已清除", null);
    }

    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("AI服务运行正常", "OK");
    }

    private String getSystemPrompt(String agentType) {
        return switch (agentType) {
            case "property" -> SystemPrompt.PROPERTY_ASSISTANT;
            case "customer_service" -> SystemPrompt.CUSTOMER_SERVICE;
            case "data_analysis" -> SystemPrompt.DATA_ANALYSIS;
            default -> SystemPrompt.CUSTOMER_SERVICE;
        };
    }
}
