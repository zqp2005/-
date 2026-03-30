package com.msb.hjycommunity.property.service;

import com.msb.hjycommunity.property.domain.HjyUnit;
import com.msb.hjycommunity.property.domain.vo.HjyUnitVo;

import java.util.List;

/**
 * 单元Service接口
 */
public interface HjyUnitService {

    /**
     * 查询单元列表
     */
    List<HjyUnit> selectUnitList(HjyUnit unit);

    /**
     * 根据ID查询单元
     */
    HjyUnit selectUnitById(Long unitId);

    /**
     * 新增单元
     */
    int insertUnit(HjyUnit unit);

    /**
     * 修改单元
     */
    int updateUnit(HjyUnit unit);

    /**
     * 删除单元
     */
    int deleteUnitById(Long unitId);

    /**
     * 批量删除单元
     */
    int deleteUnitByIds(Long[] unitIds);

    /**
     * 查询单元下拉列表
     */
    List<HjyUnitVo> selectUnitPullDown(Long buildingId);
}
