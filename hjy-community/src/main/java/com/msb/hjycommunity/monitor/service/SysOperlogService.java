package com.msb.hjycommunity.monitor.service;

import com.msb.hjycommunity.monitor.domain.SysOperlog;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 操作日志Service接口
 */
public interface SysOperlogService {

    /**
     * 查询操作日志列表
     */
    List<SysOperlog> selectOperlogList(SysOperlog operlog);

    /**
     * 删除操作日志
     */
    int deleteOperlogById(Long operId);

    /**
     * 清空操作日志
     */
    int cleanOperlog();

    /**
     * 导出操作日志
     */
    void export(SysOperlog operlog, HttpServletResponse response);

    /**
     * 记录操作日志
     */
    void insertOperlog(SysOperlog operlog);
}
