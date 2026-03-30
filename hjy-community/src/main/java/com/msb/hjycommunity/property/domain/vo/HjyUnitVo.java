package com.msb.hjycommunity.property.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 单元下拉VO
 */
public class HjyUnitVo {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long unitId;

    private String unitName;

    private String unitCode;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long buildingId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long communityId;

    public Long getUnitId() {
        return unitId;
    }

    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public String getUnitCode() {
        return unitCode;
    }

    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
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
