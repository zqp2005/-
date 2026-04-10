package com.msb.hjy.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRequest {

    @NotBlank(message = "会话ID不能为空")
    private String sessionId;

    @NotBlank(message = "消息内容不能为空")
    private String message;

    private String agentType = "customer_service";

    private Long userId;

    private String userName;

    public String getDisplayName() {
        return userName != null ? userName : "业主";
    }
}
