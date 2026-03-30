package com.msb.hjycommunity.monitor.service;

import com.msb.hjycommunity.monitor.domain.SysJobLog;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 调度日志Service接口
 */
public interface SysJobLogService {

    /**
     * 查询调度日志列表
     */
    List<SysJobLog> selectJobLogList(SysJobLog jobLog);

    /**
     * 根据ID查询调度日志
     */
    SysJobLog selectJobLogById(Long jobLogId);

    /**
     * 删除调度日志
     */
    int deleteJobLogById(Long jobLogId);

    /**
     * 清空调度日志
     */
    int cleanJobLog();

    /**
     * 导出调度日志
     */
    void export(SysJobLog jobLog, HttpServletResponse response);
}
