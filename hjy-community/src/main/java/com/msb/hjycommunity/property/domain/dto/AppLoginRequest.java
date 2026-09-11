package com.msb.hjycommunity.property.domain.dto;

import java.io.Serializable;

/**
 * 业主端登录请求体（POST /app/login）
 *
 * @author hjy
 * @date 2026/9/11
 **/
public class AppLoginRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 手机号 */
    private String phone;

    /** 密码 */
    private String password;

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
}
