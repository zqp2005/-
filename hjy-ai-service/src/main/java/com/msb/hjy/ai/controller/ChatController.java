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

/**
 * AI 对话控制器 - 提供智能客服对话 API
 * <p>
 * 包含同步对话、流式对话（SSE 打字机效果）、会话清除和健康检查接口。
 * 前端通过 /ai/* 路径代理访问本服务。
 */
@Slf4j
@RestController
@RequestMapping("/ai")
public class ChatController {

    /** Spring AI ChatClient */
    private final ChatClient chatClient;
    /** 聊天记忆管理器 */
    private final ChatMemory chatMemory;
    /** 聊天服务（处理会话清理等逻辑） */
    private final ChatService chatService;
    /** 报修工具 */
    private final RepairTool repairTool;
    /** 投诉工具 */
    private final ComplaintTool complaintTool;
    /** 物业费工具 */
    private final PropertyFeeTool propertyFeeTool;
    /** 业主信息工具 */
    private final OwnerInfoTool ownerInfoTool;
    /** 公告工具 */
    private final AnnouncementTool announcementTool;
    /** 社区信息工具 */
    private final CommunityTool communityTool;

    /**
     * 构造注入依赖
     */
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

    /**
     * 同步对话接口
     * <p>
     * 接收用户消息并返回 AI 完整回复结果。
     * AI 会根据意图自动调用相应工具获取真实数据。
     *
     * @param request 聊天请求体（含 sessionId 和 message）
     * @return 统一响应，data 中携带 ChatResponse
     */
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

    /**
     * 流式对话接口（SSE）
     * <p>
     * 使用 Server-Sent Events 实现打字机效果逐字输出。
     * 前端可通过 EventSource 或 fetch 流式读取回复内容。
     *
     * @param request 聊天请求体
     * @return Flux 流式响应，每段内容以 "data: " 开头
     */
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

    /**
     * 清除会话历史
     *
     * @param sessionId 会话 ID
     */
    @DeleteMapping("/session/{sessionId}")
    public Result<Void> clearSession(@PathVariable String sessionId) {
        chatService.clearSession(sessionId);
        return Result.success("会话已清除", null);
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("AI服务运行正常", "OK");
    }
}