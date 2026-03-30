package com.msb.hjycommunity.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.msb.hjycommunity.system.domain.SysNotice;

import java.util.List;

/**
 * 通知公告Mapper接口
 */
public interface SysNoticeMapper extends BaseMapper<SysNotice> {

    /**
     * 查询通知公告列表
     */
    List<SysNotice> selectNoticeList(SysNotice notice);

    /**
     * 根据ID查询通知公告
     */
    SysNotice selectNoticeById(Long noticeId);

    /**
     * 新增通知公告
     */
    int insertNotice(SysNotice notice);

    /**
     * 修改通知公告
     */
    int updateNotice(SysNotice notice);

    /**
     * 删除通知公告
     */
    int deleteNoticeById(Long noticeId);

    /**
     * 批量删除通知公告
     */
    int deleteNoticeByIds(Long[] noticeIds);
}
