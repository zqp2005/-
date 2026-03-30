package com.msb.hjycommunity.monitor.controller;

import com.msb.hjycommunity.common.core.controller.BaseController;
import com.msb.hjycommunity.common.core.domain.BaseResponse;
import com.msb.hjycommunity.monitor.service.SysCacheService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 缓存监控Controller
 */
@RestController
@RequestMapping("/monitor/cache")
public class SysCacheController extends BaseController {

    @Resource
    private SysCacheService cacheService;

    /**
     * 获取缓存信息
     */
    @GetMapping
    @PreAuthorize("@pe.hasPerms('monitor:cache:list')")
    public BaseResponse<Map<String, Object>> getCache() {
        return BaseResponse.success(cacheService.getCache());
    }
}
