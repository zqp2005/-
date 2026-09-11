package com.msb.hjycommunity.framework.security.domain;

import java.io.Serializable;

/**
 * 业主端（小程序 /app 接口）登录态对象
 * <p>
 * 登录成功后以 {@code owner_tokens:{uuid}} 存入 Redis，JWT 中只携带 uuid（claim: owner_user_key），
 * 与管理端 LoginUser/login_tokens 完全隔离。
 *
 * @author hjy
 * @date 2026/9/11
 **/
public class AppOwnerToken implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 登录态唯一标识（uuid，同时也是 Redis key 后缀） */
    private String token;

    /** 业主ID */
    private Long ownerId;

    /** 业主手机号 */
    private String phone;

    /** 业主真实姓名 */
    private String realName;

    /** 登录时间（毫秒） */
    private Long loginTime;

    /** 过期时间（毫秒） */
    private Long expireTime;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public Long getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Long loginTime) {
        this.loginTime = loginTime;
    }

    public Long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Long expireTime) {
        this.expireTime = expireTime;
    }
}
