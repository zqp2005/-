package com.msb.hjycommunity.property.service.impl;

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
        building.setCreateBy(SecurityUtils.getUserName());
        return buildingMapper.insertBuilding(building);
    }

    @Override
    @Transactional
    public int updateBuilding(HjyBuilding building) {
        building.setUpdateBy(SecurityUtils.getUserName());
        return buildingMapper.updateBuilding(building);
    }

    @Override
    @Transactional
    public int deleteBuildingById(Long buildingId) {
        return buildingMapper.deleteBuildingById(buildingId);
    }

    @Override
    @Transactional
    public int deleteBuildingByIds(Long[] buildingIds) {
        return buildingMapper.deleteBuildingByIds(buildingIds);
    }

    @Override
    public List<HjyBuildingVo> selectBuildingPullDown(Long communityId) {
        return buildingMapper.selectBuildingPullDown(communityId);
    }
}
