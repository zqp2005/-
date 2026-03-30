package com.msb.hjycommunity.monitor.controller;

import com.msb.hjycommunity.common.core.controller.BaseController;
import com.msb.hjycommunity.common.core.domain.BaseResponse;
import com.msb.hjycommunity.common.core.page.PageResult;
import com.msb.hjycommunity.monitor.domain.SysJobLog;
import com.msb.hjycommunity.monitor.service.SysJobLogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 调度日志Controller
 */
@RestController
@RequestMapping("/monitor/jobLog")
public class SysJobLogController extends BaseController {

    @Resource
    private SysJobLogService jobLogService;

    /**
     * 获取调度日志列表
     */
    @GetMapping("/list")
    @PreAuthorize("@pe.hasPerms('monitor:jobLog:list')")
    public PageResult list(SysJobLog jobLog) {
        startPage();
        List<SysJobLog> list = jobLogService.selectJobLogList(jobLog);
        return getData(list);
    }

    /**
     * 获取调度日志详情
     */
    @GetMapping("/{jobLogId}")
    @PreAuthorize("@pe.hasPerms('monitor:jobLog:query')")
    public BaseResponse<?> getInfo(@PathVariable Long jobLogId) {
        return BaseResponse.success(jobLogService.selectJobLogById(jobLogId));
    }

    /**
     * 删除调度日志
     */
    @DeleteMapping("/{jobLogId}")
    @PreAuthorize("@pe.hasPerms('monitor:jobLog:remove')")
    public BaseResponse<?> remove(@PathVariable Long jobLogId) {
        return toAjax(jobLogService.deleteJobLogById(jobLogId));
    }

    /**
     * 清空调度日志
     */
    @DeleteMapping("/clean")
    @PreAuthorize("@pe.hasPerms('monitor:jobLog:remove')")
    public BaseResponse<?> clean() {
        return toAjax(jobLogService.cleanJobLog());
    }

    /**
     * 导出调度日志
     */
    @GetMapping("/export")
    @PreAuthorize("@pe.hasPerms('monitor:jobLog:export')")
    public void export(SysJobLog jobLog, HttpServletResponse response) {
        jobLogService.export(jobLog, response);
    }
}
