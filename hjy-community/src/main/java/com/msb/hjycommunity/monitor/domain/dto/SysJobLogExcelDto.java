package com.msb.hjycommunity.monitor.domain.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 任务调度日志导出Excel实体
 */
@Data
@ExcelTarget("jobLogExcel")
public class SysJobLogExcelDto implements Serializable {

    @Excel(name = "日志编号")
    private Long jobLogId;

    @Excel(name = "任务名称")
    private String jobName;

    @Excel(name = "任务组名")
    private String jobGroup;

    @Excel(name = "调用目标字符串")
    private String invokeTarget;

    @Excel(name = "日志信息")
    private String jobMessage;

    @Excel(name = "执行状态", replace = {"成功_0", "失败_1"})
    private String status;

    @Excel(name = "执行时间", exportFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
