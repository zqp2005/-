package com.msb.hjycommunity.property.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.msb.hjycommunity.common.core.domain.BaseEntity;

/**
 * 房间对象 hjy_room
 */
public class HjyRoom extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 房间ID */
    @TableId
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long roomId;

    /** 楼栋ID */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long buildingId;

    /** 单元ID */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long unitId;

    /** 小区ID */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long communityId;

    /** 房间名称 */
    private String roomName;

    /** 房间编码 */
    private String roomCode;

    /** 楼层 */
    private Integer roomLevel;

    /** 建筑面积 */
    private String roomAcreage;

    /** 算费系数 */
    private String roomCost;

    /** 房屋状态 */
    private String roomStatus;

    /** 是否商铺 */
    private String roomIsShop;

    /** 是否商品房 */
    private String roomSCommercialHouse;

    /** 房屋户型 */
    private String roomHouseType;

    /** 楼栋名称 */
    private String buildingName;

    /** 单元名称 */
    private String unitName;

    /** 小区名称 */
    private String communityName;

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
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

    public Long getCommunityId() {
        return communityId;
    }

    public void setCommunityId(Long communityId) {
        this.communityId = communityId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public Integer getRoomLevel() {
        return roomLevel;
    }

    public void setRoomLevel(Integer roomLevel) {
        this.roomLevel = roomLevel;
    }

    public String getRoomAcreage() {
        return roomAcreage;
    }

    public void setRoomAcreage(String roomAcreage) {
        this.roomAcreage = roomAcreage;
    }

    public String getRoomCost() {
        return roomCost;
    }

    public void setRoomCost(String roomCost) {
        this.roomCost = roomCost;
    }

    public String getRoomStatus() {
        return roomStatus;
    }

    public void setRoomStatus(String roomStatus) {
        this.roomStatus = roomStatus;
    }

    public String getRoomIsShop() {
        return roomIsShop;
    }

    public void setRoomIsShop(String roomIsShop) {
        this.roomIsShop = roomIsShop;
    }

    public String getRoomSCommercialHouse() {
        return roomSCommercialHouse;
    }

    public void setRoomSCommercialHouse(String roomSCommercialHouse) {
        this.roomSCommercialHouse = roomSCommercialHouse;
    }

    public String getRoomHouseType() {
        return roomHouseType;
    }

    public void setRoomHouseType(String roomHouseType) {
        this.roomHouseType = roomHouseType;
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

    public String getCommunityName() {
        return communityName;
    }

    public void setCommunityName(String communityName) {
        this.communityName = communityName;
    }

    @Override
    public String toString() {
        return "HjyRoom{" +
                "roomId=" + roomId +
                ", buildingId=" + buildingId +
                ", unitId=" + unitId +
                ", communityId=" + communityId +
                ", roomName='" + roomName + '\'' +
                ", roomCode='" + roomCode + '\'' +
                ", roomLevel=" + roomLevel +
                ", roomStatus='" + roomStatus + '\'' +
                '}';
    }
}
