package com.msb.hjycommunity.web.controller.property;

import com.msb.hjycommunity.common.core.controller.BaseController;
import com.msb.hjycommunity.common.core.domain.BaseResponse;
import com.msb.hjycommunity.common.core.exception.CustomException;
import com.msb.hjycommunity.common.core.page.PageResult;
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
     * 修改报修（普通编辑仅允许内容类字段，状态走动作接口）
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
     * 派单：待处理 -> 已分派
     */
    @PutMapping("/assign/{repairId}")
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
    @PreAuthorize("@pe.hasPerms('system:repair:receive')")
    public BaseResponse receive(@PathVariable Long repairId) {
        return toAjax(repairService.receiveRepair(repairId));
    }

    /**
     * 完成：处理中 -> 已处理
     */
    @PutMapping("/complete/{repairId}")
    @PreAuthorize("@pe.hasPerms('system:repair:complete')")
    public BaseResponse complete(@PathVariable Long repairId) {
        return toAjax(repairService.completeRepair(repairId));
    }

    /**
     * 取消：待处理/已分派 -> 已取消
     */
    @PutMapping("/cancel/{repairId}")
    @PreAuthorize("@pe.hasPerms('system:repair:cancel')")
    public BaseResponse cancel(@PathVariable Long repairId, @RequestBody Map<String, Object> params) {
        String reason = params.get("reason") == null ? "" : params.get("reason").toString();
        return toAjax(repairService.cancelRepair(repairId, reason));
    }

    /**
     * 不处理：待处理 -> 不处理
     */
    @PutMapping("/reject/{repairId}")
    @PreAuthorize("@pe.hasPerms('system:repair:reject')")
    public BaseResponse reject(@PathVariable Long repairId, @RequestBody Map<String, Object> params) {
        String reason = params.get("reason") == null ? "" : params.get("reason").toString();
        return toAjax(repairService.rejectRepair(repairId, reason));
    }
}
