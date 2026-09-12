package com.msb.hjycommunity.web.controller.property;

import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import com.msb.hjycommunity.property.domain.dto.HjyBuildingExcelDto;
import org.springframework.beans.BeanUtils;
import com.msb.hjycommunity.common.utils.ExcelUtils;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.msb.hjycommunity.common.annotation.Log;
import com.msb.hjycommunity.common.core.controller.BaseController;
import com.msb.hjycommunity.common.core.domain.BaseResponse;
import com.msb.hjycommunity.common.core.page.PageResult;
import com.msb.hjycommunity.common.enums.BusinessType;
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
     * 导出楼栋信息数据（Excel流下载）
     */
    @GetMapping("/export")
    public void export(HjyBuilding building, HttpServletResponse response) {
        List<HjyBuilding> list = buildingService.selectBuildingList(building);
        List<HjyBuildingExcelDto> dtoList = list.stream().map(item -> {
            HjyBuildingExcelDto dto = new HjyBuildingExcelDto();
            BeanUtils.copyProperties(item, dto);
            return dto;
        }).collect(Collectors.toList());
        ExcelUtils.exportExcel(dtoList, HjyBuildingExcelDto.class, "楼栋信息.xls", response,
                new ExportParams("楼栋信息列表", "楼栋信息"));
    }

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
    @Log(title = "楼栋管理", businessType = BusinessType.INSERT)
    @PreAuthorize("@pe.hasPerms('system:building:add')")
    public BaseResponse add(@RequestBody HjyBuilding building) {
        building.setCreateBy(SecurityUtils.getUserName());
        return toAjax(buildingService.insertBuilding(building));
    }

    /**
     * 修改楼栋
     */
    @PutMapping
    @Log(title = "楼栋管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@pe.hasPerms('system:building:edit')")
    public BaseResponse edit(@RequestBody HjyBuilding building) {
        building.setUpdateBy(SecurityUtils.getUserName());
        return toAjax(buildingService.updateBuilding(building));
    }

    /**
     * 删除楼栋
     */
    @DeleteMapping("/{buildingIds}")
    @Log(title = "楼栋管理", businessType = BusinessType.DELETE)
    @PreAuthorize("@pe.hasPerms('system:building:remove')")
    public BaseResponse remove(@PathVariable Long[] buildingIds) {
        return toAjax(buildingService.deleteBuildingByIds(buildingIds));
    }
}
