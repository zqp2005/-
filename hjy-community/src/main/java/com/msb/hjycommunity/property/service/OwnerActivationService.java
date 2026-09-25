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
import java.util.Arrays;
import java.util.Collections;

@Service
public class OwnerActivationService {
    private static final String KEY_PREFIX = "owner:activation:";
    private static final String CODE_KEY_PREFIX = "owner:activation:code:";
    private static final DefaultRedisScript<Long> ISSUE = new DefaultRedisScript<>(
            "if redis.call('exists', KEYS[3]) == 1 then return 0 end "
                    + "local previous = redis.call('get', KEYS[1]); "
                    + "if previous then redis.call('del', ARGV[3] .. previous) end "
                    + "redis.call('set', KEYS[1], ARGV[1], 'EX', 900); "
                    + "redis.call('set', KEYS[2], ARGV[2], 'EX', 900); return 1",
            Long.class);
    private static final DefaultRedisScript<Long> RESERVE = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) ~= ARGV[1] then return 0 end "
                    + "if redis.call('set', KEYS[2], ARGV[1], 'NX', 'EX', 60) then return 1 end return 0",
            Long.class);
    private static final DefaultRedisScript<Long> COMPLETE = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then redis.call('del', KEYS[1], KEYS[2]) end "
                    + "if redis.call('get', KEYS[3]) == ARGV[1] then redis.call('del', KEYS[3]) end return 1",
            Long.class);
    private static final DefaultRedisScript<Long> RELEASE = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) end return 0",
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
                || !"Enable".equals(owner.getOwnerStatus()) || hasPassword(owner)) {
            throw new CustomException(500, "仅可为启用状态、有手机号且尚未开通小程序登录的居民发放激活码");
        }
        byte[] bytes = new byte[12];
        random.nextBytes(bytes);
        String code = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String codeDigest = digest(code);
        Long issued = redis.execute(ISSUE,
                Arrays.asList(key(ownerId), codeKey(codeDigest), lockKey(ownerId)),
                codeDigest, String.valueOf(ownerId), CODE_KEY_PREFIX);
        if (!Long.valueOf(1).equals(issued)) {
            throw new CustomException(409, "该居民正在激活，请稍后再生成激活码");
        }
        return code;
    }

    /** 仅凭一次性码查找待激活档案；返回对象只供服务端使用，不直接序列化给客户端。 */
    public HjyOwner findPendingOwner(String code) {
        if (code == null || !code.matches("[A-Za-z0-9_-]{16}")) return null;
        String codeDigest = digest(code);
        String id = redis.opsForValue().get(codeKey(codeDigest));
        if (id == null || !codeDigest.equals(redis.opsForValue().get(key(Long.valueOf(id))))) return null;
        HjyOwner owner = ownerMapper.selectOwnerById(Long.valueOf(id));
        return owner != null && "Enable".equals(owner.getOwnerStatus()) && !hasPassword(owner) ? owner : null;
    }

    public boolean activate(HjyOwner owner, String code, String passwordHash) {
        if (owner == null || !"Enable".equals(owner.getOwnerStatus()) || hasPassword(owner)
                || code == null || code.trim().isEmpty()) return false;
        String codeDigest = digest(code.trim());
        Long reserved = redis.execute(RESERVE,
                Arrays.asList(key(owner.getOwnerId()), lockKey(owner.getOwnerId())), codeDigest);
        if (!Long.valueOf(1).equals(reserved)) return false;
        boolean activated = false;
        try {
            activated = ownerMapper.activateOwner(owner.getOwnerId(), passwordHash) == 1;
            if (activated) {
                redis.execute(COMPLETE,
                        Arrays.asList(key(owner.getOwnerId()), codeKey(codeDigest), lockKey(owner.getOwnerId())),
                        codeDigest);
            }
            return activated;
        } finally {
            if (!activated) redis.execute(RELEASE, Collections.singletonList(lockKey(owner.getOwnerId())), codeDigest);
        }
    }

    private boolean hasPassword(HjyOwner owner) {
        return owner.getOwnerPassword() != null && !owner.getOwnerPassword().trim().isEmpty();
    }

    private String key(Long ownerId) { return KEY_PREFIX + ownerId; }

    private String codeKey(String codeDigest) { return CODE_KEY_PREFIX + codeDigest; }

    private String lockKey(Long ownerId) { return KEY_PREFIX + "lock:" + ownerId; }

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
