package com.msb.hjycommunity.property.domain.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 报修工单导出Excel实体
 */
@Data
@ExcelTarget("repairExcel")
public class HjyRepairExcelDto implements Serializable {

    @Excel(name = "序号")
    private Long repairId;

    @Excel(name = "工单号")
    private String repairNum;

    @Excel(name = "状态", replace = {"待处理_Pending", "已分派_Allocated", "处理中_Processing",
            "已处理_Processed", "已取消_Cancelled", "不处理_No_Processed"})
    private String repairState;

    @Excel(name = "业主姓名")
    private String ownerRealName;

    @Excel(name = "联系电话")
    private String ownerPhoneNumber;

    @Excel(name = "报修内容")
    private String repairContent;

    @Excel(name = "地址")
    private String address;

    @Excel(name = "预约上门", exportFormat = "yyyy-MM-dd HH:mm")
    private Date doorTime;

    @Excel(name = "处理人")
    private String completeName;

    @Excel(name = "创建时间", exportFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
