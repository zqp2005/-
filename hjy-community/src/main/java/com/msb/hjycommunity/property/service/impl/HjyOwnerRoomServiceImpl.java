package com.msb.hjycommunity.property.service.impl;

import com.msb.hjycommunity.common.utils.SecurityUtils;
import com.msb.hjycommunity.property.domain.HjyOwnerRoom;
import com.msb.hjycommunity.property.domain.HjyOwnerRoomRecord;
import com.msb.hjycommunity.property.mapper.HjyOwnerRoomMapper;
import com.msb.hjycommunity.property.service.HjyOwnerRoomService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 房屋绑定Service实现
 */
@Service
public class HjyOwnerRoomServiceImpl implements HjyOwnerRoomService {

    @Resource
    private HjyOwnerRoomMapper ownerRoomMapper;

    @Override
    public List<HjyOwnerRoom> selectOwnerRoomList(HjyOwnerRoom ownerRoom) {
        return ownerRoomMapper.selectOwnerRoomList(ownerRoom);
    }

    @Override
    public HjyOwnerRoom selectOwnerRoomById(Long ownerRoomId) {
        return ownerRoomMapper.selectOwnerRoomById(ownerRoomId);
    }

    @Override
    @Transactional
    public int insertOwnerRoom(HjyOwnerRoom ownerRoom) {
        ownerRoom.setCreateBy(SecurityUtils.getUserName());
        return ownerRoomMapper.insertOwnerRoom(ownerRoom);
    }

    @Override
    @Transactional
    public int updateOwnerRoom(HjyOwnerRoom ownerRoom) {
        ownerRoom.setUpdateBy(SecurityUtils.getUserName());
        return ownerRoomMapper.updateOwnerRoom(ownerRoom);
    }

    @Override
    @Transactional
    public int deleteOwnerRoomById(Long ownerRoomId) {
        return ownerRoomMapper.deleteOwnerRoomById(ownerRoomId);
    }

    @Override
    @Transactional
    public int deleteOwnerRoomByIds(Long[] ownerRoomIds) {
        return ownerRoomMapper.deleteOwnerRoomByIds(ownerRoomIds);
    }

    @Override
    public List<HjyOwnerRoomRecord> selectRecordList(Long ownerRoomId) {
        return ownerRoomMapper.selectRecordList(ownerRoomId);
    }

    @Override
    @Transactional
    public int auditOwnerRoom(HjyOwnerRoom ownerRoom, HjyOwnerRoomRecord record) {
        ownerRoom.setUpdateBy(SecurityUtils.getUserName());
        int result = ownerRoomMapper.updateOwnerRoom(ownerRoom);
        record.setOwnerRoomId(ownerRoom.getOwnerRoomId());
        record.setCreateBy(SecurityUtils.getUserName());
        ownerRoomMapper.insertRecord(record);
        return result;
    }
}
