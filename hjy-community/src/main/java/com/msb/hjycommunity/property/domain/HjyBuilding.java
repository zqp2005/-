package com.msb.hjycommunity.property.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.msb.hjycommunity.common.core.domain.BaseEntity;

/**
 * 楼栋对象 hjy_building
 */
public class HjyBuilding extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 楼栋ID */
    @TableId
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long buildingId;

    /** 楼栋名称 */
    private String buildingName;

    /** 楼栋编码 */
    private String buildingCode;

    /** 楼栋面积 */
    private String buildingAcreage;

    /** 小区ID */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long communityId;

    /** 小区名称 */
    private String communityName;

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

    public String getBuildingAcreage() {
        return buildingAcreage;
    }

    public void setBuildingAcreage(String buildingAcreage) {
        this.buildingAcreage = buildingAcreage;
    }

    public Long getCommunityId() {
        return communityId;
    }

    public void setCommunityId(Long communityId) {
        this.communityId = communityId;
    }

    public String getCommunityName() {
        return communityName;
    }

    public void setCommunityName(String communityName) {
        this.communityName = communityName;
    }

    @Override
    public String toString() {
        return "HjyBuilding{" +
                "buildingId=" + buildingId +
                ", buildingName='" + buildingName + '\'' +
                ", buildingCode='" + buildingCode + '\'' +
                ", buildingAcreage='" + buildingAcreage + '\'' +
                ", communityId=" + communityId +
                ", communityName='" + communityName + '\'' +
                '}';
    }
}
