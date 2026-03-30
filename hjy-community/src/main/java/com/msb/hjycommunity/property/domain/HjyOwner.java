package com.msb.hjycommunity.property.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.msb.hjycommunity.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 业主对象 hjy_owner
 */
public class HjyOwner extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 业主ID */
    @TableId
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long ownerId;

    /** 业主昵称 */
    private String ownerNickname;

    /** 业主真实姓名 */
    private String ownerRealName;

    /** 业主性别 */
    private String ownerGender;

    /** 业主年龄 */
    private Integer ownerAge;

    /** 身份证号 */
    private String ownerIdCard;

    /** 手机号 */
    private String ownerPhoneNumber;

    /** 微信OpenId */
    private String ownerOpenId;

    /** 微信ID */
    private String ownerWechatId;

    /** QQ号 */
    private String ownerQqNumber;

    /** 生日 */
    private Date ownerBirthday;

    /** 头像 */
    private String ownerPortrait;

    /** 个性签名 */
    private String ownerSignature;

    /** 状态 */
    private String ownerStatus;

    /** 注册方式 */
    private String ownerLogonMode;

    /** 业主类型 */
    private String ownerType;

    /** 密码 */
    private String ownerPassword;

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerNickname() {
        return ownerNickname;
    }

    public void setOwnerNickname(String ownerNickname) {
        this.ownerNickname = ownerNickname;
    }

    public String getOwnerRealName() {
        return ownerRealName;
    }

    public void setOwnerRealName(String ownerRealName) {
        this.ownerRealName = ownerRealName;
    }

    public String getOwnerGender() {
        return ownerGender;
    }

    public void setOwnerGender(String ownerGender) {
        this.ownerGender = ownerGender;
    }

    public Integer getOwnerAge() {
        return ownerAge;
    }

    public void setOwnerAge(Integer ownerAge) {
        this.ownerAge = ownerAge;
    }

    public String getOwnerIdCard() {
        return ownerIdCard;
    }

    public void setOwnerIdCard(String ownerIdCard) {
        this.ownerIdCard = ownerIdCard;
    }

    public String getOwnerPhoneNumber() {
        return ownerPhoneNumber;
    }

    public void setOwnerPhoneNumber(String ownerPhoneNumber) {
        this.ownerPhoneNumber = ownerPhoneNumber;
    }

    public String getOwnerOpenId() {
        return ownerOpenId;
    }

    public void setOwnerOpenId(String ownerOpenId) {
        this.ownerOpenId = ownerOpenId;
    }

    public String getOwnerWechatId() {
        return ownerWechatId;
    }

    public void setOwnerWechatId(String ownerWechatId) {
        this.ownerWechatId = ownerWechatId;
    }

    public String getOwnerQqNumber() {
        return ownerQqNumber;
    }

    public void setOwnerQqNumber(String ownerQqNumber) {
        this.ownerQqNumber = ownerQqNumber;
    }

    public Date getOwnerBirthday() {
        return ownerBirthday;
    }

    public void setOwnerBirthday(Date ownerBirthday) {
        this.ownerBirthday = ownerBirthday;
    }

    public String getOwnerPortrait() {
        return ownerPortrait;
    }

    public void setOwnerPortrait(String ownerPortrait) {
        this.ownerPortrait = ownerPortrait;
    }

    public String getOwnerSignature() {
        return ownerSignature;
    }

    public void setOwnerSignature(String ownerSignature) {
        this.ownerSignature = ownerSignature;
    }

    public String getOwnerStatus() {
        return ownerStatus;
    }

    public void setOwnerStatus(String ownerStatus) {
        this.ownerStatus = ownerStatus;
    }

    public String getOwnerLogonMode() {
        return ownerLogonMode;
    }

    public void setOwnerLogonMode(String ownerLogonMode) {
        this.ownerLogonMode = ownerLogonMode;
    }

    public String getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(String ownerType) {
        this.ownerType = ownerType;
    }

    public String getOwnerPassword() {
        return ownerPassword;
    }

    public void setOwnerPassword(String ownerPassword) {
        this.ownerPassword = ownerPassword;
    }

    @Override
    public String toString() {
        return "HjyOwner{" +
                "ownerId=" + ownerId +
                ", ownerNickname='" + ownerNickname + '\'' +
                ", ownerRealName='" + ownerRealName + '\'' +
                ", ownerPhoneNumber='" + ownerPhoneNumber + '\'' +
                ", ownerStatus='" + ownerStatus + '\'' +
                '}';
    }
}
