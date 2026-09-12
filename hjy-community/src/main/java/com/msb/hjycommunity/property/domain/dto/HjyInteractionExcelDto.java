package com.msb.hjycommunity.property.domain.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 社区互动导出Excel实体
 */
@Data
@ExcelTarget("interactionExcel")
public class HjyInteractionExcelDto implements Serializable {

    @Excel(name = "序号")
    private Long interactionId;

    @Excel(name = "互动内容")
    private String content;

    @Excel(name = "所属小区")
    private String communityName;

    @Excel(name = "发布人")
    private String createBy;

    @Excel(name = "发布时间", exportFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
