package com.msb.hjycommunity.property.service.impl;

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
        return ownerMapper.deleteOwnerById(ownerId);
    }

    @Override
    @Transactional
    public int deleteOwnerByIds(Long[] ownerIds) {
        return ownerMapper.deleteOwnerByIds(ownerIds);
    }

    @Override
    public HjyOwner selectOwnerByPhone(String ownerPhoneNumber) {
        return ownerMapper.selectOwnerByPhone(ownerPhoneNumber);
    }
}
