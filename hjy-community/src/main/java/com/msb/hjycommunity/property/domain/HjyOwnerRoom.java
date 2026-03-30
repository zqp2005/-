package com.msb.hjycommunity.property.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.msb.hjycommunity.common.core.domain.BaseEntity;

/**
 * 房屋绑定对象 hjy_owner_room
 */
public class HjyOwnerRoom extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 绑定ID */
    @TableId
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long ownerRoomId;

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

    /** 小区名称 */
    private String communityName;

    /** 楼栋名称 */
    private String buildingName;

    /** 单元名称 */
    private String unitName;

    /** 房间名称 */
    private String roomName;

    /** 业主姓名 */
    private String ownerRealName;

    /** 业主手机号 */
    private String ownerPhoneNumber;

    public Long getOwnerRoomId() {
        return ownerRoomId;
    }

    public void setOwnerRoomId(Long ownerRoomId) {
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

    public String getCommunityName() {
        return communityName;
    }

    public void setCommunityName(String communityName) {
        this.communityName = communityName;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
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

    @Override
    public String toString() {
        return "HjyOwnerRoom{" +
                "ownerRoomId=" + ownerRoomId +
                ", communityId=" + communityId +
                ", roomId=" + roomId +
                ", ownerId=" + ownerId +
                ", roomStatus='" + roomStatus + '\'' +
                '}';
    }
}
