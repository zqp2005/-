package com.msb.hjycommunity.property.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.msb.hjycommunity.common.core.domain.BaseEntity;

/**
 * 社区互动对象 hjy_interaction
 */
public class HjyInteraction extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 互动ID */
    @TableId
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long interactionId;

    /** 小区ID */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long communityId;

    /** 用户ID */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long userId;

    /** 内容 */
    private String content;

    /** 删除标志 */
    private String delFlag;

    /** 小区名称 */
    private String communityName;

    public Long getInteractionId() {
        return interactionId;
    }

    public void setInteractionId(Long interactionId) {
        this.interactionId = interactionId;
    }

    public Long getCommunityId() {
        return communityId;
    }

    public void setCommunityId(Long communityId) {
        this.communityId = communityId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public String getCommunityName() {
        return communityName;
    }

    public void setCommunityName(String communityName) {
        this.communityName = communityName;
    }

    @Override
    public String toString() {
        return "HjyInteraction{" +
                "interactionId=" + interactionId +
                ", communityId=" + communityId +
                ", userId=" + userId +
                ", content='" + content + '\'' +
                '}';
    }
}
