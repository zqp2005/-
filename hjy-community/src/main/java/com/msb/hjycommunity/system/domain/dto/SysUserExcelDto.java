package com.msb.hjycommunity.system.domain.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户导出Excel实体（不含密码等敏感字段）
 */
@Data
@ExcelTarget("userExcel")
public class SysUserExcelDto implements Serializable {

    @Excel(name = "用户编号")
    private Long userId;

    @Excel(name = "登录账号")
    private String userName;

    @Excel(name = "用户昵称")
    private String nickName;

    @Excel(name = "邮箱")
    private String email;

    @Excel(name = "手机号")
    private String phonenumber;

    @Excel(name = "性别", replace = {"男_0", "女_1", "未知_2"})
    private String sex;

    @Excel(name = "状态", replace = {"正常_0", "停用_1"})
    private String status;

    @Excel(name = "最后登录IP")
    private String loginIp;

    @Excel(name = "最后登录时间", exportFormat = "yyyy-MM-dd HH:mm:ss")
    private Date loginDate;

    @Excel(name = "创建时间", exportFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
