package com.msb.hjycommunity.property.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.msb.hjycommunity.common.core.domain.BaseEntity;

/**
 * 审核记录对象 zy_owner_room_record
 */
public class HjyOwnerRoomRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 记录ID */
    @TableId
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long recordId;

    /** 绑定ID */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long ownerRoomId;

    /** 绑定状态 */
    private String roomStatus;

    /** 审核意见 */
    private String recordAuditOpinion;

    /** 审核类型 */
    private String recordAuditType;

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public Long getOwnerRoomId() {
        return ownerRoomId;
    }

    public void setOwnerRoomId(Long ownerRoomId) {
        this.ownerRoomId = ownerRoomId;
    }

    public String getRoomStatus() {
        return roomStatus;
    }

    public void setRoomStatus(String roomStatus) {
        this.roomStatus = roomStatus;
    }

    public String getRecordAuditOpinion() {
        return recordAuditOpinion;
    }

    public void setRecordAuditOpinion(String recordAuditOpinion) {
        this.recordAuditOpinion = recordAuditOpinion;
    }

    public String getRecordAuditType() {
        return recordAuditType;
    }

    public void setRecordAuditType(String recordAuditType) {
        this.recordAuditType = recordAuditType;
    }

    @Override
    public String toString() {
        return "HjyOwnerRoomRecord{" +
                "recordId=" + recordId +
                ", ownerRoomId=" + ownerRoomId +
                ", roomStatus='" + roomStatus + '\'' +
                ", recordAuditType='" + recordAuditType + '\'' +
                '}';
    }
}
