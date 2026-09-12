package com.msb.hjycommunity.property.domain.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 业主导出Excel实体
 */
@Data
@ExcelTarget("ownerExcel")
public class HjyOwnerExcelDto implements Serializable {

    @Excel(name = "序号")
    private Long ownerId;

    @Excel(name = "姓名")
    private String ownerRealName;

    @Excel(name = "性别", replace = {"男_Male", "女_Female"})
    private String ownerGender;

    @Excel(name = "年龄")
    private Integer ownerAge;

    @Excel(name = "手机号")
    private String ownerPhoneNumber;

    @Excel(name = "身份证号")
    private String ownerIdCard;

    @Excel(name = "状态")
    private String ownerStatus;

    @Excel(name = "创建时间", exportFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
