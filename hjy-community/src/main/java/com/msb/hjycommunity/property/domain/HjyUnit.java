package com.msb.hjycommunity.property.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.msb.hjycommunity.common.core.domain.BaseEntity;

/**
 * 单元对象 hjy_unit
 */
public class HjyUnit extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 单元ID */
    @TableId
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long unitId;

    /** 楼栋ID */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long buildingId;

    /** 小区ID */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long communityId;

    /** 单元名称 */
    private String unitName;

    /** 单元编码 */
    private String unitCode;

    /** 层数 */
    private Integer unitLevel;

    /** 建筑面积 */
    private String unitAcreage;

    /** 是否有电梯 */
    private String unitHaveElevator;

    /** 楼栋名称 */
    private String buildingName;

    /** 小区名称 */
    private String communityName;

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

    public Integer getUnitLevel() {
        return unitLevel;
    }

    public void setUnitLevel(Integer unitLevel) {
        this.unitLevel = unitLevel;
    }

    public String getUnitAcreage() {
        return unitAcreage;
    }

    public void setUnitAcreage(String unitAcreage) {
        this.unitAcreage = unitAcreage;
    }

    public String getUnitHaveElevator() {
        return unitHaveElevator;
    }

    public void setUnitHaveElevator(String unitHaveElevator) {
        this.unitHaveElevator = unitHaveElevator;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public String getCommunityName() {
        return communityName;
    }

    public void setCommunityName(String communityName) {
        this.communityName = communityName;
    }

    @Override
    public String toString() {
        return "HjyUnit{" +
                "unitId=" + unitId +
                ", buildingId=" + buildingId +
                ", communityId=" + communityId +
                ", unitName='" + unitName + '\'' +
                ", unitCode='" + unitCode + '\'' +
                ", unitLevel=" + unitLevel +
                ", unitAcreage='" + unitAcreage + '\'' +
                ", unitHaveElevator='" + unitHaveElevator + '\'' +
                '}';
    }
}
