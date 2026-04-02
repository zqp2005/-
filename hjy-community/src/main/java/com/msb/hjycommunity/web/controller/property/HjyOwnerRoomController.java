package com.msb.hjycommunity.web.controller.property;

import com.msb.hjycommunity.common.core.controller.BaseController;
import com.msb.hjycommunity.common.core.domain.BaseResponse;
import com.msb.hjycommunity.common.core.page.PageResult;
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
    @PreAuthorize("@pe.hasPerms('system:ownerRoom:add')")
    public BaseResponse add(@RequestBody HjyOwnerRoom ownerRoom) {
        ownerRoom.setCreateBy(SecurityUtils.getUserName());
        return toAjax(ownerRoomService.insertOwnerRoom(ownerRoom));
    }

    /**
     * 修改房屋绑定
     */
    @PutMapping
    @PreAuthorize("@pe.hasPerms('system:ownerRoom:edit')")
    public BaseResponse edit(@RequestBody HjyOwnerRoom ownerRoom) {
        ownerRoom.setUpdateBy(SecurityUtils.getUserName());
        return toAjax(ownerRoomService.updateOwnerRoom(ownerRoom));
    }

    /**
     * 删除房屋绑定
     */
    @DeleteMapping("/{ownerRoomIds}")
    @PreAuthorize("@pe.hasPerms('system:ownerRoom:remove')")
    public BaseResponse remove(@PathVariable Long[] ownerRoomIds) {
        return toAjax(ownerRoomService.deleteOwnerRoomByIds(ownerRoomIds));
    }

    /**
     * 审核房屋绑定
     */
    @PutMapping("/audit")
    @PreAuthorize("@pe.hasPerms('system:ownerRoom:audit')")
    public BaseResponse audit(@RequestBody Map<String, Object> params) {
        HjyOwnerRoom ownerRoom = new HjyOwnerRoom();
        ownerRoom.setOwnerRoomId(Long.valueOf(params.get("ownerRoomId").toString()));
        ownerRoom.setRoomStatus(params.get("roomStatus").toString());
        
        HjyOwnerRoomRecord record = new HjyOwnerRoomRecord();
        record.setRecordAuditOpinion((String) params.get("recordAuditOpinion"));
        record.setRecordAuditType("Web");
        
        return toAjax(ownerRoomService.auditOwnerRoom(ownerRoom, record));
    }
}
