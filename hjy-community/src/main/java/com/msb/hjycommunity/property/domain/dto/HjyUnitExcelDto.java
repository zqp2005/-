package com.msb.hjycommunity.property.domain.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 单元导出Excel实体
 */
@Data
@ExcelTarget("unitExcel")
public class HjyUnitExcelDto implements Serializable {

    @Excel(name = "序号")
    private Long unitId;

    @Excel(name = "单元名称")
    private String unitName;

    @Excel(name = "单元编码")
    private String unitCode;

    @Excel(name = "层数")
    private Integer unitLevel;

    @Excel(name = "面积(㎡)")
    private String unitAcreage;

    @Excel(name = "所属楼栋")
    private String buildingName;

    @Excel(name = "所属小区")
    private String communityName;

    @Excel(name = "创建时间", exportFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
