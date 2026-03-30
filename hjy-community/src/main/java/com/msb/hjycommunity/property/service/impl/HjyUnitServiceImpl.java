package com.msb.hjycommunity.property.service.impl;

import com.msb.hjycommunity.common.utils.SecurityUtils;
import com.msb.hjycommunity.property.domain.HjyUnit;
import com.msb.hjycommunity.property.domain.vo.HjyUnitVo;
import com.msb.hjycommunity.property.mapper.HjyUnitMapper;
import com.msb.hjycommunity.property.service.HjyUnitService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 单元Service实现
 */
@Service
public class HjyUnitServiceImpl implements HjyUnitService {

    @Resource
    private HjyUnitMapper unitMapper;

    @Override
    public List<HjyUnit> selectUnitList(HjyUnit unit) {
        return unitMapper.selectUnitList(unit);
    }

    @Override
    public HjyUnit selectUnitById(Long unitId) {
        return unitMapper.selectUnitById(unitId);
    }

    @Override
    @Transactional
    public int insertUnit(HjyUnit unit) {
        unit.setCreateBy(SecurityUtils.getUserName());
        return unitMapper.insertUnit(unit);
    }

    @Override
    @Transactional
    public int updateUnit(HjyUnit unit) {
        unit.setUpdateBy(SecurityUtils.getUserName());
        return unitMapper.updateUnit(unit);
    }

    @Override
    @Transactional
    public int deleteUnitById(Long unitId) {
        return unitMapper.deleteUnitById(unitId);
    }

    @Override
    @Transactional
    public int deleteUnitByIds(Long[] unitIds) {
        return unitMapper.deleteUnitByIds(unitIds);
    }

    @Override
    public List<HjyUnitVo> selectUnitPullDown(Long buildingId) {
        return unitMapper.selectUnitPullDown(buildingId);
    }
}
