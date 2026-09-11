package com.msb.hjycommunity.property.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.msb.hjycommunity.common.core.exception.CustomException;
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
        // 手写XML insert不走MyBatis-Plus主键策略，显式生成雪花ID
        unit.setUnitId(IdWorker.getId());
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
        // 委托批量删除，复用级联删除校验
        return deleteUnitByIds(new Long[]{unitId});
    }

    @Override
    @Transactional
    public int deleteUnitByIds(Long[] unitIds) {
        // 级联删除校验：单元下存在房间则整批拒绝删除
        for (Long unitId : unitIds) {
            long roomCount = unitMapper.countRoomByUnit(unitId);
            if (roomCount > 0) {
                HjyUnit unit = unitMapper.selectUnitById(unitId);
                throw new CustomException(500, String.format("单元[%s]下存在 %d 个房间，无法删除",
                        unit != null ? unit.getUnitName() : unitId, roomCount));
            }
        }
        return unitMapper.deleteUnitByIds(unitIds);
    }

    @Override
    public List<HjyUnitVo> selectUnitPullDown(Long buildingId) {
        return unitMapper.selectUnitPullDown(buildingId);
    }
}
