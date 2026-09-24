package com.msb.hjycommunity.common.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/** 只在服务端缓存凭据指纹，不在 JWT 或业务响应中暴露密码哈希。 */
public final class SessionFingerprint {
    private SessionFingerprint() { }

    public static String of(String secret, Object id, String password, Object changedAt) {
        if (secret == null || secret.isEmpty() || id == null || password == null) {
            throw new IllegalArgumentException("缺少会话校验凭据");
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String source = id + "\n" + password + "\n" + changedAt;
            return Base64.getEncoder().encodeToString(mac.doFinal(source.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.GeneralSecurityException e) {
            throw new IllegalStateException("无法校验会话", e);
        }
    }
}
