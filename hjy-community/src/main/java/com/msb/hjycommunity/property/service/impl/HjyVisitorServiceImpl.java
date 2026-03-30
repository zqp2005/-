package com.msb.hjycommunity.property.service.impl;

import com.msb.hjycommunity.common.utils.SecurityUtils;
import com.msb.hjycommunity.property.domain.HjyVisitor;
import com.msb.hjycommunity.property.mapper.HjyVisitorMapper;
import com.msb.hjycommunity.property.service.HjyVisitorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 访客Service实现
 */
@Service
public class HjyVisitorServiceImpl implements HjyVisitorService {

    @Resource
    private HjyVisitorMapper visitorMapper;

    @Override
    public List<HjyVisitor> selectVisitorList(HjyVisitor visitor) {
        return visitorMapper.selectVisitorList(visitor);
    }

    @Override
    public HjyVisitor selectVisitorById(Long visitorId) {
        return visitorMapper.selectVisitorById(visitorId);
    }

    @Override
    @Transactional
    public int insertVisitor(HjyVisitor visitor) {
        visitor.setCreateBy(SecurityUtils.getUserName());
        return visitorMapper.insertVisitor(visitor);
    }

    @Override
    @Transactional
    public int updateVisitor(HjyVisitor visitor) {
        visitor.setUpdateBy(SecurityUtils.getUserName());
        return visitorMapper.updateVisitor(visitor);
    }

    @Override
    @Transactional
    public int deleteVisitorById(Long visitorId) {
        return visitorMapper.deleteVisitorById(visitorId);
    }

    @Override
    @Transactional
    public int deleteVisitorByIds(Long[] visitorIds) {
        return visitorMapper.deleteVisitorByIds(visitorIds);
    }
}
