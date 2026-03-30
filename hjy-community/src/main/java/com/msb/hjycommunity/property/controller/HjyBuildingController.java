package com.msb.hjycommunity.property.controller;

import com.msb.hjycommunity.common.core.controller.BaseController;
import com.msb.hjycommunity.common.core.domain.BaseResponse;
import com.msb.hjycommunity.common.core.page.PageResult;
import com.msb.hjycommunity.common.utils.SecurityUtils;
import com.msb.hjycommunity.property.domain.HjyBuilding;
import com.msb.hjycommunity.property.domain.vo.HjyBuildingVo;
import com.msb.hjycommunity.property.service.HjyBuildingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 楼栋Controller
 */
@RestController
@RequestMapping("/system/building")
public class HjyBuildingController extends BaseController {

    @Resource
    private HjyBuildingService buildingService;

    /**
     * 获取楼栋列表
     */
    @GetMapping("/list")
    @PreAuthorize("@pe.hasPerms('system:building:list')")
    public PageResult list(HjyBuilding building) {
        startPage();
        List<HjyBuilding> list = buildingService.selectBuildingList(building);
        return getData(list);
    }

    /**
     * 获取下拉列表
     */
    @GetMapping("/queryPullDown")
    public BaseResponse queryPullDown(@RequestParam(required = false) Long communityId) {
        List<HjyBuildingVo> list = buildingService.selectBuildingPullDown(communityId);
        return BaseResponse.success(list);
    }

    /**
     * 获取楼栋详情
     */
    @GetMapping("/{buildingId}")
    @PreAuthorize("@pe.hasPerms('system:building:query')")
    public BaseResponse getInfo(@PathVariable Long buildingId) {
        return BaseResponse.success(buildingService.selectBuildingById(buildingId));
    }

    /**
     * 新增楼栋
     */
    @PostMapping
    @PreAuthorize("@pe.hasPerms('system:building:add')")
    public BaseResponse add(@RequestBody HjyBuilding building) {
        building.setCreateBy(SecurityUtils.getUserName());
        return toAjax(buildingService.insertBuilding(building));
    }

    /**
     * 修改楼栋
     */
    @PutMapping
    @PreAuthorize("@pe.hasPerms('system:building:edit')")
    public BaseResponse edit(@RequestBody HjyBuilding building) {
        building.setUpdateBy(SecurityUtils.getUserName());
        return toAjax(buildingService.updateBuilding(building));
    }

    /**
     * 删除楼栋
     */
    @DeleteMapping("/{buildingIds}")
    @PreAuthorize("@pe.hasPerms('system:building:remove')")
    public BaseResponse remove(@PathVariable Long[] buildingIds) {
        return toAjax(buildingService.deleteBuildingByIds(buildingIds));
    }
}
