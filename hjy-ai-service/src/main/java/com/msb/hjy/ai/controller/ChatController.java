package com.msb.hjy.ai.controller;

import com.msb.hjy.ai.common.result.Result;
import com.msb.hjy.ai.dto.ChatRequest;
import com.msb.hjy.ai.dto.ChatResponse;
import com.msb.hjy.ai.service.ChatService;
import com.msb.hjy.ai.tools.*;
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
    private final ChatService chatService;
    private final RepairTool repairTool;
    private final ComplaintTool complaintTool;
    private final PropertyFeeTool propertyFeeTool;
    private final OwnerInfoTool ownerInfoTool;
    private final AnnouncementTool announcementTool;
    private final CommunityTool communityTool;

    public ChatController(ChatClient chatClient, ChatMemory chatMemory, ChatService chatService,
                          RepairTool repairTool, ComplaintTool complaintTool,
                          PropertyFeeTool propertyFeeTool, OwnerInfoTool ownerInfoTool,
                          AnnouncementTool announcementTool, CommunityTool communityTool) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
        this.chatService = chatService;
        this.repairTool = repairTool;
        this.complaintTool = complaintTool;
        this.propertyFeeTool = propertyFeeTool;
        this.ownerInfoTool = ownerInfoTool;
        this.announcementTool = announcementTool;
        this.communityTool = communityTool;
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
            return Result.error("AI服务暂时无法响应，请稍后重试。");
        }
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@Valid @RequestBody ChatRequest request) {
        log.info("收到流式聊天请求 - sessionId: {}, message: {}",
                request.getSessionId(), request.getMessage());

        return chatClient.prompt()
                .user(request.getMessage())
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, request.getSessionId()))
                .tools(repairTool, complaintTool, propertyFeeTool, ownerInfoTool, announcementTool, communityTool)
                .stream()
                .content()
                .map(content -> "data: " + content + "\n\n")
                .onErrorResume(e -> {
                    log.error("流式对话异常: {}", e.getMessage(), e);
                    return Flux.just("data: AI服务暂时无法响应，请稍后重试。\n\n");
                });
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
}