package com.msb.hjycommunity.monitor.controller;

import com.msb.hjycommunity.common.core.controller.BaseController;
import com.msb.hjycommunity.common.core.domain.BaseResponse;
import com.msb.hjycommunity.monitor.service.SysServerService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 服务器监控Controller
 */
@RestController
@RequestMapping("/monitor/server")
public class SysServerController extends BaseController {

    @Resource
    private SysServerService serverService;

    /**
     * 获取服务器信息
     */
    @GetMapping
    @PreAuthorize("@pe.hasPerms('monitor:server:list')")
    public BaseResponse<Map<String, Object>> getServer() {
        return BaseResponse.success(serverService.getServerInfo());
    }
}
