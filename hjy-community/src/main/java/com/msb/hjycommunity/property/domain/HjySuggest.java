package com.msb.hjycommunity.property.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.msb.hjycommunity.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 投诉建议对象 hjy_suggest
 */
public class HjySuggest extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 投诉建议ID */
    @TableId
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long complaintSuggestId;

    /** 类型 */
    private String complaintSuggestType;

    /** 内容 */
    private String complaintSuggestContent;

    /** 业主姓名 */
    private String ownerRealName;

    /** 业主手机号 */
    private String ownerPhoneNumber;

    /** 图片JSON数组 */
    private String urlList;

    /** 投诉状态 */
    private String complaintState;

    /** 处理人 */
    private String handleBy;

    /** 处理时间 */
    private Date handleTime;

    /** 回复内容 */
    private String replyContent;

    public Long getComplaintSuggestId() {
        return complaintSuggestId;
    }

    public void setComplaintSuggestId(Long complaintSuggestId) {
        this.complaintSuggestId = complaintSuggestId;
    }

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

    public String getOwnerRealName() {
        return ownerRealName;
    }

    public void setOwnerRealName(String ownerRealName) {
        this.ownerRealName = ownerRealName;
    }

    public String getOwnerPhoneNumber() {
        return ownerPhoneNumber;
    }

    public void setOwnerPhoneNumber(String ownerPhoneNumber) {
        this.ownerPhoneNumber = ownerPhoneNumber;
    }

    public String getUrlList() {
        return urlList;
    }

    public void setUrlList(String urlList) {
        this.urlList = urlList;
    }

    public String getComplaintState() {
        return complaintState;
    }

    public void setComplaintState(String complaintState) {
        this.complaintState = complaintState;
    }

    public String getHandleBy() {
        return handleBy;
    }

    public void setHandleBy(String handleBy) {
        this.handleBy = handleBy;
    }

    public Date getHandleTime() {
        return handleTime;
    }

    public void setHandleTime(Date handleTime) {
        this.handleTime = handleTime;
    }

    public String getReplyContent() {
        return replyContent;
    }

    public void setReplyContent(String replyContent) {
        this.replyContent = replyContent;
    }

    @Override
    public String toString() {
        return "HjySuggest{" +
                "complaintSuggestId=" + complaintSuggestId +
                ", complaintSuggestType='" + complaintSuggestType + '\'' +
                ", ownerRealName='" + ownerRealName + '\'' +
                '}';
    }
}
