package com.msb.hjycommunity.property.domain.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 业主-房间绑定导出Excel实体
 */
@Data
@ExcelTarget("ownerRoomExcel")
public class HjyOwnerRoomExcelDto implements Serializable {

    @Excel(name = "序号")
    private Long ownerRoomId;

    @Excel(name = "小区")
    private String communityName;

    @Excel(name = "楼栋")
    private String buildingName;

    @Excel(name = "单元")
    private String unitName;

    @Excel(name = "房间")
    private String roomName;

    @Excel(name = "业主姓名")
    private String ownerRealName;

    @Excel(name = "业主电话")
    private String ownerPhoneNumber;

    @Excel(name = "居住类型")
    private String ownerType;

    @Excel(name = "创建时间", exportFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
