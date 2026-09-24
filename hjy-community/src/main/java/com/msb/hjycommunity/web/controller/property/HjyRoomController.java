package com.msb.hjycommunity.web.controller.property;

import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import com.msb.hjycommunity.property.domain.dto.HjyRoomExcelDto;
import org.springframework.beans.BeanUtils;
import com.msb.hjycommunity.common.utils.ExcelUtils;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.msb.hjycommunity.common.annotation.Log;
import com.msb.hjycommunity.common.core.controller.BaseController;
import com.msb.hjycommunity.common.core.domain.BaseResponse;
import com.msb.hjycommunity.common.core.page.PageResult;
import com.msb.hjycommunity.common.enums.BusinessType;
import com.msb.hjycommunity.common.utils.SecurityUtils;
import com.msb.hjycommunity.property.domain.HjyRoom;
import com.msb.hjycommunity.property.domain.vo.HjyRoomVo;
import com.msb.hjycommunity.property.service.HjyRoomService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 房间Controller
 */
@RestController
@RequestMapping("/system/room")
public class HjyRoomController extends BaseController {

    @Resource
    private HjyRoomService roomService;

    /**
     * 导出房间信息数据（Excel流下载）
     */
    @GetMapping("/export")
    @PreAuthorize("@pe.hasPerms('system:room:export')")
    public void export(HjyRoom room, HttpServletResponse response) {
        List<HjyRoom> list = roomService.selectRoomList(room);
        List<HjyRoomExcelDto> dtoList = list.stream().map(item -> {
            HjyRoomExcelDto dto = new HjyRoomExcelDto();
            BeanUtils.copyProperties(item, dto);
            return dto;
        }).collect(Collectors.toList());
        ExcelUtils.exportExcel(dtoList, HjyRoomExcelDto.class, "房间信息.xls", response,
                new ExportParams("房间信息列表", "房间信息"));
    }

    /**
     * 获取房间列表
     */
    @GetMapping("/list")
    @PreAuthorize("@pe.hasPerms('system:room:list')")
    public PageResult list(HjyRoom room) {
        startPage();
        List<HjyRoom> list = roomService.selectRoomList(room);
        return getData(list);
    }

    /**
     * 获取下拉列表
     */
    @GetMapping("/queryPullDownRoom")
    public BaseResponse queryPullDownRoom(@RequestParam(required = false) Long unitId) {
        List<HjyRoomVo> list = roomService.selectRoomPullDown(unitId);
        return BaseResponse.success(list);
    }

    /**
     * 获取房间详情
     */
    @GetMapping("/{roomId}")
    @PreAuthorize("@pe.hasPerms('system:room:query')")
    public BaseResponse getInfo(@PathVariable Long roomId) {
        return BaseResponse.success(roomService.selectRoomById(roomId));
    }

    /**
     * 新增房间
     */
    @PostMapping
    @Log(title = "房屋管理", businessType = BusinessType.INSERT)
    @PreAuthorize("@pe.hasPerms('system:room:add')")
    public BaseResponse add(@RequestBody HjyRoom room) {
        room.setCreateBy(SecurityUtils.getUserName());
        return toAjax(roomService.insertRoom(room));
    }

    /**
     * 修改房间
     */
    @PutMapping
    @Log(title = "房屋管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@pe.hasPerms('system:room:edit')")
    public BaseResponse edit(@RequestBody HjyRoom room) {
        room.setUpdateBy(SecurityUtils.getUserName());
        return toAjax(roomService.updateRoom(room));
    }

    /**
     * 删除房间
     */
    @DeleteMapping("/{roomIds}")
    @Log(title = "房屋管理", businessType = BusinessType.DELETE)
    @PreAuthorize("@pe.hasPerms('system:room:remove')")
    public BaseResponse remove(@PathVariable Long[] roomIds) {
        return toAjax(roomService.deleteRoomByIds(roomIds));
    }
}
