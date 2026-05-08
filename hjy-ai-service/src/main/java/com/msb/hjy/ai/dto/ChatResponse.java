package com.msb.hjy.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 聊天响应 DTO
 * <p>
 * 封装 AI 对话的返回结果，包含会话标识、回复内容、时间戳、
 * 调用的工具列表等信息。提供便捷的静态工厂方法 success() 和 error()。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    /** 会话 ID */
    private String sessionId;

    /** AI 回复内容 */
    private String content;

    /** 代理类型 */
    private String agentType;

    /** 响应时间戳 */
    private LocalDateTime timestamp;

    /** 消息唯一标识 */
    private String messageId;

    /** 本次对话调用的工具列表 */
    private List<Map<String, String>> toolsCalled;

    /** 参考内容来源 */
    private String referenceContent;

    /** 是否成功 */
    private boolean success;

    /** 错误消息（失败时填充） */
    private String errorMessage;

    /** 快速创建成功响应 */
    public static ChatResponse success(String sessionId, String content) {
        return ChatResponse.builder()
                .sessionId(sessionId)
                .content(content)
                .timestamp(LocalDateTime.now())
                .success(true)
                .build();
    }

    /** 快速创建失败响应 */
    public static ChatResponse error(String sessionId, String errorMessage) {
        return ChatResponse.builder()
                .sessionId(sessionId)
                .errorMessage(errorMessage)
                .timestamp(LocalDateTime.now())
                .success(false)
                .build();
    }
}
