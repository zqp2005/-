package com.msb.hjycommunity.property.domain.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 访客导出Excel实体
 */
@Data
@ExcelTarget("visitorExcel")
public class HjyVisitorExcelDto implements Serializable {

    @Excel(name = "序号")
    private Long visitorId;

    @Excel(name = "访客姓名")
    private String visitorName;

    @Excel(name = "访客电话")
    private String visitorPhoneNumber;

    @Excel(name = "来访时间", exportFormat = "yyyy-MM-dd HH:mm")
    private Date visitorDate;

    @Excel(name = "所属小区")
    private String communityName;

    @Excel(name = "创建时间", exportFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
