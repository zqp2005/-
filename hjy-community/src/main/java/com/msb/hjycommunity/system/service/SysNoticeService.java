package com.msb.hjycommunity.system.service;

import com.msb.hjycommunity.system.domain.SysNotice;

import java.util.List;

/**
 * 通知公告Service接口
 */
public interface SysNoticeService {

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
