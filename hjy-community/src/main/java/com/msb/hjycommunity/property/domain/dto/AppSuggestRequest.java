package com.msb.hjycommunity.property.domain.dto;

import java.io.Serializable;

/**
 * 业主端提交投诉/建议请求体（POST /app/suggest）
 *
 * @author hjy
 * @date 2026/9/11
 **/
public class AppSuggestRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 类型：Complaint（投诉）/ Suggest（建议） */
    private String complaintSuggestType;

    /** 内容（必填） */
    private String complaintSuggestContent;

    public String getComplaintSuggestType() {
        return complaintSuggestType;
    }

    public void setComplaintSuggestType(String complaintSuggestType) {
        this.complaintSuggestType = complaintSuggestType;
    }

    public String getComplaintSuggestContent() {
        return complaintSuggestContent;
    }

    public void setComplaintSuggestContent(String complaintSuggestContent) {
        this.complaintSuggestContent = complaintSuggestContent;
    }
}
