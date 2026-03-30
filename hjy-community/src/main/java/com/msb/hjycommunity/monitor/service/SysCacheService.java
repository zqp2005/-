package com.msb.hjycommunity.monitor.service;

import java.util.Map;

/**
 * 缓存监控Service接口
 */
public interface SysCacheService {

    /**
     * 获取缓存监控信息
     */
    Map<String, Object> getCache();
}
