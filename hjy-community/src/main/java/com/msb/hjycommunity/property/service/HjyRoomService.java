package com.msb.hjycommunity.property.service;

import com.msb.hjycommunity.property.domain.HjyRoom;
import com.msb.hjycommunity.property.domain.vo.HjyRoomVo;

import java.util.List;

/**
 * 房间Service接口
 */
public interface HjyRoomService {

    /**
     * 查询房间列表
     */
    List<HjyRoom> selectRoomList(HjyRoom room);

    /**
     * 根据ID查询房间
     */
    HjyRoom selectRoomById(Long roomId);

    /**
     * 新增房间
     */
    int insertRoom(HjyRoom room);

    /**
     * 修改房间
     */
    int updateRoom(HjyRoom room);

    /**
     * 删除房间
     */
    int deleteRoomById(Long roomId);

    /**
     * 批量删除房间
     */
    int deleteRoomByIds(Long[] roomIds);

    /**
     * 查询房间下拉列表
     */
    List<HjyRoomVo> selectRoomPullDown(Long unitId);
}
