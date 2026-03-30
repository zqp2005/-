package com.msb.hjycommunity.property.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.msb.hjycommunity.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 报修对象 hjy_repair
 */
public class HjyRepair extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 报修ID */
    @TableId
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long repairId;

    /** 报修编号 */
    private String repairNum;

    /** 报修状态 */
    private String repairState;

    /** 业主姓名 */
    private String ownerRealName;

    /** 业主手机号 */
    private String ownerPhoneNumber;

    /** 报修内容 */
    private String repairContent;

    /** 详细地址 */
    private String address;

    /** 期待上门时间 */
    private Date doorTime;

    /** 派单时间 */
    private Date assignmentTime;

    /** 分派人ID */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long assignmentId;

    /** 接单时间 */
    private Date receivingOrdersTime;

    /** 处理人ID */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long completeId;

    /** 处理人姓名 */
    private String completeName;

    /** 处理人电话 */
    private String completePhone;

    /** 处理完成时间 */
    private Date completeTime;

    /** 取消时间 */
    private Date cancelTime;

    /** 小区ID */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long communityId;

    /** 小区名称 */
    private String communityName;

    public Long getRepairId() {
        return repairId;
    }

    public void setRepairId(Long repairId) {
        this.repairId = repairId;
    }

    public String getRepairNum() {
        return repairNum;
    }

    public void setRepairNum(String repairNum) {
        this.repairNum = repairNum;
    }

    public String getRepairState() {
        return repairState;
    }

    public void setRepairState(String repairState) {
        this.repairState = repairState;
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

    public Date getDoorTime() {
        return doorTime;
    }

    public void setDoorTime(Date doorTime) {
        this.doorTime = doorTime;
    }

    public Date getAssignmentTime() {
        return assignmentTime;
    }

    public void setAssignmentTime(Date assignmentTime) {
        this.assignmentTime = assignmentTime;
    }

    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public Date getReceivingOrdersTime() {
        return receivingOrdersTime;
    }

    public void setReceivingOrdersTime(Date receivingOrdersTime) {
        this.receivingOrdersTime = receivingOrdersTime;
    }

    public Long getCompleteId() {
        return completeId;
    }

    public void setCompleteId(Long completeId) {
        this.completeId = completeId;
    }

    public String getCompleteName() {
        return completeName;
    }

    public void setCompleteName(String completeName) {
        this.completeName = completeName;
    }

    public String getCompletePhone() {
        return completePhone;
    }

    public void setCompletePhone(String completePhone) {
        this.completePhone = completePhone;
    }

    public Date getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(Date completeTime) {
        this.completeTime = completeTime;
    }

    public Date getCancelTime() {
        return cancelTime;
    }

    public void setCancelTime(Date cancelTime) {
        this.cancelTime = cancelTime;
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
        return "HjyRepair{" +
                "repairId=" + repairId +
                ", repairNum='" + repairNum + '\'' +
                ", repairState='" + repairState + '\'' +
                ", ownerRealName='" + ownerRealName + '\'' +
                '}';
    }
}
