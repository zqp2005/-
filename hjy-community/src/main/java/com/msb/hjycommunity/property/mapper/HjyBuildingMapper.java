package com.msb.hjycommunity.property.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.msb.hjycommunity.property.domain.HjyBuilding;
import com.msb.hjycommunity.property.domain.vo.HjyBuildingVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 楼栋Mapper接口
 */
public interface HjyBuildingMapper extends BaseMapper<HjyBuilding> {

    /**
     * 查询楼栋列表
     */
    List<HjyBuilding> selectBuildingList(HjyBuilding building);

    /**
     * 根据ID查询楼栋
     */
    HjyBuilding selectBuildingById(Long buildingId);

    /**
     * 新增楼栋
     */
    int insertBuilding(HjyBuilding building);

    /**
     * 修改楼栋
     */
    int updateBuilding(HjyBuilding building);

    /**
     * 删除楼栋
     */
    int deleteBuildingById(Long buildingId);

    /**
     * 批量删除楼栋
     */
    int deleteBuildingByIds(Long[] buildingIds);

    /**
     * 查询楼栋下拉列表
     */
    List<HjyBuildingVo> selectBuildingPullDown(@Param("communityId") Long communityId);
}
