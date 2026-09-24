package com.msb.hjycommunity.web.controller.property;

import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import com.msb.hjycommunity.property.domain.dto.HjyVisitorExcelDto;
import org.springframework.beans.BeanUtils;
import com.msb.hjycommunity.common.utils.ExcelUtils;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.msb.hjycommunity.common.core.controller.BaseController;
import com.msb.hjycommunity.common.core.domain.BaseResponse;
import com.msb.hjycommunity.common.core.page.PageResult;
import com.msb.hjycommunity.common.utils.SecurityUtils;
import com.msb.hjycommunity.property.domain.HjyVisitor;
import com.msb.hjycommunity.property.service.HjyVisitorService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 访客Controller
 */
@RestController
@RequestMapping("/system/visitor")
public class HjyVisitorController extends BaseController {

    @Resource
    private HjyVisitorService visitorService;

    /**
     * 导出访客信息数据（Excel流下载）
     */
    @GetMapping("/export")
    @PreAuthorize("@pe.hasPerms('system:visitor:export')")
    public void export(HjyVisitor visitor, HttpServletResponse response) {
        List<HjyVisitor> list = visitorService.selectVisitorList(visitor);
        List<HjyVisitorExcelDto> dtoList = list.stream().map(item -> {
            HjyVisitorExcelDto dto = new HjyVisitorExcelDto();
            BeanUtils.copyProperties(item, dto);
            return dto;
        }).collect(Collectors.toList());
        ExcelUtils.exportExcel(dtoList, HjyVisitorExcelDto.class, "访客信息.xls", response,
                new ExportParams("访客信息列表", "访客信息"));
    }

    /**
     * 获取访客列表
     */
    @GetMapping("/list")
    @PreAuthorize("@pe.hasPerms('system:visitor:list')")
    public PageResult list(HjyVisitor visitor) {
        startPage();
        List<HjyVisitor> list = visitorService.selectVisitorList(visitor);
        return getData(list);
    }

    /**
     * 获取访客详情
     */
    @GetMapping("/{visitorId}")
    @PreAuthorize("@pe.hasPerms('system:visitor:query')")
    public BaseResponse getInfo(@PathVariable Long visitorId) {
        return BaseResponse.success(visitorService.selectVisitorById(visitorId));
    }

    /**
     * 新增访客
     */
    @PostMapping
    @PreAuthorize("@pe.hasPerms('system:visitor:add')")
    public BaseResponse add(@RequestBody HjyVisitor visitor) {
        visitor.setCreateBy(SecurityUtils.getUserName());
        return toAjax(visitorService.insertVisitor(visitor));
    }

    /**
     * 修改访客
     */
    @PutMapping
    @PreAuthorize("@pe.hasPerms('system:visitor:edit')")
    public BaseResponse edit(@RequestBody HjyVisitor visitor) {
        visitor.setUpdateBy(SecurityUtils.getUserName());
        return toAjax(visitorService.updateVisitor(visitor));
    }

    /**
     * 删除访客
     */
    @DeleteMapping("/{visitorIds}")
    @PreAuthorize("@pe.hasPerms('system:visitor:remove')")
    public BaseResponse remove(@PathVariable Long[] visitorIds) {
        return toAjax(visitorService.deleteVisitorByIds(visitorIds));
    }
}
