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
import java.util.regex.Pattern;

/**
 * 业主Service实现
 */
@Service
public class HjyOwnerServiceImpl implements HjyOwnerService {

    /** 大陆 11 位手机号 */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    /** 18 位身份证号（末位数字或 X，大小写均允许） */
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("^\\d{17}[\\dXx]$");

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
        validateOwnerContact(owner);
        // 手写XML insert不走MyBatis-Plus主键策略，显式生成雪花ID
        owner.setOwnerId(IdWorker.getId());
        owner.setCreateBy(SecurityUtils.getUserName());
        return ownerMapper.insertOwner(owner);
    }

    @Override
    @Transactional
    public int updateOwner(HjyOwner owner) {
        validateOwnerContact(owner);
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

    /** 手机号/身份证号非空时校验格式，不符抛业务异常 */
    private void validateOwnerContact(HjyOwner owner) {
        String phone = owner.getOwnerPhoneNumber();
        if (phone != null && !phone.trim().isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
            throw new CustomException(500, "手机号格式不正确");
        }
        String idCard = owner.getOwnerIdCard();
        if (idCard != null && !idCard.trim().isEmpty() && !ID_CARD_PATTERN.matcher(idCard).matches()) {
            throw new CustomException(500, "身份证号格式不正确");
        }
    }
}
