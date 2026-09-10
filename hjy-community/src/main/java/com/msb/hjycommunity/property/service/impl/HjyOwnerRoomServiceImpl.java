package com.msb.hjycommunity.property.service.impl;

import com.msb.hjycommunity.common.core.exception.CustomException;
import com.msb.hjycommunity.common.utils.SecurityUtils;
import com.msb.hjycommunity.property.domain.HjyOwnerRoom;
import com.msb.hjycommunity.property.domain.HjyOwnerRoomRecord;
import com.msb.hjycommunity.property.domain.HjyRoom;
import com.msb.hjycommunity.property.mapper.HjyOwnerRoomMapper;
import com.msb.hjycommunity.property.mapper.HjyRoomMapper;
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

    /** 绑定状态值（与前端字典一致） */
    private static final String BINDING_AUDITING = "Auditing";
    private static final String BINDING_BOUND = "Binding";
    private static final String BINDING_REJECTED = "Rejected";
    /** 房间状态（hjy_room_state 字典） */
    private static final String ROOM_HAS_STAY = "has_stay";
    private static final String ROOM_NONE_STAY = "none_stay";

    @Resource
    private HjyOwnerRoomMapper ownerRoomMapper;

    @Resource
    private HjyRoomMapper roomMapper;

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
        // 同房间同人不可重复绑定（已驳回的除外，可重新提交）
        if (ownerRoomMapper.countActiveBinding(ownerRoom.getRoomId(), ownerRoom.getOwnerId()) > 0) {
            throw new CustomException(500, "该业主与此房屋已存在绑定关系，不可重复绑定");
        }
        // 状态一律由审核流程控制
        ownerRoom.setRoomStatus(BINDING_AUDITING);
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
        // 委托批量删除，单条删除同样走解绑留痕语义
        return deleteOwnerRoomByIds(new Long[]{ownerRoomId});
    }

    @Override
    @Transactional
    public int deleteOwnerRoomByIds(Long[] ownerRoomIds) {
        // 解绑语义：已绑定的删除前留痕；删除后房间无人则置回未入住
        for (Long ownerRoomId : ownerRoomIds) {
            HjyOwnerRoom ownerRoom = ownerRoomMapper.selectOwnerRoomById(ownerRoomId);
            if (ownerRoom == null) {
                continue;
            }
            if (BINDING_BOUND.equals(ownerRoom.getRoomStatus())) {
                HjyOwnerRoomRecord record = new HjyOwnerRoomRecord();
                record.setOwnerRoomId(String.valueOf(ownerRoomId));
                record.setRoomId(ownerRoom.getRoomId());
                record.setCommunityId(ownerRoom.getCommunityId());
                record.setBuildingId(ownerRoom.getBuildingId());
                record.setUnitId(ownerRoom.getUnitId());
                record.setOwnerId(ownerRoom.getOwnerId());
                record.setOwnerType(ownerRoom.getOwnerType());
                record.setRecordAuditType("unbind");
                record.setRecordAuditOpinion("解除绑定");
                record.setCreateBy(SecurityUtils.getUserName());
                ownerRoomMapper.insertRecord(record);
            }
            ownerRoomMapper.deleteOwnerRoomById(ownerRoomId);
            syncRoomStatus(ownerRoom.getRoomId());
        }
        return ownerRoomIds.length;
    }

    @Override
    public List<HjyOwnerRoomRecord> selectRecordList(Long ownerRoomId) {
        return ownerRoomMapper.selectRecordList(ownerRoomId);
    }

    @Override
    @Transactional
    public int auditOwnerRoom(Long ownerRoomId, boolean pass, String auditOpinion) {
        if (auditOpinion == null || auditOpinion.trim().isEmpty()) {
            throw new CustomException(500, "审核意见不能为空");
        }
        HjyOwnerRoom ownerRoom = ownerRoomMapper.selectOwnerRoomById(ownerRoomId);
        if (ownerRoom == null) {
            throw new CustomException(500, "绑定记录不存在");
        }
        if (!BINDING_AUDITING.equals(ownerRoom.getRoomStatus())) {
            throw new CustomException(500, "该绑定当前状态为[" + ownerRoom.getRoomStatus() + "]，只有审核中的记录才能审核");
        }

        HjyOwnerRoom update = new HjyOwnerRoom();
        update.setOwnerRoomId(ownerRoomId);
        update.setRoomStatus(pass ? BINDING_BOUND : BINDING_REJECTED);
        update.setUpdateBy(SecurityUtils.getUserName());
        int result = ownerRoomMapper.updateOwnerRoom(update);

        // 审核留痕
        HjyOwnerRoomRecord record = new HjyOwnerRoomRecord();
        record.setOwnerRoomId(String.valueOf(ownerRoomId));
        record.setRoomId(ownerRoom.getRoomId());
        record.setCommunityId(ownerRoom.getCommunityId());
        record.setBuildingId(ownerRoom.getBuildingId());
        record.setUnitId(ownerRoom.getUnitId());
        record.setOwnerId(ownerRoom.getOwnerId());
        record.setOwnerType(ownerRoom.getOwnerType());
        record.setRecordAuditType(pass ? "pass" : "reject");
        record.setRecordAuditOpinion(auditOpinion);
        record.setCreateBy(SecurityUtils.getUserName());
        ownerRoomMapper.insertRecord(record);

        // 审核通过：房间状态联动为已入住
        if (pass) {
            syncRoomStatus(ownerRoom.getRoomId());
        }
        return result;
    }

    /** 房间入住状态联动：还有已绑定的人则已入住，否则未入住 */
    private void syncRoomStatus(Long roomId) {
        HjyRoom roomUpdate = new HjyRoom();
        roomUpdate.setRoomId(roomId);
        roomUpdate.setRoomStatus(
                ownerRoomMapper.countRoomBindings(roomId) > 0 ? ROOM_HAS_STAY : ROOM_NONE_STAY);
        roomMapper.updateRoom(roomUpdate);
    }
}
