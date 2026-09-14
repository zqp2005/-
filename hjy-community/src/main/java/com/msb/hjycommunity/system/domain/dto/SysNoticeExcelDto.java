package com.msb.hjycommunity.system.domain.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 通知公告导出Excel实体
 */
@Data
@ExcelTarget("noticeExcel")
public class SysNoticeExcelDto implements Serializable {

    @Excel(name = "公告序号")
    private Long noticeId;

    @Excel(name = "公告标题")
    private String noticeTitle;

    @Excel(name = "公告类型")
    private String noticeType;

    @Excel(name = "状态", replace = {"正常_0", "关闭_1"})
    private String status;

    @Excel(name = "创建者")
    private String createBy;

    @Excel(name = "创建时间", exportFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @Excel(name = "备注")
    private String remark;
}
