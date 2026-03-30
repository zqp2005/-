package com.msb.hjycommunity.monitor.domain;

import java.util.List;
import java.util.Map;

/**
 * 缓存信息
 */
public class SysCache {

    /** 缓存名称 */
    private String name;

    /** 缓存键数量 */
    private Integer cacheSize;

    /** 键列表 */
    private List<Map<String, String>> keys;
}
