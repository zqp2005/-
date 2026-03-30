package com.msb.hjycommunity.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.msb.hjycommunity.monitor.domain.SysJobLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 调度日志Mapper接口
 */
public interface SysJobLogMapper extends BaseMapper<SysJobLog> {

    /**
     * 查询调度日志列表
     */
    List<SysJobLog> selectJobLogList(SysJobLog jobLog);

    /**
     * 根据ID查询调度日志
     */
    SysJobLog selectJobLogById(@Param("jobLogId") Long jobLogId);

    /**
     * 删除调度日志
     */
    int deleteJobLogById(@Param("jobLogId") Long jobLogId);

    /**
     * 清空调度日志
     */
    int cleanJobLog();
}
