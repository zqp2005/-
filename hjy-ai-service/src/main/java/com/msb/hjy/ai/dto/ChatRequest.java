package com.msb.hjy.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 聊天请求 DTO
 * <p>
 * 封装前端发起的对话请求参数，包含会话标识、消息内容、用户信息和代理类型。
 * 使用 Jakarta Validation 注解进行参数校验。
 */
@Data
public class ChatRequest {

    /** 会话 ID，用于区分不同用户的对话上下文 */
    @NotBlank(message = "会话ID不能为空")
    private String sessionId;

    /** 用户发送的消息内容 */
    @NotBlank(message = "消息内容不能为空")
    private String message;

    /** 代理类型：customer_service（客服）、property（物业）、data_analysis（数据） */
    private String agentType = "customer_service";

    /** 用户 ID */
    private Long userId;

    /** 用户姓名 */
    private String userName;

    /** 获取显示名称，未设置时默认显示"业主" */
    public String getDisplayName() {
        return userName != null ? userName : "业主";
    }
}
