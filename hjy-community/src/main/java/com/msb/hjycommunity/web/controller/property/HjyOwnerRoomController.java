package com.msb.hjycommunity.web.controller.property;

import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import com.msb.hjycommunity.property.domain.dto.HjyOwnerRoomExcelDto;
import org.springframework.beans.BeanUtils;
import com.msb.hjycommunity.common.utils.ExcelUtils;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.msb.hjycommunity.common.annotation.Log;
import com.msb.hjycommunity.common.core.controller.BaseController;
import com.msb.hjycommunity.common.core.domain.BaseResponse;
import com.msb.hjycommunity.common.core.page.PageResult;
import com.msb.hjycommunity.common.enums.BusinessType;
import com.msb.hjycommunity.common.utils.SecurityUtils;
import com.msb.hjycommunity.property.domain.HjyOwnerRoom;
import com.msb.hjycommunity.property.domain.HjyOwnerRoomRecord;
import com.msb.hjycommunity.property.service.HjyOwnerRoomService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 房屋绑定Controller
 */
@RestController
@RequestMapping("/system/ownerRoom")
public class HjyOwnerRoomController extends BaseController {

    @Resource
    private HjyOwnerRoomService ownerRoomService;

    /**
     * 导出业主房屋绑定数据（Excel流下载）
     */
    @GetMapping("/export")
    public void export(HjyOwnerRoom ownerRoom, HttpServletResponse response) {
        List<HjyOwnerRoom> list = ownerRoomService.selectOwnerRoomList(ownerRoom);
        List<HjyOwnerRoomExcelDto> dtoList = list.stream().map(item -> {
            HjyOwnerRoomExcelDto dto = new HjyOwnerRoomExcelDto();
            BeanUtils.copyProperties(item, dto);
            return dto;
        }).collect(Collectors.toList());
        ExcelUtils.exportExcel(dtoList, HjyOwnerRoomExcelDto.class, "业主房屋绑定.xls", response,
                new ExportParams("业主房屋绑定列表", "业主房屋绑定"));
    }

    /**
     * 获取房屋绑定列表
     */
    @GetMapping("/list")
    @PreAuthorize("@pe.hasPerms('system:ownerRoom:list')")
    public PageResult list(HjyOwnerRoom ownerRoom) {
        startPage();
        List<HjyOwnerRoom> list = ownerRoomService.selectOwnerRoomList(ownerRoom);
        return getData(list);
    }

    /**
     * 获取房屋绑定详情
     */
    @GetMapping("/{ownerRoomId}")
    @PreAuthorize("@pe.hasPerms('system:ownerRoom:query')")
    public BaseResponse getInfo(@PathVariable Long ownerRoomId) {
        HjyOwnerRoom ownerRoom = ownerRoomService.selectOwnerRoomById(ownerRoomId);
        List<HjyOwnerRoomRecord> records = ownerRoomService.selectRecordList(ownerRoomId);
        Map<String, Object> result = new HashMap<>();
        result.put("data", ownerRoom);
        result.put("records", records);
        return BaseResponse.success(result);
    }

    /**
     * 新增房屋绑定
     */
    @PostMapping
    @Log(title = "房屋绑定", businessType = BusinessType.INSERT)
    @PreAuthorize("@pe.hasPerms('system:ownerRoom:add')")
    public BaseResponse add(@RequestBody HjyOwnerRoom ownerRoom) {
        ownerRoom.setCreateBy(SecurityUtils.getUserName());
        return toAjax(ownerRoomService.insertOwnerRoom(ownerRoom));
    }

    /**
     * 修改房屋绑定
     */
    @PutMapping
    @Log(title = "房屋绑定", businessType = BusinessType.UPDATE)
    @PreAuthorize("@pe.hasPerms('system:ownerRoom:edit')")
    public BaseResponse edit(@RequestBody HjyOwnerRoom ownerRoom) {
        ownerRoom.setUpdateBy(SecurityUtils.getUserName());
        return toAjax(ownerRoomService.updateOwnerRoom(ownerRoom));
    }

    /**
     * 删除房屋绑定
     */
    @DeleteMapping("/{ownerRoomIds}")
    @Log(title = "房屋绑定", businessType = BusinessType.DELETE)
    @PreAuthorize("@pe.hasPerms('system:ownerRoom:remove')")
    public BaseResponse remove(@PathVariable Long[] ownerRoomIds) {
        return toAjax(ownerRoomService.deleteOwnerRoomByIds(ownerRoomIds));
    }

    /**
     * 审核绑定：通过(pass)或驳回(reject)
     */
    @PutMapping("/audit")
    @Log(title = "房屋绑定", businessType = BusinessType.OTHER)
    @PreAuthorize("@pe.hasPerms('system:ownerRoom:audit')")
    public BaseResponse audit(@RequestBody Map<String, Object> params) {
        Long ownerRoomId = Long.valueOf(params.get("ownerRoomId").toString());
        boolean pass = "pass".equals(params.get("auditResult"));
        String opinion = params.get("recordAuditOpinion") == null ? "" : params.get("recordAuditOpinion").toString();
        return toAjax(ownerRoomService.auditOwnerRoom(ownerRoomId, pass, opinion));
    }
}
