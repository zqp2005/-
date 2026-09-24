package com.msb.hjy.ai.controller;

import com.msb.hjy.ai.common.result.Result;
import com.msb.hjy.ai.config.JwtAuthFilter;
import com.msb.hjy.ai.dto.ChatRequest;
import com.msb.hjy.ai.dto.ChatResponse;
import com.msb.hjy.ai.service.ChatService;
import jakarta.servlet.http.HttpServletRequest;
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
     * @param servletRequest servlet 请求，携带鉴权过滤器写入的用户身份
     * @return 统一响应，data 中携带 ChatResponse
     */
    @PostMapping("/chat")
    public Result<ChatResponse> chat(@Valid @RequestBody ChatRequest request, HttpServletRequest servletRequest) {
        fillUserIdentity(request, servletRequest);
        log.info("收到聊天请求 - userId: {}, sessionId: {}, message: {}",
                request.getUserId(), request.getSessionId(), request.getMessage());

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
     * @param servletRequest servlet 请求，携带鉴权过滤器写入的用户身份
     * @return Flux 流式响应，每段内容以 "data: " 开头
     */
    @PostMapping(value = "/chat/stream", produces = "text/event-stream;charset=UTF-8")
    public Flux<org.springframework.http.codec.ServerSentEvent<String>> chatStream(@Valid @RequestBody ChatRequest request, HttpServletRequest servletRequest) {
        fillUserIdentity(request, servletRequest);
        log.info("收到流式聊天请求 - userId: {}, sessionId: {}, message: {}",
                request.getUserId(), request.getSessionId(), request.getMessage());

        return chatService.chatStream(request).map(content -> org.springframework.http.codec.ServerSentEvent.builder(content).build());
    }

    /**
     * 清除会话历史（按用户隔离，实际清除 userId:sessionId 对应的会话）
     *
     * @param sessionId 会话 ID
     * @param servletRequest servlet 请求，携带鉴权过滤器写入的用户身份
     */
    @DeleteMapping("/session/{sessionId}")
    public Result<Void> clearSession(@PathVariable String sessionId, HttpServletRequest servletRequest) {
        chatService.clearSession(conversationId(sessionId, servletRequest));
        return Result.success("会话已清除", null);
    }

    /**
     * 把鉴权过滤器（JwtAuthFilter）写入 request attribute 的用户身份，
     * 覆盖到请求体上，防止伪造请求体中的用户信息冒充他人
     */
    private void fillUserIdentity(ChatRequest request, HttpServletRequest servletRequest) {
        request.setCallerAuthorization(servletRequest.getHeader("Authorization"));
        Object userId = servletRequest.getAttribute(JwtAuthFilter.ATTR_USER_ID);
        if (userId instanceof Long) {
            request.setUserId((Long) userId);
        }
        Object userName = servletRequest.getAttribute(JwtAuthFilter.ATTR_USER_NAME);
        if (userName instanceof String) {
            request.setUserName((String) userName);
        }
    }

    /**
     * 会话 key 加上用户 ID 前缀，与 ChatServiceImpl 的会话隔离规则保持一致
     */
    private String conversationId(String sessionId, HttpServletRequest servletRequest) {
        Object userId = servletRequest.getAttribute(JwtAuthFilter.ATTR_USER_ID);
        return userId + ":" + sessionId;
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("AI服务运行正常", "OK");
    }
}
