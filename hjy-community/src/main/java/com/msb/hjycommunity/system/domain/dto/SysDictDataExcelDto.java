package com.msb.hjycommunity.system.domain.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import lombok.Data;

import java.io.Serializable;

/**
 * 字典数据导出Excel实体
 */
@Data
@ExcelTarget("dictDataExcel")
public class SysDictDataExcelDto implements Serializable {

    @Excel(name = "字典编码")
    private Long dictCode;

    @Excel(name = "字典排序")
    private Long dictSort;

    @Excel(name = "字典标签")
    private String dictLabel;

    @Excel(name = "字典键值")
    private String dictValue;

    @Excel(name = "字典类型")
    private String dictType;

    @Excel(name = "状态", replace = {"正常_0", "停用_1"})
    private String status;

    @Excel(name = "备注")
    private String remark;
}
