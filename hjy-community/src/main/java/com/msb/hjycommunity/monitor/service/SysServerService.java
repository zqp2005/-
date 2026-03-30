package com.msb.hjycommunity.monitor.service;

import java.util.Map;

/**
 * 服务器监控Service接口
 */
public interface SysServerService {

    /**
     * 获取服务器信息
     */
    Map<String, Object> getServerInfo();
}
