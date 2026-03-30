package com.msb.hjycommunity.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.msb.hjycommunity.common.core.domain.BaseEntity;

/**
 * 通知公告对象 sys_notice
 */
public class SysNotice extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 公告ID */
    @TableId
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long noticeId;

    /** 公告标题 */
    private String noticeTitle;

    /** 公告类型 */
    private String noticeType;

    /** 公告内容 */
    private String noticeContent;

    /** 公告状态 */
    private String status;

    public Long getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(Long noticeId) {
        this.noticeId = noticeId;
    }

    public String getNoticeTitle() {
        return noticeTitle;
    }

    public void setNoticeTitle(String noticeTitle) {
        this.noticeTitle = noticeTitle;
    }

    public String getNoticeType() {
        return noticeType;
    }

    public void setNoticeType(String noticeType) {
        this.noticeType = noticeType;
    }

    public String getNoticeContent() {
        return noticeContent;
    }

    public void setNoticeContent(String noticeContent) {
        this.noticeContent = noticeContent;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "SysNotice{" +
                "noticeId=" + noticeId +
                ", noticeTitle='" + noticeTitle + '\'' +
                ", noticeType='" + noticeType + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
