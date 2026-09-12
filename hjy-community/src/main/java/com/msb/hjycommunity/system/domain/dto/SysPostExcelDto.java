package com.msb.hjycommunity.system.domain.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import lombok.Data;

import java.io.Serializable;

/**
 * 岗位导出Excel实体
 */
@Data
@ExcelTarget("postExcel")
public class SysPostExcelDto implements Serializable {

    @Excel(name = "岗位编号")
    private Long postId;

    @Excel(name = "岗位编码")
    private String postCode;

    @Excel(name = "岗位名称")
    private String postName;

    @Excel(name = "岗位排序")
    private String postSort;

    @Excel(name = "状态", replace = {"正常_0", "停用_1"})
    private String status;

    @Excel(name = "备注")
    private String remark;
}
