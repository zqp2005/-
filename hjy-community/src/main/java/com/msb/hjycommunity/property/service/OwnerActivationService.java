package com.msb.hjycommunity.property.service;

import com.msb.hjycommunity.property.domain.HjyOwner;
import com.msb.hjycommunity.property.mapper.HjyOwnerMapper;
import com.msb.hjycommunity.common.core.exception.CustomException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Service
public class OwnerActivationService {
    private static final int EXPIRE_MINUTES = 15;
    private static final String KEY_PREFIX = "owner:activation:";
    private static final DefaultRedisScript<Long> CONSUME = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);

    private final HjyOwnerMapper ownerMapper;
    private final StringRedisTemplate redis;
    private final SecureRandom random = new SecureRandom();

    public OwnerActivationService(HjyOwnerMapper ownerMapper, StringRedisTemplate redis) {
        this.ownerMapper = ownerMapper;
        this.redis = redis;
    }

    public String issue(Long ownerId) {
        HjyOwner owner = ownerMapper.selectOwnerById(ownerId);
        if (owner == null || owner.getOwnerPhoneNumber() == null || owner.getOwnerPhoneNumber().trim().isEmpty()
                || "Disable".equals(owner.getOwnerStatus()) || hasPassword(owner)) {
            throw new CustomException(500, "仅可为启用状态、有手机号且尚未开通小程序登录的居民发放激活码");
        }
        byte[] bytes = new byte[12];
        random.nextBytes(bytes);
        String code = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        redis.opsForValue().set(key(ownerId), digest(code), EXPIRE_MINUTES, TimeUnit.MINUTES);
        return code;
    }

    public boolean activate(HjyOwner owner, String code, String passwordHash) {
        if (owner == null || "Disable".equals(owner.getOwnerStatus()) || hasPassword(owner)
                || code == null || code.trim().isEmpty()) return false;
        Long consumed = redis.execute(CONSUME, Collections.singletonList(key(owner.getOwnerId())), digest(code.trim()));
        if (!Long.valueOf(1).equals(consumed)) return false;
        return ownerMapper.activateOwner(owner.getOwnerId(), passwordHash) == 1;
    }

    private boolean hasPassword(HjyOwner owner) {
        return owner.getOwnerPassword() != null && !owner.getOwnerPassword().trim().isEmpty();
    }

    private String key(Long ownerId) { return KEY_PREFIX + ownerId; }

    private String digest(String value) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) hex.append(String.format("%02x", b & 0xff));
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("无法生成激活码摘要", e);
        }
    }
}
