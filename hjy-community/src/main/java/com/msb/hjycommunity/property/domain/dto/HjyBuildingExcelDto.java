package com.msb.hjycommunity.property.domain.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 楼栋导出Excel实体
 */
@Data
@ExcelTarget("buildingExcel")
public class HjyBuildingExcelDto implements Serializable {

    @Excel(name = "序号")
    private Long buildingId;

    @Excel(name = "楼栋名称")
    private String buildingName;

    @Excel(name = "楼栋编码")
    private String buildingCode;

    @Excel(name = "面积(㎡)")
    private String buildingAcreage;

    @Excel(name = "所属小区")
    private String communityName;

    @Excel(name = "创建时间", exportFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
