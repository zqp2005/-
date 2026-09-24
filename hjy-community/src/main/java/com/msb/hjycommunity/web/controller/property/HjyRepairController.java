package com.msb.hjycommunity.web.controller.property;

import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import com.msb.hjycommunity.property.domain.dto.HjyRepairExcelDto;
import org.springframework.beans.BeanUtils;
import com.msb.hjycommunity.common.utils.ExcelUtils;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.msb.hjycommunity.common.annotation.Log;
import com.msb.hjycommunity.common.core.controller.BaseController;
import com.msb.hjycommunity.common.core.domain.BaseResponse;
import com.msb.hjycommunity.common.core.exception.CustomException;
import com.msb.hjycommunity.common.core.page.PageResult;
import com.msb.hjycommunity.common.enums.BusinessType;
import com.msb.hjycommunity.common.utils.SecurityUtils;
import com.msb.hjycommunity.property.domain.HjyRepair;
import com.msb.hjycommunity.property.service.HjyRepairService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 报修Controller
 */
@RestController
@RequestMapping("/system/repair")
public class HjyRepairController extends BaseController {

    @Resource
    private HjyRepairService repairService;

    /**
     * 导出报修工单数据（Excel流下载）
     */
    @GetMapping("/export")
    @PreAuthorize("@pe.hasPerms('system:repair:export')")
    public void export(HjyRepair repair, HttpServletResponse response) {
        List<HjyRepair> list = repairService.selectRepairList(repair);
        List<HjyRepairExcelDto> dtoList = list.stream().map(item -> {
            HjyRepairExcelDto dto = new HjyRepairExcelDto();
            BeanUtils.copyProperties(item, dto);
            return dto;
        }).collect(Collectors.toList());
        ExcelUtils.exportExcel(dtoList, HjyRepairExcelDto.class, "报修工单.xls", response,
                new ExportParams("报修工单列表", "报修工单"));
    }

    /**
     * 获取报修列表
     */
    @GetMapping("/list")
    @PreAuthorize("@pe.hasPerms('system:repair:list')")
    public PageResult list(HjyRepair repair) {
        startPage();
        List<HjyRepair> list = repairService.selectRepairList(repair);
        return getData(list);
    }

    /**
     * 获取报修详情
     */
    @GetMapping("/{repairId}")
    @PreAuthorize("@pe.hasPerms('system:repair:query')")
    public BaseResponse getInfo(@PathVariable Long repairId) {
        return BaseResponse.success(repairService.selectRepairById(repairId));
    }

    /**
     * 新增报修
     */
    @PostMapping
    @Log(title = "报修管理", businessType = BusinessType.INSERT)
    @PreAuthorize("@pe.hasPerms('system:repair:add')")
    public BaseResponse add(@RequestBody HjyRepair repair) {
        repair.setCreateBy(SecurityUtils.getUserName());
        return toAjax(repairService.insertRepair(repair));
    }

    /**
     * 修改报修（普通编辑仅允许内容类字段，状态走动作接口）
     */
    @PutMapping
    @Log(title = "报修管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@pe.hasPerms('system:repair:edit')")
    public BaseResponse edit(@RequestBody HjyRepair repair) {
        repair.setUpdateBy(SecurityUtils.getUserName());
        return toAjax(repairService.updateRepair(repair));
    }

    /**
     * 删除报修
     */
    @DeleteMapping("/{repairIds}")
    @Log(title = "报修管理", businessType = BusinessType.DELETE)
    @PreAuthorize("@pe.hasPerms('system:repair:remove')")
    public BaseResponse remove(@PathVariable Long[] repairIds) {
        return toAjax(repairService.deleteRepairByIds(repairIds));
    }

    /**
     * 派单：待处理 -> 已分派
     */
    @PutMapping("/assign/{repairId}")
    @Log(title = "报修管理", businessType = BusinessType.OTHER)
    @PreAuthorize("@pe.hasPerms('system:repair:assign')")
    public BaseResponse assign(@PathVariable Long repairId, @RequestBody Map<String, Object> params) {
        Object assignmentIdParam = params.get("assignmentId");
        if (assignmentIdParam == null) {
            throw new CustomException(500, "参数缺失：assignmentId");
        }
        Long assignmentId = Long.valueOf(assignmentIdParam.toString());
        return toAjax(repairService.assignRepair(repairId, assignmentId));
    }

    /**
     * 接单：已分派 -> 处理中
     */
    @PutMapping("/receive/{repairId}")
    @Log(title = "报修管理", businessType = BusinessType.OTHER)
    @PreAuthorize("@pe.hasPerms('system:repair:receive')")
    public BaseResponse receive(@PathVariable Long repairId) {
        return toAjax(repairService.receiveRepair(repairId));
    }

    /**
     * 完成：处理中 -> 已处理
     */
    @PutMapping("/complete/{repairId}")
    @Log(title = "报修管理", businessType = BusinessType.OTHER)
    @PreAuthorize("@pe.hasPerms('system:repair:complete')")
    public BaseResponse complete(@PathVariable Long repairId) {
        return toAjax(repairService.completeRepair(repairId));
    }

    /**
     * 取消：待处理/已分派 -> 已取消
     */
    @PutMapping("/cancel/{repairId}")
    @Log(title = "报修管理", businessType = BusinessType.OTHER)
    @PreAuthorize("@pe.hasPerms('system:repair:cancel')")
    public BaseResponse cancel(@PathVariable Long repairId, @RequestBody Map<String, Object> params) {
        String reason = params.get("reason") == null ? "" : params.get("reason").toString();
        return toAjax(repairService.cancelRepair(repairId, reason));
    }

    /**
     * 不处理：待处理 -> 不处理
     */
    @PutMapping("/reject/{repairId}")
    @Log(title = "报修管理", businessType = BusinessType.OTHER)
    @PreAuthorize("@pe.hasPerms('system:repair:reject')")
    public BaseResponse reject(@PathVariable Long repairId, @RequestBody Map<String, Object> params) {
        String reason = params.get("reason") == null ? "" : params.get("reason").toString();
        return toAjax(repairService.rejectRepair(repairId, reason));
    }
}
