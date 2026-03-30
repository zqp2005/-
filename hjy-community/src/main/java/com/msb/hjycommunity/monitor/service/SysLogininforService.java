package com.msb.hjycommunity.monitor.service;

import com.msb.hjycommunity.monitor.domain.SysLogininfor;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 登录日志Service接口
 */
public interface SysLogininforService {

    /**
     * 查询登录日志列表
     */
    List<SysLogininfor> selectLogininforList(SysLogininfor logininfor);

    /**
     * 删除登录日志
     */
    int deleteLogininforById(Long infoId);

    /**
     * 清空登录日志
     */
    int cleanLogininfor();

    /**
     * 导出登录日志
     */
    void export(SysLogininfor logininfor, HttpServletResponse response);

    /**
     * 记录登录信息
     */
    void insertLogininfor(SysLogininfor logininfor);
}
