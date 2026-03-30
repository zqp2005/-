package com.msb.hjycommunity.monitor.controller;

import com.msb.hjycommunity.common.core.controller.BaseController;
import com.msb.hjycommunity.common.core.domain.BaseResponse;
import com.msb.hjycommunity.common.core.page.PageResult;
import com.msb.hjycommunity.common.utils.SecurityUtils;
import com.msb.hjycommunity.monitor.domain.SysOperlog;
import com.msb.hjycommunity.monitor.service.SysOperlogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 操作日志Controller
 */
@RestController
@RequestMapping("/monitor/operlog")
public class SysOperlogController extends BaseController {

    @Resource
    private SysOperlogService operlogService;

    /**
     * 获取操作日志列表
     */
    @GetMapping("/list")
    @PreAuthorize("@pe.hasPerms('monitor:operlog:list')")
    public PageResult list(SysOperlog operlog) {
        startPage();
        List<SysOperlog> list = operlogService.selectOperlogList(operlog);
        return getData(list);
    }

    /**
     * 删除操作日志
     */
    @DeleteMapping("/{operId}")
    @PreAuthorize("@pe.hasPerms('monitor:operlog:remove')")
    public BaseResponse<?> remove(@PathVariable Long operId) {
        return toAjax(operlogService.deleteOperlogById(operId));
    }

    /**
     * 清空操作日志
     */
    @DeleteMapping("/clean")
    @PreAuthorize("@pe.hasPerms('monitor:operlog:remove')")
    public BaseResponse<?> clean() {
        return toAjax(operlogService.cleanOperlog());
    }

    /**
     * 导出操作日志
     */
    @GetMapping("/export")
    @PreAuthorize("@pe.hasPerms('monitor:operlog:export')")
    public void export(SysOperlog operlog, HttpServletResponse response) {
        operlogService.export(operlog, response);
    }
}
