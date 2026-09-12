package com.msb.hjycommunity.monitor.domain.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import lombok.Data;

import java.io.Serializable;

/**
 * 定时任务导出Excel实体
 */
@Data
@ExcelTarget("jobExcel")
public class SysJobExcelDto implements Serializable {

    @Excel(name = "任务编号")
    private Long jobId;

    @Excel(name = "任务名称")
    private String jobName;

    @Excel(name = "任务组名")
    private String jobGroup;

    @Excel(name = "调用目标字符串")
    private String invokeTarget;

    @Excel(name = "cron执行表达式")
    private String cronExpression;

    @Excel(name = "状态", replace = {"正常_0", "暂停_1"})
    private String status;
}
