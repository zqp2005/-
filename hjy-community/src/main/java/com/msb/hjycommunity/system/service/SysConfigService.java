package com.msb.hjycommunity.system.service;

import com.msb.hjycommunity.system.domain.SysConfig;

import java.util.List;

/**
 * 参数配置Service接口
 */
public interface SysConfigService {

    /**
     * 查询参数配置列表
     */
    List<SysConfig> selectConfigList(SysConfig config);

    /**
     * 根据ID查询参数配置
     */
    SysConfig selectConfigById(Long configId);

    /**
     * 根据键名查询参数配置
     */
    String selectConfigByKey(String configKey);

    /**
     * 新增参数配置
     */
    int insertConfig(SysConfig config);

    /**
     * 修改参数配置
     */
    int updateConfig(SysConfig config);

    /**
     * 删除参数配置
     */
    int deleteConfigById(Long configId);

    /**
     * 批量删除参数配置
     */
    int deleteConfigByIds(Long[] configIds);

    /**
     * 清空缓存
     */
    void clearCache();
}
