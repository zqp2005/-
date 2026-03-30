package com.msb.hjycommunity.property.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.msb.hjycommunity.property.domain.HjyOwnerRoom;
import com.msb.hjycommunity.property.domain.HjyOwnerRoomRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 房屋绑定Mapper接口
 */
public interface HjyOwnerRoomMapper extends BaseMapper<HjyOwnerRoom> {

    /**
     * 查询房屋绑定列表
     */
    List<HjyOwnerRoom> selectOwnerRoomList(HjyOwnerRoom ownerRoom);

    /**
     * 根据ID查询房屋绑定
     */
    HjyOwnerRoom selectOwnerRoomById(Long ownerRoomId);

    /**
     * 新增房屋绑定
     */
    int insertOwnerRoom(HjyOwnerRoom ownerRoom);

    /**
     * 修改房屋绑定
     */
    int updateOwnerRoom(HjyOwnerRoom ownerRoom);

    /**
     * 删除房屋绑定
     */
    int deleteOwnerRoomById(Long ownerRoomId);

    /**
     * 批量删除房屋绑定
     */
    int deleteOwnerRoomByIds(Long[] ownerRoomIds);

    /**
     * 查询审核记录
     */
    List<HjyOwnerRoomRecord> selectRecordList(@Param("ownerRoomId") Long ownerRoomId);

    /**
     * 新增审核记录
     */
    int insertRecord(HjyOwnerRoomRecord record);
}
