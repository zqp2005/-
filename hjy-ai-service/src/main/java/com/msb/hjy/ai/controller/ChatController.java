package com.msb.hjy.ai.controller;

import com.msb.hjy.ai.common.result.Result;
import com.msb.hjy.ai.dto.ChatRequest;
import com.msb.hjy.ai.dto.ChatResponse;
import com.msb.hjy.ai.tools.RepairTool;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/ai")
public class ChatController {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final RepairTool repairTool;

    public ChatController(ChatClient chatClient, ChatMemory chatMemory, RepairTool repairTool) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
        this.repairTool = repairTool;
    }

    @PostMapping("/chat")
    public Result<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        log.info("收到聊天请求 - sessionId: {}, message: {}", 
                request.getSessionId(), request.getMessage());

        try {
            String response = chatClient.prompt()
                    .user(request.getMessage())
                    .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, request.getSessionId()))
                    .call()
                    .content();

            ChatResponse chatResponse = ChatResponse.builder()
                    .sessionId(request.getSessionId())
                    .content(response)
                    .success(true)
                    .build();

            return Result.success("对话成功", chatResponse);
        } catch (Exception e) {
            log.error("AI对话失败: {}", e.getMessage(), e);
            return Result.error("AI服务异常: " + e.getMessage());
        }
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody ChatRequest request) {
        log.info("收到流式聊天请求 - sessionId: {}, message: {}", 
                request.getSessionId(), request.getMessage());
        
        return chatClient.prompt()
                .user(request.getMessage())
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, request.getSessionId()))
                .tools(repairTool)
                .stream()
                .content()
                .map(content -> "data: " + content + "\n\n");
    }

    @DeleteMapping("/session/{sessionId}")
    public Result<Void> clearSession(@PathVariable String sessionId) {
        chatMemory.clear(sessionId);
        log.info("清除会话历史 - sessionId: {}", sessionId);
        return Result.success("会话已清除", null);
    }

    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("AI服务运行正常", "OK");
    }
}