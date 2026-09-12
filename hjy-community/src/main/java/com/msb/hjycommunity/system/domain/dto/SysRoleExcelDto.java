package com.msb.hjycommunity.system.domain.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 角色导出Excel实体
 */
@Data
@ExcelTarget("roleExcel")
public class SysRoleExcelDto implements Serializable {

    @Excel(name = "角色编号")
    private Long roleId;

    @Excel(name = "角色名称")
    private String roleName;

    @Excel(name = "权限字符")
    private String roleKey;

    @Excel(name = "显示顺序")
    private String roleSort;

    @Excel(name = "状态", replace = {"正常_0", "停用_1"})
    private String status;

    @Excel(name = "创建时间", exportFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
}
