package com.msb.hjycommunity.property.service;

import com.msb.hjycommunity.property.domain.HjyOwnerRoom;
import com.msb.hjycommunity.property.domain.HjyOwnerRoomRecord;

import java.util.List;

/**
 * 房屋绑定Service接口
 */
public interface HjyOwnerRoomService {

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
    List<HjyOwnerRoomRecord> selectRecordList(Long ownerRoomId);

    /**
     * 新增审核记录并更新状态
     */
    int auditOwnerRoom(HjyOwnerRoom ownerRoom, HjyOwnerRoomRecord record);
}
