package com.msb.hjycommunity.system.service.impl;

import com.msb.hjycommunity.common.utils.SecurityUtils;
import com.msb.hjycommunity.system.domain.SysNotice;
import com.msb.hjycommunity.system.mapper.SysNoticeMapper;
import com.msb.hjycommunity.system.service.SysNoticeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 通知公告Service实现
 */
@Service
public class SysNoticeServiceImpl implements SysNoticeService {

    @Resource
    private SysNoticeMapper noticeMapper;

    @Override
    public List<SysNotice> selectNoticeList(SysNotice notice) {
        return noticeMapper.selectNoticeList(notice);
    }

    @Override
    public SysNotice selectNoticeById(Long noticeId) {
        return noticeMapper.selectNoticeById(noticeId);
    }

    @Override
    @Transactional
    public int insertNotice(SysNotice notice) {
        notice.setCreateBy(SecurityUtils.getUserName());
        return noticeMapper.insertNotice(notice);
    }

    @Override
    @Transactional
    public int updateNotice(SysNotice notice) {
        notice.setUpdateBy(SecurityUtils.getUserName());
        return noticeMapper.updateNotice(notice);
    }

    @Override
    @Transactional
    public int deleteNoticeById(Long noticeId) {
        return noticeMapper.deleteNoticeById(noticeId);
    }

    @Override
    @Transactional
    public int deleteNoticeByIds(Long[] noticeIds) {
        return noticeMapper.deleteNoticeByIds(noticeIds);
    }
}
