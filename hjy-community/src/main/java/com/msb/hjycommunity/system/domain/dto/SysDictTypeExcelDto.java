package com.msb.hjycommunity.system.domain.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import lombok.Data;

import java.io.Serializable;

/**
 * 字典类型导出Excel实体
 */
@Data
@ExcelTarget("dictTypeExcel")
public class SysDictTypeExcelDto implements Serializable {

    @Excel(name = "字典主键")
    private Long dictId;

    @Excel(name = "字典名称")
    private String dictName;

    @Excel(name = "字典类型")
    private String dictType;

    @Excel(name = "状态", replace = {"正常_0", "停用_1"})
    private String status;

    @Excel(name = "备注")
    private String remark;
}
