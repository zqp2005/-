package com.msb.hjycommunity.monitor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.msb.hjycommunity.monitor.domain.SysLogininfor;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 登录日志Mapper接口
 */
public interface SysLogininforMapper extends BaseMapper<SysLogininfor> {

    /**
     * 查询登录日志列表
     */
    List<SysLogininfor> selectLogininforList(SysLogininfor logininfor);

    /**
     * 删除登录日志
     */
    int deleteLogininforById(@Param("infoId") Long infoId);

    /**
     * 清空登录日志
     */
    int cleanLogininfor();
}
