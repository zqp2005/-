package com.msb.hjycommunity.web.controller.monitor;

import com.msb.hjycommunity.common.core.controller.BaseController;
import com.msb.hjycommunity.common.core.domain.BaseResponse;
import com.msb.hjycommunity.common.core.page.PageResult;
import com.msb.hjycommunity.monitor.domain.SysLogininfor;
import com.msb.hjycommunity.monitor.service.SysLogininforService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 登录日志Controller
 */
@RestController
@RequestMapping("/monitor/logininfor")
public class SysLogininforController extends BaseController {

    @Resource
    private SysLogininforService logininforService;

    /**
     * 获取登录日志列表
     */
    @GetMapping("/list")
    @PreAuthorize("@pe.hasPerms('monitor:logininfor:list')")
    public PageResult list(SysLogininfor logininfor) {
        startPage();
        List<SysLogininfor> list = logininforService.selectLogininforList(logininfor);
        return getData(list);
    }

    /**
     * 删除登录日志
     */
    @DeleteMapping("/{infoId}")
    @PreAuthorize("@pe.hasPerms('monitor:logininfor:remove')")
    public BaseResponse<?> remove(@PathVariable Long infoId) {
        return toAjax(logininforService.deleteLogininforById(infoId));
    }

    /**
     * 清空登录日志
     */
    @DeleteMapping("/clean")
    @PreAuthorize("@pe.hasPerms('monitor:logininfor:remove')")
    public BaseResponse<?> clean() {
        return toAjax(logininforService.cleanLogininfor());
    }

    /**
     * 导出登录日志
     */
    @GetMapping("/export")
    @PreAuthorize("@pe.hasPerms('monitor:logininfor:export')")
    public void export(SysLogininfor logininfor, HttpServletResponse response) {
        logininforService.export(logininfor, response);
    }
}
