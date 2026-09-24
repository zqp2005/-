package com.msb.hjycommunity.property.domain.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 房间导出Excel实体
 */
@Data
@ExcelTarget("roomExcel")
public class HjyRoomExcelDto implements Serializable {

    @Excel(name = "序号")
    private Long roomId;

    @Excel(name = "房间名称")
    private String roomName;

    @Excel(name = "房间编码")
    private String roomCode;

    @Excel(name = "面积(㎡)")
    private String roomAcreage;

    @Excel(name = "房屋状态")
    private String roomStatus;

    /** 状态编码含下划线，不能使用 EasyPOI 的下划线分隔 replace 规则。 */
    public String getRoomStatus() {
        if (roomStatus == null) {
            return null;
        }
        switch (roomStatus) {
            case "has_stay": return "已入住";
            case "none_stay": return "未入住";
            case "none": return "未出售";
            case "has_give": return "已交房";
            default: return roomStatus;
        }
    }

    @Excel(name = "户型")
    private String roomHouseType;

    @Excel(name = "所属楼栋")
    private String buildingName;

    @Excel(name = "创建时间", exportFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
