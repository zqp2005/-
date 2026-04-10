package com.msb.hjy.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    private String sessionId;

    private String content;

    private String agentType;

    private LocalDateTime timestamp;

    private String messageId;

    private List<Map<String, String>> toolsCalled;

    private String referenceContent;

    private boolean success;

    private String errorMessage;

    public static ChatResponse success(String sessionId, String content) {
        return ChatResponse.builder()
                .sessionId(sessionId)
                .content(content)
                .timestamp(LocalDateTime.now())
                .success(true)
                .build();
    }

    public static ChatResponse error(String sessionId, String errorMessage) {
        return ChatResponse.builder()
                .sessionId(sessionId)
                .errorMessage(errorMessage)
                .timestamp(LocalDateTime.now())
                .success(false)
                .build();
    }
}
