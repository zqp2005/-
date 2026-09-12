package com.msb.hjycommunity.system.domain.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import lombok.Data;

import java.io.Serializable;

/**
 * 参数配置导出Excel实体
 */
@Data
@ExcelTarget("configExcel")
public class SysConfigExcelDto implements Serializable {

    @Excel(name = "参数主键")
    private Long configId;

    @Excel(name = "参数名称")
    private String configName;

    @Excel(name = "参数键名")
    private String configKey;

    @Excel(name = "参数键值")
    private String configValue;

    @Excel(name = "系统内置", replace = {"是_Y", "否_N"})
    private String configType;

    @Excel(name = "备注")
    private String remark;
}
