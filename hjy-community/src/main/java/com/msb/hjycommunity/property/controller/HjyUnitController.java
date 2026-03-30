package com.msb.hjycommunity.property.controller;

import com.msb.hjycommunity.common.core.controller.BaseController;
import com.msb.hjycommunity.common.core.domain.BaseResponse;
import com.msb.hjycommunity.common.core.page.PageResult;
import com.msb.hjycommunity.common.utils.SecurityUtils;
import com.msb.hjycommunity.property.domain.HjyUnit;
import com.msb.hjycommunity.property.domain.vo.HjyUnitVo;
import com.msb.hjycommunity.property.service.HjyUnitService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 单元Controller
 */
@RestController
@RequestMapping("/system/unit")
public class HjyUnitController extends BaseController {

    @Resource
    private HjyUnitService unitService;

    /**
     * 获取单元列表
     */
    @GetMapping("/list")
    @PreAuthorize("@pe.hasPerms('system:unit:list')")
    public PageResult list(HjyUnit unit) {
        startPage();
        List<HjyUnit> list = unitService.selectUnitList(unit);
        return getData(list);
    }

    /**
     * 获取下拉列表
     */
    @GetMapping("/queryPullDown")
    public BaseResponse queryPullDown(@RequestParam(required = false) Long buildingId) {
        List<HjyUnitVo> list = unitService.selectUnitPullDown(buildingId);
        return BaseResponse.success(list);
    }

    /**
     * 获取单元详情
     */
    @GetMapping("/{unitId}")
    @PreAuthorize("@pe.hasPerms('system:unit:query')")
    public BaseResponse getInfo(@PathVariable Long unitId) {
        return BaseResponse.success(unitService.selectUnitById(unitId));
    }

    /**
     * 新增单元
     */
    @PostMapping
    @PreAuthorize("@pe.hasPerms('system:unit:add')")
    public BaseResponse add(@RequestBody HjyUnit unit) {
        unit.setCreateBy(SecurityUtils.getUserName());
        return toAjax(unitService.insertUnit(unit));
    }

    /**
     * 修改单元
     */
    @PutMapping
    @PreAuthorize("@pe.hasPerms('system:unit:edit')")
    public BaseResponse edit(@RequestBody HjyUnit unit) {
        unit.setUpdateBy(SecurityUtils.getUserName());
        return toAjax(unitService.updateUnit(unit));
    }

    /**
     * 删除单元
     */
    @DeleteMapping("/{unitIds}")
    @PreAuthorize("@pe.hasPerms('system:unit:remove')")
    public BaseResponse remove(@PathVariable Long[] unitIds) {
        return toAjax(unitService.deleteUnitByIds(unitIds));
    }
}
