package com.msb.hjycommunity.property.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.msb.hjycommunity.common.core.domain.BaseEntity;

/**
 * 评论对象 zy_comment
 */
public class HjyComment extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 评论ID */
    @TableId
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long commentId;

    /** 互动ID */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long interactionId;

    /** 业主ID */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long ownerId;

    /** 业主名称 */
    private String ownerName;

    /** 业主头像 */
    private String ownerPortrait;

    /** 被回复人ID */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long passiveOwnerId;

    /** 被回复人名称 */
    private String passiveOwnerName;

    /** 内容 */
    private String content;

    /** 删除标志 */
    private String delFlag;

    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }

    public Long getInteractionId() {
        return interactionId;
    }

    public void setInteractionId(Long interactionId) {
        this.interactionId = interactionId;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerPortrait() {
        return ownerPortrait;
    }

    public void setOwnerPortrait(String ownerPortrait) {
        this.ownerPortrait = ownerPortrait;
    }

    public Long getPassiveOwnerId() {
        return passiveOwnerId;
    }

    public void setPassiveOwnerId(Long passiveOwnerId) {
        this.passiveOwnerId = passiveOwnerId;
    }

    public String getPassiveOwnerName() {
        return passiveOwnerName;
    }

    public void setPassiveOwnerName(String passiveOwnerName) {
        this.passiveOwnerName = passiveOwnerName;
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

    @Override
    public String toString() {
        return "HjyComment{" +
                "commentId=" + commentId +
                ", interactionId=" + interactionId +
                ", ownerName='" + ownerName + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
