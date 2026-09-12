package com.msb.hjycommunity.monitor.domain.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 登录日志导出Excel实体
 */
@Data
@ExcelTarget("logininforExcel")
public class SysLogininforExcelDto implements Serializable {

    @Excel(name = "访问编号")
    private Long infoId;

    @Excel(name = "登录账号")
    private String userName;

    @Excel(name = "登录IP地址")
    private String ipaddr;

    @Excel(name = "登录地点")
    private String loginLocation;

    @Excel(name = "浏览器")
    private String browser;

    @Excel(name = "操作系统")
    private String os;

    @Excel(name = "登录状态", replace = {"成功_0", "失败_1"})
    private String status;

    @Excel(name = "提示消息")
    private String msg;

    @Excel(name = "访问时间", exportFormat = "yyyy-MM-dd HH:mm:ss")
    private Date loginTime;
}
