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

    /** 业主ID */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long ownerId;

    /** 业主昵称 */
    private String ownerNickname;

    /** 业主姓名 */
    private String ownerRealName;

    /** 业主手机号 */
    private String ownerPhoneNumber;

    /** 内容 */
    private String content;

    /** 图片ID集 */
    private String filesId;

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

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerNickname() {
        return ownerNickname;
    }

    public void setOwnerNickname(String ownerNickname) {
        this.ownerNickname = ownerNickname;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFilesId() {
        return filesId;
    }

    public void setFilesId(String filesId) {
        this.filesId = filesId;
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
                ", ownerNickname='" + ownerNickname + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
