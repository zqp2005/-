package com.msb.hjycommunity.property.service;

import com.msb.hjycommunity.property.domain.HjyRepair;

import java.util.List;

/**
 * 报修Service接口
 */
public interface HjyRepairService {

    List<com.msb.hjycommunity.property.domain.vo.RepairWorkerVo> selectEligibleWorkers();

    /**
     * 查询报修列表
     */
    List<HjyRepair> selectRepairList(HjyRepair repair);

    /**
     * 根据ID查询报修
     */
    HjyRepair selectRepairById(Long repairId);

    /**
     * 新增报修
     */
    int insertRepair(HjyRepair repair);

    /**
     * 修改报修
     */
    int updateRepair(HjyRepair repair);

    /**
     * 删除报修
     */
    int deleteRepairById(Long repairId);

    /**
     * 批量删除报修
     */
    int deleteRepairByIds(Long[] repairIds);

    /** 派单：Pending -> Allocated，记录派单时间与维修人 */
    int assignRepair(Long repairId, Long assignmentId);

    /** 接单：Allocated -> Processing，记录接单时间 */
    int receiveRepair(Long repairId);

    /** 完成：Processing -> Processed，记录完成时间与完成人 */
    int completeRepair(Long repairId);

    /** 取消：Pending/Allocated -> Cancelled，记录取消时间与原因 */
    int cancelRepair(Long repairId, String reason);

    /** 不处理：Pending -> No_Processed，记录原因 */
    int rejectRepair(Long repairId, String reason);
}
