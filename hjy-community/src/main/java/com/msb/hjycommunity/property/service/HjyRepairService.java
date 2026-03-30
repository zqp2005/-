package com.msb.hjycommunity.property.service;

import com.msb.hjycommunity.property.domain.HjyRepair;

import java.util.List;

/**
 * 报修Service接口
 */
public interface HjyRepairService {

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
}
