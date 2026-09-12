package com.msb.hjycommunity.monitor.domain.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 操作日志导出Excel实体
 */
@Data
@ExcelTarget("operlogExcel")
public class SysOperlogExcelDto implements Serializable {

    @Excel(name = "日志编号")
    private Long operId;

    @Excel(name = "模块标题")
    private String title;

    @Excel(name = "业务类型")
    private String businessType;

    @Excel(name = "方法名称")
    private String method;

    @Excel(name = "操作人员")
    private String operName;

    @Excel(name = "请求URL")
    private String operUrl;

    @Excel(name = "操作IP")
    private String operIp;

    @Excel(name = "操作地点")
    private String operLocation;

    @Excel(name = "操作状态", replace = {"成功_0", "失败_1"})
    private String status;

    @Excel(name = "操作时间", exportFormat = "yyyy-MM-dd HH:mm:ss")
    private Date operTime;
}
