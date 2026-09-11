package com.msb.hjycommunity.property.domain.dto;

import java.io.Serializable;

/**
 * 业主端提交报修请求体（POST /app/repair）
 *
 * @author hjy
 * @date 2026/9/11
 **/
public class AppRepairRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 报修内容（必填） */
    private String repairContent;

    /** 详细地址（选填） */
    private String address;

    /** 小区ID（选填） */
    private Long communityId;

    public String getRepairContent() {
        return repairContent;
    }

    public void setRepairContent(String repairContent) {
        this.repairContent = repairContent;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Long getCommunityId() {
        return communityId;
    }

    public void setCommunityId(Long communityId) {
        this.communityId = communityId;
    }
}
