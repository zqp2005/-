package com.msb.hjycommunity.web.controller.monitor;

import com.msb.hjycommunity.common.core.controller.BaseController;
import com.msb.hjycommunity.common.core.domain.BaseResponse;
import com.msb.hjycommunity.common.core.page.PageResult;
import com.msb.hjycommunity.monitor.domain.SysUserOnline;
import com.msb.hjycommunity.monitor.service.SysUserOnlineService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 在线用户Controller
 */
@RestController
@RequestMapping("/monitor/online")
public class SysUserOnlineController extends BaseController {

    @Resource
    private SysUserOnlineService userOnlineService;

    /**
     * 获取在线用户列表
     */
    @GetMapping("/list")
    @PreAuthorize("@pe.hasPerms('monitor:online:list')")
    public PageResult list(SysUserOnline sysUserOnline) {
        startPage();
        List<SysUserOnline> list = userOnlineService.selectOnlineUserList(sysUserOnline);
        return getData(list);
    }

    /**
     * 强退用户
     */
    @DeleteMapping("/{tokenId}")
    @PreAuthorize("@pe.hasPerms('monitor:online:forceLogout')")
    public BaseResponse<?> forceLogout(@PathVariable String tokenId) {
        return toAjax(userOnlineService.forceLogout(tokenId));
    }
}
