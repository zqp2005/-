package com.msb.hjycommunity.property.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.msb.hjycommunity.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 访客对象 hjy_visitor
 */
public class HjyVisitor extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 访客ID */
    @TableId
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long visitorId;

    /** 访客姓名 */
    private String visitorName;

    /** 访客手机号 */
    private String visitorPhoneNumber;

    /** 到访时间 */
    private Date visitorDate;

    /** 创建人ID */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long createById;

    /** 创建人OpenId */
    private String createByOpenId;

    /** 小区ID */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long communityId;

    /** 小区名称 */
    private String communityName;

    public Long getVisitorId() {
        return visitorId;
    }

    public void setVisitorId(Long visitorId) {
        this.visitorId = visitorId;
    }

    public String getVisitorName() {
        return visitorName;
    }

    public void setVisitorName(String visitorName) {
        this.visitorName = visitorName;
    }

    public String getVisitorPhoneNumber() {
        return visitorPhoneNumber;
    }

    public void setVisitorPhoneNumber(String visitorPhoneNumber) {
        this.visitorPhoneNumber = visitorPhoneNumber;
    }

    public Date getVisitorDate() {
        return visitorDate;
    }

    public void setVisitorDate(Date visitorDate) {
        this.visitorDate = visitorDate;
    }

    public Long getCreateById() {
        return createById;
    }

    public void setCreateById(Long createById) {
        this.createById = createById;
    }

    public String getCreateByOpenId() {
        return createByOpenId;
    }

    public void setCreateByOpenId(String createByOpenId) {
        this.createByOpenId = createByOpenId;
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
        return "HjyVisitor{" +
                "visitorId=" + visitorId +
                ", visitorName='" + visitorName + '\'' +
                ", visitorPhoneNumber='" + visitorPhoneNumber + '\'' +
                ", communityId=" + communityId +
                '}';
    }
}
