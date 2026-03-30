package com.msb.hjycommunity.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.msb.hjycommunity.monitor.domain.SysJob;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 定时任务Mapper接口
 */
public interface SysJobMapper extends BaseMapper<SysJob> {

    /**
     * 查询定时任务列表
     */
    List<SysJob> selectJobList(SysJob job);

    /**
     * 根据ID查询定时任务
     */
    SysJob selectJobById(@Param("jobId") Long jobId);

    /**
     * 新增定时任务
     */
    int insertJob(SysJob job);

    /**
     * 修改定时任务
     */
    int updateJob(SysJob job);

    /**
     * 删除定时任务
     */
    int deleteJobById(@Param("jobId") Long jobId);

    /**
     * 批量删除定时任务
     */
    int deleteJobByIds(@Param("jobIds") Long[] jobIds);
}
