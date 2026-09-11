package com.msb.hjycommunity.property.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.msb.hjycommunity.common.core.exception.CustomException;
import com.msb.hjycommunity.common.utils.SecurityUtils;
import com.msb.hjycommunity.property.domain.HjyOwner;
import com.msb.hjycommunity.property.mapper.HjyOwnerMapper;
import com.msb.hjycommunity.property.service.HjyOwnerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 业主Service实现
 */
@Service
public class HjyOwnerServiceImpl implements HjyOwnerService {

    @Resource
    private HjyOwnerMapper ownerMapper;

    @Override
    public List<HjyOwner> selectOwnerList(HjyOwner owner) {
        return ownerMapper.selectOwnerList(owner);
    }

    @Override
    public HjyOwner selectOwnerById(Long ownerId) {
        return ownerMapper.selectOwnerById(ownerId);
    }

    @Override
    @Transactional
    public int insertOwner(HjyOwner owner) {
        // 手写XML insert不走MyBatis-Plus主键策略，显式生成雪花ID
        owner.setOwnerId(IdWorker.getId());
        owner.setCreateBy(SecurityUtils.getUserName());
        return ownerMapper.insertOwner(owner);
    }

    @Override
    @Transactional
    public int updateOwner(HjyOwner owner) {
        owner.setUpdateBy(SecurityUtils.getUserName());
        return ownerMapper.updateOwner(owner);
    }

    @Override
    @Transactional
    public int deleteOwnerById(Long ownerId) {
        // 委托批量删除，复用级联删除校验
        return deleteOwnerByIds(new Long[]{ownerId});
    }

    @Override
    @Transactional
    public int deleteOwnerByIds(Long[] ownerIds) {
        // 级联删除校验：业主名下存在有效房屋绑定（已驳回的除外）则整批拒绝删除
        for (Long ownerId : ownerIds) {
            long bindingCount = ownerMapper.countActiveBindingByOwner(ownerId);
            if (bindingCount > 0) {
                HjyOwner owner = ownerMapper.selectOwnerById(ownerId);
                throw new CustomException(500, String.format("业主[%s]名下存在 %d 条房屋绑定，无法删除，请先解绑",
                        owner != null ? owner.getOwnerRealName() : ownerId, bindingCount));
            }
        }
        return ownerMapper.deleteOwnerByIds(ownerIds);
    }

    @Override
    public HjyOwner selectOwnerByPhone(String ownerPhoneNumber) {
        return ownerMapper.selectOwnerByPhone(ownerPhoneNumber);
    }
}
