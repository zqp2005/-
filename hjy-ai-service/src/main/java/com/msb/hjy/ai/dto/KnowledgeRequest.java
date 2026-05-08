package com.msb.hjy.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 知识库请求 DTO
 * <p>
 * 用于向知识库添加或查询知识的请求参数封装。
 * 包含分类、标题、内容、来源和优先级等字段。
 */
@Data
public class KnowledgeRequest {

    /** 知识类别 */
    @NotBlank(message = "知识类别不能为空")
    private String category;

    /** 知识标题 */
    private String title;

    /** 知识内容 */
    @NotBlank(message = "知识内容不能为空")
    private String content;

    /** 来源 */
    private String source;

    /** 优先级（数值越大优先级越高） */
    private Integer priority = 0;
}
