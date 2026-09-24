package com.msb.hjycommunity.property.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.msb.hjycommunity.common.core.exception.CustomException;
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
    private com.msb.hjycommunity.property.service.PropertyHierarchyValidator hierarchyValidator;

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
        hierarchyValidator.room(room, null);
        // 手写XML insert不走MyBatis-Plus主键策略，显式生成雪花ID
        room.setRoomId(IdWorker.getId());
        room.setCreateBy(SecurityUtils.getUserName());
        return roomMapper.insertRoom(room);
    }

    @Override
    @Transactional
    public int updateRoom(HjyRoom room) {
        HjyRoom existing = room.getRoomId() == null ? null : roomMapper.selectRoomById(room.getRoomId());
        com.msb.hjycommunity.property.service.PropertyHierarchyValidator.require(existing, "记录不存在");
        hierarchyValidator.room(room, existing);
        room.setUpdateBy(SecurityUtils.getUserName());
        return roomMapper.updateRoom(room);
    }

    @Override
    @Transactional
    public int deleteRoomById(Long roomId) {
        // 委托批量删除，复用级联删除校验
        return deleteRoomByIds(new Long[]{roomId});
    }

    @Override
    @Transactional
    public int deleteRoomByIds(Long[] roomIds) {
        // 级联删除校验：房间存在有效业主绑定（已驳回的除外）则整批拒绝删除
        for (Long roomId : roomIds) {
            long bindingCount = roomMapper.countActiveBindingByRoom(roomId);
            if (bindingCount > 0) {
                HjyRoom room = roomMapper.selectRoomById(roomId);
                throw new CustomException(500, String.format("房间[%s]存在 %d 条业主绑定关系，无法删除，请先解绑",
                        room != null ? room.getRoomName() : roomId, bindingCount));
            }
        }
        return roomMapper.deleteRoomByIds(roomIds);
    }

    @Override
    public List<HjyRoomVo> selectRoomPullDown(Long unitId) {
        return roomMapper.selectRoomPullDown(unitId);
    }
}
