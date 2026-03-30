package com.msb.hjycommunity.monitor.controller;

import com.msb.hjycommunity.common.core.controller.BaseController;
import com.msb.hjycommunity.common.core.domain.BaseResponse;
import com.msb.hjycommunity.common.core.page.PageResult;
import com.msb.hjycommunity.common.utils.SecurityUtils;
import com.msb.hjycommunity.monitor.domain.SysJob;
import com.msb.hjycommunity.monitor.service.SysJobService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 定时任务Controller
 */
@RestController
@RequestMapping("/monitor/job")
public class SysJobController extends BaseController {

    @Resource
    private SysJobService jobService;

    /**
     * 获取定时任务列表
     */
    @GetMapping("/list")
    @PreAuthorize("@pe.hasPerms('monitor:job:list')")
    public PageResult list(SysJob job) {
        startPage();
        List<SysJob> list = jobService.selectJobList(job);
        return getData(list);
    }

    /**
     * 获取定时任务详情
     */
    @GetMapping("/{jobId}")
    @PreAuthorize("@pe.hasPerms('monitor:job:query')")
    public BaseResponse<?> getInfo(@PathVariable Long jobId) {
        return BaseResponse.success(jobService.selectJobById(jobId));
    }

    /**
     * 新增定时任务
     */
    @PostMapping
    @PreAuthorize("@pe.hasPerms('monitor:job:add')")
    public BaseResponse<?> add(@RequestBody SysJob job) {
        job.setCreateBy(SecurityUtils.getUserName());
        return toAjax(jobService.insertJob(job));
    }

    /**
     * 修改定时任务
     */
    @PutMapping
    @PreAuthorize("@pe.hasPerms('monitor:job:edit')")
    public BaseResponse<?> edit(@RequestBody SysJob job) {
        job.setUpdateBy(SecurityUtils.getUserName());
        return toAjax(jobService.updateJob(job));
    }

    /**
     * 删除定时任务
     */
    @DeleteMapping("/{jobId}")
    @PreAuthorize("@pe.hasPerms('monitor:job:remove')")
    public BaseResponse<?> remove(@PathVariable Long jobId) {
        return toAjax(jobService.deleteJobById(jobId));
    }

    /**
     * 修改任务状态
     */
    @PutMapping("/changeStatus")
    @PreAuthorize("@pe.hasPerms('monitor:job:edit')")
    public BaseResponse<?> changeStatus(@RequestBody SysJob job) {
        job.setUpdateBy(SecurityUtils.getUserName());
        return toAjax(jobService.changeJobStatus(job));
    }

    /**
     * 立即执行一次
     */
    @PutMapping("/run")
    @PreAuthorize("@pe.hasPerms('monitor:job:edit')")
    public BaseResponse<?> run(@RequestBody SysJob job) {
        return toAjax(jobService.runJob(job));
    }

    /**
     * 导出定时任务
     */
    @GetMapping("/export")
    @PreAuthorize("@pe.hasPerms('monitor:job:export')")
    public void export(SysJob job, HttpServletResponse response) {
        jobService.export(job, response);
    }
}
