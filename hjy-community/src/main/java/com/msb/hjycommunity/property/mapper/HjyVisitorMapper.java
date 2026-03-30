package com.msb.hjycommunity.property.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.msb.hjycommunity.property.domain.HjyVisitor;

import java.util.List;

/**
 * 访客Mapper接口
 */
public interface HjyVisitorMapper extends BaseMapper<HjyVisitor> {

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
