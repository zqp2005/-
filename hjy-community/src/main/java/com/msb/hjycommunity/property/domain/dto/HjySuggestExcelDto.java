package com.msb.hjycommunity.property.domain.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 投诉建议导出Excel实体
 */
@Data
@ExcelTarget("suggestExcel")
public class HjySuggestExcelDto implements Serializable {

    @Excel(name = "序号")
    private Long complaintSuggestId;

    @Excel(name = "类型")
    private String complaintSuggestType;

    @Excel(name = "状态", replace = {"待受理_Pending", "处理中_Processing", "已处理_Replied", "已关闭_Closed"})
    private String complaintState;

    @Excel(name = "业主姓名")
    private String ownerRealName;

    @Excel(name = "联系电话")
    private String ownerPhoneNumber;

    @Excel(name = "内容")
    private String complaintSuggestContent;

    @Excel(name = "处理人")
    private String handleBy;

    @Excel(name = "处理时间", exportFormat = "yyyy-MM-dd HH:mm")
    private Date handleTime;

    @Excel(name = "回复内容")
    private String replyContent;

    @Excel(name = "创建时间", exportFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
