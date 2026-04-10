package com.msb.hjy.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class KnowledgeRequest {

    @NotBlank(message = "知识类别不能为空")
    private String category;

    private String title;

    @NotBlank(message = "知识内容不能为空")
    private String content;

    private String source;

    private Integer priority = 0;
}
