package com.msb.hjycommunity.property.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 楼栋下拉VO
 */
public class HjyBuildingVo {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long buildingId;

    private String buildingName;

    private String buildingCode;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long communityId;

    public Long getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(Long buildingId) {
        this.buildingId = buildingId;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public String getBuildingCode() {
        return buildingCode;
    }

    public void setBuildingCode(String buildingCode) {
        this.buildingCode = buildingCode;
    }

    public Long getCommunityId() {
        return communityId;
    }

    public void setCommunityId(Long communityId) {
        this.communityId = communityId;
    }
}
