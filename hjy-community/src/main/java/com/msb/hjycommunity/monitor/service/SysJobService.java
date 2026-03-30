package com.msb.hjycommunity.monitor.service;

import com.msb.hjycommunity.monitor.domain.SysJob;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 定时任务Service接口
 */
public interface SysJobService {

    /**
     * 查询定时任务列表
     */
    List<SysJob> selectJobList(SysJob job);

    /**
     * 根据ID查询定时任务
     */
    SysJob selectJobById(Long jobId);

    /**
     * 新增定时任务
     */
    int insertJob(SysJob job);

    /**
     * 修改定时任务
     */
    int updateJob(SysJob job);

    /**
     * 删除定时任务
     */
    int deleteJobById(Long jobId);

    /**
     * 批量删除定时任务
     */
    int deleteJobByIds(Long[] jobIds);

    /**
     * 修改任务状态
     */
    int changeJobStatus(SysJob job);

    /**
     * 立即执行一次
     */
    int runJob(SysJob job);

    /**
     * 导出定时任务
     */
    void export(SysJob job, HttpServletResponse response);
}
