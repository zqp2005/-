package com.msb.hjycommunity.property.controller;

import com.msb.hjycommunity.common.core.controller.BaseController;
import com.msb.hjycommunity.common.core.domain.BaseResponse;
import com.msb.hjycommunity.common.core.page.PageResult;
import com.msb.hjycommunity.common.utils.SecurityUtils;
import com.msb.hjycommunity.property.domain.HjyRepair;
import com.msb.hjycommunity.property.service.HjyRepairService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * 报修Controller
 */
@RestController
@RequestMapping("/system/repair")
public class HjyRepairController extends BaseController {

    @Resource
    private HjyRepairService repairService;

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
    @PreAuthorize("@pe.hasPerms('system:repair:add')")
    public BaseResponse add(@RequestBody HjyRepair repair) {
        repair.setCreateBy(SecurityUtils.getUserName());
        return toAjax(repairService.insertRepair(repair));
    }

    /**
     * 修改报修
     */
    @PutMapping
    @PreAuthorize("@pe.hasPerms('system:repair:edit')")
    public BaseResponse edit(@RequestBody HjyRepair repair) {
        repair.setUpdateBy(SecurityUtils.getUserName());
        return toAjax(repairService.updateRepair(repair));
    }

    /**
     * 删除报修
     */
    @DeleteMapping("/{repairIds}")
    @PreAuthorize("@pe.hasPerms('system:repair:remove')")
    public BaseResponse remove(@PathVariable Long[] repairIds) {
        return toAjax(repairService.deleteRepairByIds(repairIds));
    }

    /**
     * 分派报修
     */
    @PutMapping("/assign")
    @PreAuthorize("@pe.hasPerms('system:repair:assign')")
    public BaseResponse assign(@RequestBody HjyRepair repair) {
        repair.setRepairState("Allocated");
        repair.setAssignmentTime(new Date());
        repair.setUpdateBy(SecurityUtils.getUserName());
        return toAjax(repairService.updateRepair(repair));
    }

    /**
     * 处理报修
     */
    @PutMapping("/process")
    @PreAuthorize("@pe.hasPerms('system:repair:process')")
    public BaseResponse process(@RequestBody HjyRepair repair) {
        repair.setRepairState("Processed");
        repair.setCompleteTime(new Date());
        repair.setUpdateBy(SecurityUtils.getUserName());
        return toAjax(repairService.updateRepair(repair));
    }
}
