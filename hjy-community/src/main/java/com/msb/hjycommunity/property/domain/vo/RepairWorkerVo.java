package com.msb.hjycommunity.property.domain.vo;

/** 派单下拉只输出人员选择所需字段，不返回电话、密码、邮箱。 */
public class RepairWorkerVo {
    private String userId;
    private String userName;
    private String nickName;
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }
}
