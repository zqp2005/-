package com.msb.hjycommunity.property.service.impl;

import com.msb.hjycommunity.common.utils.SecurityUtils;
import com.msb.hjycommunity.property.domain.HjyRoom;
import com.msb.hjycommunity.property.domain.vo.HjyRoomVo;
import com.msb.hjycommunity.property.mapper.HjyRoomMapper;
import com.msb.hjycommunity.property.service.HjyRoomService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 房间Service实现
 */
@Service
public class HjyRoomServiceImpl implements HjyRoomService {

    @Resource
    private HjyRoomMapper roomMapper;

    @Override
    public List<HjyRoom> selectRoomList(HjyRoom room) {
        return roomMapper.selectRoomList(room);
    }

    @Override
    public HjyRoom selectRoomById(Long roomId) {
        return roomMapper.selectRoomById(roomId);
    }

    @Override
    @Transactional
    public int insertRoom(HjyRoom room) {
        room.setCreateBy(SecurityUtils.getUserName());
        return roomMapper.insertRoom(room);
    }

    @Override
    @Transactional
    public int updateRoom(HjyRoom room) {
        room.setUpdateBy(SecurityUtils.getUserName());
        return roomMapper.updateRoom(room);
    }

    @Override
    @Transactional
    public int deleteRoomById(Long roomId) {
        return roomMapper.deleteRoomById(roomId);
    }

    @Override
    @Transactional
    public int deleteRoomByIds(Long[] roomIds) {
        return roomMapper.deleteRoomByIds(roomIds);
    }

    @Override
    public List<HjyRoomVo> selectRoomPullDown(Long unitId) {
        return roomMapper.selectRoomPullDown(unitId);
    }
}
