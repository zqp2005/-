package com.msb.hjycommunity.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.msb.hjycommunity.monitor.domain.SysOperlog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 操作日志Mapper接口
 */
public interface SysOperlogMapper extends BaseMapper<SysOperlog> {

    /**
     * 查询操作日志列表
     */
    List<SysOperlog> selectOperlogList(SysOperlog operlog);

    /**
     * 删除操作日志
     */
    int deleteOperlogById(@Param("operId") Long operId);

    /**
     * 清空操作日志
     */
    int cleanOperlog();
}
