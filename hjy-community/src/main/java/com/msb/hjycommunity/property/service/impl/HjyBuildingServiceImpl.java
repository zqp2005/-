package com.msb.hjycommunity.property.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.msb.hjycommunity.common.core.exception.CustomException;
import com.msb.hjycommunity.common.utils.SecurityUtils;
import com.msb.hjycommunity.property.domain.HjyBuilding;
import com.msb.hjycommunity.property.domain.vo.HjyBuildingVo;
import com.msb.hjycommunity.property.mapper.HjyBuildingMapper;
import com.msb.hjycommunity.property.service.HjyBuildingService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * 楼栋Service实现
 */
@Service
public class HjyBuildingServiceImpl implements HjyBuildingService {

    @Resource
    private com.msb.hjycommunity.property.service.PropertyHierarchyValidator hierarchyValidator;

    @Resource
    private HjyBuildingMapper buildingMapper;

    @Override
    public List<HjyBuilding> selectBuildingList(HjyBuilding building) {
        return buildingMapper.selectBuildingList(building);
    }

    @Override
    public HjyBuilding selectBuildingById(Long buildingId) {
        return buildingMapper.selectBuildingById(buildingId);
    }

    @Override
    @Transactional
    public int insertBuilding(HjyBuilding building) {
        hierarchyValidator.building(building, null);
        // 手写XML insert不走MyBatis-Plus主键策略，显式生成雪花ID
        building.setBuildingId(IdWorker.getId());
        building.setCreateBy(SecurityUtils.getUserName());
        return buildingMapper.insertBuilding(building);
    }

    @Override
    @Transactional
    public int updateBuilding(HjyBuilding building) {
        HjyBuilding existing = building.getBuildingId() == null ? null : buildingMapper.selectBuildingById(building.getBuildingId());
        com.msb.hjycommunity.property.service.PropertyHierarchyValidator.require(existing, "记录不存在");
        hierarchyValidator.building(building, existing);
        building.setUpdateBy(SecurityUtils.getUserName());
        return buildingMapper.updateBuilding(building);
    }

    @Override
    @Transactional
    public int deleteBuildingById(Long buildingId) {
        // 委托批量删除，复用级联删除校验
        return deleteBuildingByIds(new Long[]{buildingId});
    }

    @Override
    @Transactional
    public int deleteBuildingByIds(Long[] buildingIds) {
        // 级联删除校验：楼栋下存在单元则整批拒绝删除
        for (Long buildingId : buildingIds) {
            long unitCount = buildingMapper.countUnitByBuilding(buildingId);
            if (unitCount > 0) {
                HjyBuilding building = buildingMapper.selectBuildingById(buildingId);
                throw new CustomException(500, String.format("楼栋[%s]下存在 %d 个单元，无法删除",
                        building != null ? building.getBuildingName() : buildingId, unitCount));
            }
        }
        return buildingMapper.deleteBuildingByIds(buildingIds);
    }

    @Override
    public List<HjyBuildingVo> selectBuildingPullDown(Long communityId) {
        return buildingMapper.selectBuildingPullDown(communityId);
    }
}
