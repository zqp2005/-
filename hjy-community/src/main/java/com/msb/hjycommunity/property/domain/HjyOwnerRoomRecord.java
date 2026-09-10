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

    /** 绑定ID（数据库 varchar） */
    private String ownerRoomId;

    /** 小区ID */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long communityId;

    /** 楼栋ID */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long buildingId;

    /** 单元ID */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long unitId;

    /** 房间ID */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long roomId;

    /** 业主ID */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long ownerId;

    /** 业主类型 */
    private String ownerType;

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

    public String getOwnerRoomId() {
        return ownerRoomId;
    }

    public void setOwnerRoomId(String ownerRoomId) {
        this.ownerRoomId = ownerRoomId;
    }

    public Long getCommunityId() {
        return communityId;
    }

    public void setCommunityId(Long communityId) {
        this.communityId = communityId;
    }

    public Long getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(Long buildingId) {
        this.buildingId = buildingId;
    }

    public Long getUnitId() {
        return unitId;
    }

    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(String ownerType) {
        this.ownerType = ownerType;
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
                ", roomId=" + roomId +
                ", ownerId=" + ownerId +
                ", roomStatus='" + roomStatus + '\'' +
                ", recordAuditType='" + recordAuditType + '\'' +
                '}';
    }
}
