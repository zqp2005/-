package com.msb.hjycommunity.property.service.impl;

import com.msb.hjycommunity.common.utils.SecurityUtils;
import com.msb.hjycommunity.property.domain.HjyRepair;
import com.msb.hjycommunity.property.mapper.HjyRepairMapper;
import com.msb.hjycommunity.property.service.HjyRepairService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 报修Service实现
 */
@Service
public class HjyRepairServiceImpl implements HjyRepairService {

    @Resource
    private HjyRepairMapper repairMapper;

    @Override
    public List<HjyRepair> selectRepairList(HjyRepair repair) {
        return repairMapper.selectRepairList(repair);
    }

    @Override
    public HjyRepair selectRepairById(Long repairId) {
        return repairMapper.selectRepairById(repairId);
    }

    @Override
    @Transactional
    public int insertRepair(HjyRepair repair) {
        repair.setCreateBy(SecurityUtils.getUserName());
        return repairMapper.insertRepair(repair);
    }

    @Override
    @Transactional
    public int updateRepair(HjyRepair repair) {
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
}
