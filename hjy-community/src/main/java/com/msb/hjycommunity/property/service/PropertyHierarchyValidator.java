package com.msb.hjycommunity.property.service;

import com.msb.hjycommunity.common.core.exception.CustomException;
import com.msb.hjycommunity.community.mapper.HjyCommunityMapper;
import com.msb.hjycommunity.property.domain.*;
import com.msb.hjycommunity.property.mapper.*;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.Objects;

/** 冗余归属来自父级；普通编辑不能迁移父级，避免留下跨小区的子记录。 */
@Component
public class PropertyHierarchyValidator {
    @Resource private HjyCommunityMapper communityMapper;
    @Resource private HjyBuildingMapper buildingMapper;
    @Resource private HjyUnitMapper unitMapper;
    @Resource private HjyRoomMapper roomMapper;
    @Resource private HjyOwnerMapper ownerMapper;

    public void building(HjyBuilding value, HjyBuilding existing) {
        if (existing != null) {
            same(value.getCommunityId(), existing.getCommunityId());
            value.setCommunityId(existing.getCommunityId());
        }
        requireCommunity(value.getCommunityId());
    }

    public void unit(HjyUnit value, HjyUnit existing) {
        if (existing != null) {
            same(value.getBuildingId(), existing.getBuildingId());
            same(value.getCommunityId(), existing.getCommunityId());
            value.setBuildingId(existing.getBuildingId());
        }
        HjyBuilding parent = value.getBuildingId() == null ? null : buildingMapper.selectBuildingById(value.getBuildingId());
        require(parent, "楼栋不存在");
        requireCommunity(parent.getCommunityId());
        same(value.getCommunityId(), parent.getCommunityId());
        value.setCommunityId(parent.getCommunityId());
    }

    public void room(HjyRoom value, HjyRoom existing) {
        if (existing != null) {
            same(value.getUnitId(), existing.getUnitId());
            same(value.getBuildingId(), existing.getBuildingId());
            same(value.getCommunityId(), existing.getCommunityId());
            value.setUnitId(existing.getUnitId());
        }
        HjyUnit parent = value.getUnitId() == null ? null : unitMapper.selectUnitById(value.getUnitId());
        require(parent, "单元不存在");
        unit(parent, null);
        same(value.getBuildingId(), parent.getBuildingId());
        same(value.getCommunityId(), parent.getCommunityId());
        value.setBuildingId(parent.getBuildingId());
        value.setCommunityId(parent.getCommunityId());
    }

    public void binding(HjyOwnerRoom value) {
        HjyRoom parent = value.getRoomId() == null ? null : roomMapper.selectRoomById(value.getRoomId());
        require(parent, "房间不存在");
        room(parent, null);
        require(value.getOwnerId() == null ? null : ownerMapper.selectOwnerById(value.getOwnerId()), "业主不存在");
        same(value.getCommunityId(), parent.getCommunityId());
        same(value.getBuildingId(), parent.getBuildingId());
        same(value.getUnitId(), parent.getUnitId());
        value.setCommunityId(parent.getCommunityId());
        value.setBuildingId(parent.getBuildingId());
        value.setUnitId(parent.getUnitId());
    }

    private void requireCommunity(Long id) {
        require(id == null ? null : communityMapper.selectById(id), "小区不存在");
    }

    public static void require(Object value, String message) {
        if (value == null) throw new CustomException(400, message);
    }

    private void same(Long supplied, Long actual) {
        if (supplied != null && !Objects.equals(supplied, actual)) {
            throw new CustomException(400, "归属链不一致，普通编辑不支持迁移小区、楼栋或单元");
        }
    }
}
