package com.msb.hjycommunity.property.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.msb.hjycommunity.property.domain.HjyOwner;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 业主Mapper接口
 */
public interface HjyOwnerMapper extends BaseMapper<HjyOwner> {

    /**
     * 查询业主列表
     */
    List<HjyOwner> selectOwnerList(HjyOwner owner);

    /**
     * 根据ID查询业主
     */
    HjyOwner selectOwnerById(Long ownerId);

    /**
     * 新增业主
     */
    int insertOwner(HjyOwner owner);

    /**
     * 修改业主
     */
    int updateOwner(HjyOwner owner);

    /**
     * 删除业主
     */
    int deleteOwnerById(Long ownerId);

    /**
     * 批量删除业主
     */
    int deleteOwnerByIds(Long[] ownerIds);

    /**
     * 根据手机号查询业主
     */
    HjyOwner selectOwnerByPhone(@Param("ownerPhoneNumber") String ownerPhoneNumber);
}
