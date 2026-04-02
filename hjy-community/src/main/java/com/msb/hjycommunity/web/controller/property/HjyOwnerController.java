package com.msb.hjycommunity.web.controller.property;

import com.msb.hjycommunity.common.core.controller.BaseController;
import com.msb.hjycommunity.common.core.domain.BaseResponse;
import com.msb.hjycommunity.common.core.page.PageResult;
import com.msb.hjycommunity.common.utils.SecurityUtils;
import com.msb.hjycommunity.property.domain.HjyOwner;
import com.msb.hjycommunity.property.service.HjyOwnerService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 业主Controller
 */
@RestController
@RequestMapping("/system/owner")
public class HjyOwnerController extends BaseController {

    @Resource
    private HjyOwnerService ownerService;

    /**
     * 获取业主列表
     */
    @GetMapping("/list")
    @PreAuthorize("@pe.hasPerms('system:owner:list')")
    public PageResult list(HjyOwner owner) {
        startPage();
        List<HjyOwner> list = ownerService.selectOwnerList(owner);
        return getData(list);
    }

    /**
     * 获取业主详情
     */
    @GetMapping("/{ownerId}")
    @PreAuthorize("@pe.hasPerms('system:owner:query')")
    public BaseResponse getInfo(@PathVariable Long ownerId) {
        return BaseResponse.success(ownerService.selectOwnerById(ownerId));
    }

    /**
     * 新增业主
     */
    @PostMapping
    @PreAuthorize("@pe.hasPerms('system:owner:add')")
    public BaseResponse add(@RequestBody HjyOwner owner) {
        owner.setCreateBy(SecurityUtils.getUserName());
        return toAjax(ownerService.insertOwner(owner));
    }

    /**
     * 修改业主
     */
    @PutMapping
    @PreAuthorize("@pe.hasPerms('system:owner:edit')")
    public BaseResponse edit(@RequestBody HjyOwner owner) {
        owner.setUpdateBy(SecurityUtils.getUserName());
        return toAjax(ownerService.updateOwner(owner));
    }

    /**
     * 删除业主
     */
    @DeleteMapping("/{ownerIds}")
    @PreAuthorize("@pe.hasPerms('system:owner:remove')")
    public BaseResponse remove(@PathVariable Long[] ownerIds) {
        return toAjax(ownerService.deleteOwnerByIds(ownerIds));
    }
}
