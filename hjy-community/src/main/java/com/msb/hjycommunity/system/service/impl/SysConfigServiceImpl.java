package com.msb.hjycommunity.system.service.impl;

import com.msb.hjycommunity.common.utils.RedisCache;
import com.msb.hjycommunity.common.utils.SecurityUtils;
import com.msb.hjycommunity.system.domain.SysConfig;
import com.msb.hjycommunity.system.mapper.SysConfigMapper;
import com.msb.hjycommunity.system.service.SysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 参数配置Service实现
 */
@Service
public class SysConfigServiceImpl implements SysConfigService {

    private static final String SYS_CONFIG_KEY = "sys_config:";

    @Resource
    private SysConfigMapper configMapper;

    @Autowired
    private RedisCache redisCache;

    @Override
    public List<SysConfig> selectConfigList(SysConfig config) {
        return configMapper.selectConfigList(config);
    }

    @Override
    public SysConfig selectConfigById(Long configId) {
        return configMapper.selectConfigById(configId);
    }

    @Override
    public String selectConfigByKey(String configKey) {
        String configValue = redisCache.getCacheObject(SYS_CONFIG_KEY + configKey);
        if (configValue != null) {
            return configValue;
        }
        SysConfig config = configMapper.selectConfigByKey(configKey);
        if (config != null) {
            redisCache.setCacheObject(SYS_CONFIG_KEY + configKey, config.getConfigValue());
            return config.getConfigValue();
        }
        return "";
    }

    @Override
    @Transactional
    public int insertConfig(SysConfig config) {
        config.setCreateBy(SecurityUtils.getUserName());
        int result = configMapper.insertConfig(config);
        if (result > 0) {
            redisCache.setCacheObject(SYS_CONFIG_KEY + config.getConfigKey(), config.getConfigValue());
        }
        return result;
    }

    @Override
    @Transactional
    public int updateConfig(SysConfig config) {
        config.setUpdateBy(SecurityUtils.getUserName());
        int result = configMapper.updateConfig(config);
        if (result > 0) {
            redisCache.setCacheObject(SYS_CONFIG_KEY + config.getConfigKey(), config.getConfigValue());
        }
        return result;
    }

    @Override
    @Transactional
    public int deleteConfigById(Long configId) {
        SysConfig config = configMapper.selectConfigById(configId);
        if (config != null) {
            redisCache.deleteObject(SYS_CONFIG_KEY + config.getConfigKey());
        }
        return configMapper.deleteConfigById(configId);
    }

    @Override
    @Transactional
    public int deleteConfigByIds(Long[] configIds) {
        for (Long configId : configIds) {
            SysConfig config = configMapper.selectConfigById(configId);
            if (config != null) {
                redisCache.deleteObject(SYS_CONFIG_KEY + config.getConfigKey());
            }
        }
        return configMapper.deleteConfigByIds(configIds);
    }

    @Override
    @Transactional
    public void clearCache() {
        redisCache.deleteObject(SYS_CONFIG_KEY);
    }
}
