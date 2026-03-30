package com.msb.hjycommunity.property.service;

import com.msb.hjycommunity.property.domain.HjyVisitor;

import java.util.List;

/**
 * 访客Service接口
 */
public interface HjyVisitorService {

    /**
     * 查询访客列表
     */
    List<HjyVisitor> selectVisitorList(HjyVisitor visitor);

    /**
     * 根据ID查询访客
     */
    HjyVisitor selectVisitorById(Long visitorId);

    /**
     * 新增访客
     */
    int insertVisitor(HjyVisitor visitor);

    /**
     * 修改访客
     */
    int updateVisitor(HjyVisitor visitor);

    /**
     * 删除访客
     */
    int deleteVisitorById(Long visitorId);

    /**
     * 批量删除访客
     */
    int deleteVisitorByIds(Long[] visitorIds);
}
