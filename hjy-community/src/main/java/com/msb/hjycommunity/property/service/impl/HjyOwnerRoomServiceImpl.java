package com.msb.hjycommunity.property.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.msb.hjycommunity.common.core.exception.CustomException;
import com.msb.hjycommunity.common.utils.SecurityUtils;
import com.msb.hjycommunity.property.domain.HjyOwnerRoom;
import com.msb.hjycommunity.property.domain.HjyOwnerRoomRecord;
import com.msb.hjycommunity.property.service.PropertyHierarchyValidator;
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
    @Resource private PropertyHierarchyValidator hierarchyValidator;

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
        // 先锁定房间，串行化同一房间的关系申请；当前读防止事务快照漏掉刚提交的申请。
        PropertyHierarchyValidator.require(roomMapper.lockRoom(ownerRoom.getRoomId()), "房间不存在");
        hierarchyValidator.binding(ownerRoom);
        // 同房间同人不可重复绑定（已驳回的除外，可重新提交）
        if (!ownerRoomMapper.selectActiveBindingIdsForUpdate(ownerRoom.getRoomId(), ownerRoom.getOwnerId()).isEmpty()) {
            throw new CustomException(500, "该业主与此房屋已存在绑定关系，不可重复绑定");
        }
        // 状态一律由审核流程控制
        ownerRoom.setRoomStatus(BINDING_AUDITING);
        ownerRoom.setCreateBy(SecurityUtils.getUserName());
        // 手写XML insert不走MyBatis-Plus主键策略，显式生成雪花ID
        ownerRoom.setOwnerRoomId(IdWorker.getId());
        return ownerRoomMapper.insertOwnerRoom(ownerRoom);
    }

    @Override
    @Transactional
    public int updateOwnerRoom(HjyOwnerRoom ownerRoom) {
        // 流转字段只允许动作接口修改，普通编辑一律忽略（与报修/投诉编辑降级同模式）
        ownerRoom.setExpectedState(null);
        ownerRoom.setCommunityId(null);
        ownerRoom.setBuildingId(null);
        ownerRoom.setUnitId(null);
        ownerRoom.setOwnerType(null); // 已核验身份不能借普通编辑替换
        ownerRoom.setRoomStatus(null);
        ownerRoom.setRoomId(null);
        ownerRoom.setOwnerId(null);
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
        if (ownerRoomIds == null || ownerRoomIds.length == 0) throw new CustomException(400, "请选择绑定记录");
        int changed = 0;
        for (Long ownerRoomId : new java.util.TreeSet<>(java.util.Arrays.asList(ownerRoomIds))) {
            HjyOwnerRoom ownerRoom = ownerRoomMapper.selectOwnerRoomById(ownerRoomId);
            if (ownerRoom == null) continue;
            if (!BINDING_BOUND.equals(ownerRoom.getRoomStatus()) && !BINDING_AUDITING.equals(ownerRoom.getRoomStatus())) {
                throw new CustomException(400, "仅允许解绑已绑定记录或撤回审核中申请");
            }
            if (ownerRoomMapper.deleteOwnerRoomIfState(ownerRoomId, ownerRoom.getRoomStatus()) != 1) {
                throw new CustomException(409, "绑定状态已变化，请刷新后重试");
            }
            HjyOwnerRoomRecord record = record(ownerRoom);
            record.setRecordAuditType(BINDING_BOUND.equals(ownerRoom.getRoomStatus()) ? "unbind" : "withdraw");
            record.setRecordAuditOpinion(BINDING_BOUND.equals(ownerRoom.getRoomStatus()) ? "解除绑定" : "撤回申请");
            if (ownerRoomMapper.insertRecord(record) != 1) throw new CustomException(500, "留痕失败");
            changed++;
        }
        return changed;
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
        update.setExpectedState(BINDING_AUDITING);
        int result = ownerRoomMapper.updateOwnerRoom(update);
        if (result != 1) throw new CustomException(409, "绑定状态已变化，请刷新后重试");

        // 审核留痕
        HjyOwnerRoomRecord record = new HjyOwnerRoomRecord();
        record.setRecordId(IdWorker.getId());
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
        if (ownerRoomMapper.insertRecord(record) != 1) throw new CustomException(500, "留痕失败");
        // 人房关联不代表实际入住，不修改房间入住或销售状态。
        return result;
    }

    private HjyOwnerRoomRecord record(HjyOwnerRoom relation) {
        HjyOwnerRoomRecord record = new HjyOwnerRoomRecord();
        record.setRecordId(IdWorker.getId());
        record.setOwnerRoomId(String.valueOf(relation.getOwnerRoomId()));
        record.setRoomId(relation.getRoomId());
        record.setCommunityId(relation.getCommunityId());
        record.setBuildingId(relation.getBuildingId());
        record.setUnitId(relation.getUnitId());
        record.setOwnerId(relation.getOwnerId());
        record.setOwnerType(relation.getOwnerType());
        record.setRoomStatus(relation.getRoomStatus());
        record.setCreateBy(SecurityUtils.getUserName());
        return record;
    }
}
