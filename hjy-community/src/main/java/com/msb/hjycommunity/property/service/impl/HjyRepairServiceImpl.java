package com.msb.hjycommunity.property.service.impl;

import com.msb.hjycommunity.common.core.exception.CustomException;
import com.msb.hjycommunity.common.utils.SecurityUtils;
import com.msb.hjycommunity.property.constant.RepairState;
import com.msb.hjycommunity.property.domain.HjyRepair;
import com.msb.hjycommunity.property.mapper.HjyRepairMapper;
import com.msb.hjycommunity.property.service.HjyRepairService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 报修Service实现
 */
@Service
public class HjyRepairServiceImpl implements HjyRepairService {

    /** 大陆 11 位手机号 */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    @Resource
    private HjyRepairMapper repairMapper;

    @Resource private com.msb.hjycommunity.system.mapper.SysUserMapper userMapper;
    @Resource private com.msb.hjycommunity.framework.service.SysPermissionService permissionService;

    @Override
    public List<HjyRepair> selectRepairList(HjyRepair repair) {
        return repairMapper.selectRepairList(repair);
    }

    @Override
    public List<com.msb.hjycommunity.property.domain.vo.RepairWorkerVo> selectEligibleWorkers() {
        return repairMapper.selectEligibleWorkers();
    }

    @Override
    public HjyRepair selectRepairById(Long repairId) {
        return repairMapper.selectRepairById(repairId);
    }

    @Override
    @Transactional
    public int insertRepair(HjyRepair repair) {
        // 业主电话非空时校验手机号格式
        String phone = repair.getOwnerPhoneNumber();
        if (phone != null && !phone.trim().isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
            throw new CustomException(500, "手机号格式不正确");
        }
        repair.setCreateBy(SecurityUtils.getUserName());
        // 新建只接受业务资料，流程字段与编号由后端生成。
        repair.setRepairState(RepairState.PENDING);
        repair.setRepairNum("RX" + IdWorker.getId());
        repair.setAssignmentId(null);
        repair.setAssignmentTime(null);
        repair.setReceivingOrdersTime(null);
        repair.setCompleteId(null);
        repair.setCompleteName(null);
        repair.setCompletePhone(null);
        repair.setCompleteTime(null);
        repair.setCancelTime(null);
        return repairMapper.insertRepair(repair);
    }

    @Override
    @Transactional
    public int updateRepair(HjyRepair repair) {
        repair.setExpectedState(null);
        repair.setRepairNum(null);
        // 流转字段只允许动作接口修改，普通编辑一律忽略（mapper 动态 SQL 判空自动跳过）
        repair.setRepairState(null);
        repair.setAssignmentTime(null);
        repair.setAssignmentId(null);
        repair.setReceivingOrdersTime(null);
        repair.setCompleteId(null);
        repair.setCompleteName(null);
        repair.setCompletePhone(null);
        repair.setCompleteTime(null);
        repair.setCancelTime(null);
        repair.setUpdateBy(SecurityUtils.getUserName());
        return repairMapper.updateRepair(repair);
    }

    @Override
    @Transactional
    public int deleteRepairById(Long repairId) {
        return repairMapper.deleteRepairById(repairId);
    }

    @Override
    @Transactional
    public int deleteRepairByIds(Long[] repairIds) {
        return repairMapper.deleteRepairByIds(repairIds);
    }

    @Override
    @Transactional
    public int assignRepair(Long repairId, Long assignmentId) {
        com.msb.hjycommunity.system.domain.SysUser worker = assignmentId == null ? null : userMapper.selectUserById(assignmentId);
        if (worker == null || !"0".equals(worker.getStatus()) || !"0".equals(worker.getDelFlag())) {
            throw new CustomException(400, "请选择有效且启用的维修人员");
        }
        java.util.Set<String> permissions = permissionService.getMenuPermission(worker);
        if (permissions == null || !(permissions.contains("*:*:*")
                || (permissions.contains("system:repair:receive") && permissions.contains("system:repair:complete")))) {
            throw new CustomException(400, "所选人员没有接单及完成权限");
        }
        HjyRepair repair = getRepairOrThrow(repairId);
        assertCanTransit(repair, RepairState.ALLOCATED, "派单");

        HjyRepair update = new HjyRepair();
        update.setRepairId(repairId);
        update.setRepairState(RepairState.ALLOCATED);
        update.setAssignmentId(assignmentId);
        update.setAssignmentTime(new Date());
        update.setUpdateBy(SecurityUtils.getUserName());
        update.setExpectedState(repair.getRepairState());
        int changed = repairMapper.updateRepair(update);
        if (changed != 1) throw new CustomException(409, "状态已变化，请刷新后重试");
        return changed;
    }

    @Override
    @Transactional
    public int receiveRepair(Long repairId) {
        HjyRepair repair = getRepairOrThrow(repairId);
        assertAssignedWorker(repair);
        assertCanTransit(repair, RepairState.PROCESSING, "接单");

        HjyRepair update = new HjyRepair();
        update.setRepairId(repairId);
        update.setRepairState(RepairState.PROCESSING);
        update.setReceivingOrdersTime(new Date());
        update.setUpdateBy(SecurityUtils.getUserName());
        update.setExpectedState(repair.getRepairState());
        int changed = repairMapper.updateRepair(update);
        if (changed != 1) throw new CustomException(409, "状态已变化，请刷新后重试");
        return changed;
    }

    @Override
    @Transactional
    public int completeRepair(Long repairId) {
        HjyRepair repair = getRepairOrThrow(repairId);
        assertAssignedWorker(repair);
        assertCanTransit(repair, RepairState.PROCESSED, "完成");

        HjyRepair update = new HjyRepair();
        update.setRepairId(repairId);
        update.setRepairState(RepairState.PROCESSED);
        update.setCompleteTime(new Date());
        update.setCompleteId(SecurityUtils.getLoginUser().getUser().getUserId());
        update.setCompleteName(SecurityUtils.getUserName());
        update.setCompletePhone(SecurityUtils.getLoginUser().getUser().getPhonenumber());
        update.setUpdateBy(SecurityUtils.getUserName());
        update.setExpectedState(repair.getRepairState());
        int changed = repairMapper.updateRepair(update);
        if (changed != 1) throw new CustomException(409, "状态已变化，请刷新后重试");
        return changed;
    }

    @Override
    @Transactional
    public int cancelRepair(Long repairId, String reason) {
        if (reason == null || reason.trim().isEmpty()) throw new CustomException(400, "必须填写原因");
        HjyRepair repair = getRepairOrThrow(repairId);
        assertCanTransit(repair, RepairState.CANCELLED, "取消");

        HjyRepair update = new HjyRepair();
        update.setRepairId(repairId);
        update.setRepairState(RepairState.CANCELLED);
        update.setCancelTime(new Date());
        update.setRemark(reason);
        update.setUpdateBy(SecurityUtils.getUserName());
        update.setExpectedState(repair.getRepairState());
        int changed = repairMapper.updateRepair(update);
        if (changed != 1) throw new CustomException(409, "状态已变化，请刷新后重试");
        return changed;
    }

    @Override
    @Transactional
    public int rejectRepair(Long repairId, String reason) {
        if (reason == null || reason.trim().isEmpty()) throw new CustomException(400, "必须填写原因");
        HjyRepair repair = getRepairOrThrow(repairId);
        assertCanTransit(repair, RepairState.NO_PROCESSED, "不处理");

        HjyRepair update = new HjyRepair();
        update.setRepairId(repairId);
        update.setRepairState(RepairState.NO_PROCESSED);
        update.setRemark(reason);
        update.setUpdateBy(SecurityUtils.getUserName());
        update.setExpectedState(repair.getRepairState());
        int changed = repairMapper.updateRepair(update);
        if (changed != 1) throw new CustomException(409, "状态已变化，请刷新后重试");
        return changed;
    }

    private void assertAssignedWorker(HjyRepair repair) {
        Long current = SecurityUtils.getLoginUser().getUser().getUserId();
        if (current == null || !current.equals(repair.getAssignmentId())) {
            throw new CustomException(403, "仅被分派的维修人员可以接单或完成工单");
        }
    }

    /** 查询工单，不存在则抛业务异常 */
    private HjyRepair getRepairOrThrow(Long repairId) {
        HjyRepair repair = repairMapper.selectRepairById(repairId);
        if (repair == null) {
            throw new CustomException(500, "报修工单不存在");
        }
        return repair;
    }

    /** 校验当前状态允许流转到目标动作，否则抛业务异常 */
    private void assertCanTransit(HjyRepair repair, String targetState, String actionName) {
        if (!RepairState.canTransit(repair.getRepairState(), targetState)) {
            throw new CustomException(500,
                    String.format("工单[%s]当前状态为[%s]，无法%s", repair.getRepairNum(), repair.getRepairState(), actionName));
        }
    }
}
