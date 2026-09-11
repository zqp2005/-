package com.msb.hjycommunity.property.domain.dto;

import java.io.Serializable;

/**
 * 业主端注册请求体（POST /app/register）
 *
 * @author hjy
 * @date 2026/9/11
 **/
public class AppRegisterRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 手机号（必填，作为登录账号） */
    private String phone;

    /** 密码（必填，≥6位） */
    private String password;

    /** 真实姓名（必填） */
    private String realName;

    /** 身份证号（选填） */
    private String idCard;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }
}
