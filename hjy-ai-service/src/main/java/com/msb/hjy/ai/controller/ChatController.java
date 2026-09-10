package com.msb.hjy.ai.controller;

import com.msb.hjy.ai.common.result.Result;
import com.msb.hjy.ai.dto.ChatRequest;
import com.msb.hjy.ai.dto.ChatResponse;
import com.msb.hjy.ai.service.ChatService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * AI 对话控制器 - 提供智能客服对话 API
 * <p>
 * 包含同步对话、流式对话（SSE 打字机效果）、会话清除和健康检查接口。
 * 对话逻辑统一由 ChatService 提供（含空消息/问候/帮助拦截），
 * 控制器只做参数接收与响应包装。前端通过 /ai/* 路径代理访问本服务。
 */
@Slf4j
@RestController
@RequestMapping("/ai")
public class ChatController {

    /** 聊天服务（同步/流式对话、会话清理） */
    private final ChatService chatService;

    /**
     * 构造注入依赖
     */
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
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

        ChatResponse chatResponse = chatService.chat(request);
        return chatResponse.isSuccess()
                ? Result.success("对话成功", chatResponse)
                : Result.error(chatResponse.getErrorMessage());
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

        return chatService.chatStream(request);
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
