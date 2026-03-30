package com.msb.hjycommunity.property.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 房间下拉VO
 */
public class HjyRoomVo {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long roomId;

    private String roomName;

    private String roomCode;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long unitId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long buildingId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long communityId;

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
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

    public Long getUnitId() {
        return unitId;
    }

    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }

    public Long getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(Long buildingId) {
        this.buildingId = buildingId;
    }

    public Long getCommunityId() {
        return communityId;
    }

    public void setCommunityId(Long communityId) {
        this.communityId = communityId;
    }
}
