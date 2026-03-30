package com.msb.hjycommunity.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.msb.hjycommunity.system.domain.SysConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 参数配置Mapper接口
 */
public interface SysConfigMapper extends BaseMapper<SysConfig> {

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
    SysConfig selectConfigByKey(@Param("configKey") String configKey);

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
