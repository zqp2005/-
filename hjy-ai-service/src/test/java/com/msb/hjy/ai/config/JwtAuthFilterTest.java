package com.msb.hjy.ai.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * JwtAuthFilter JWT 解析链路单元测试（纯逻辑，不依赖 Spring 上下文与 Redis）
 * <p>
 * 用与主后端 TokenServiceImpl.createToken 完全相同的 jjwt 0.9.1 API 生成 HS256 token
 * （claims 含 login_user_key/userId/userName），再走过滤器解析逻辑取回身份，
 * 验证 jjwt 0.9.1 + jaxb 在 Java 17 下签名/验签兼容可用。
 */
class JwtAuthFilterTest {

    private static final String SECRET = "test-secret";

    /** 模拟主后端 TokenServiceImpl.createToken 的签发方式 */
    private String buildToken(Long userId, String userName) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("login_user_key", "uuid-1234");
        if (userId != null) {
            claims.put("userId", userId);
        }
        if (userName != null) {
            claims.put("userName", userName);
        }
        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }

    @Test
    void parseIdentityExtractsAllClaimsFromNewStyleToken() {
        String token = buildToken(1L, "admin");

        JwtAuthFilter.JwtIdentity identity = JwtAuthFilter.parseIdentity(token, SECRET);

        assertEquals("uuid-1234", identity.userKey);
        assertEquals(1L, identity.userId);
        assertEquals("admin", identity.userName);
    }

    @Test
    void parseIdentityReturnsNullUserIdForLegacyToken() {
        // 旧版 token 只有 login_user_key，无 userId/userName 声明
        String token = buildToken(null, null);

        JwtAuthFilter.JwtIdentity identity = JwtAuthFilter.parseIdentity(token, SECRET);

        assertEquals("uuid-1234", identity.userKey);
        assertNull(identity.userId);
        assertNull(identity.userName);
    }

    @Test
    void parseIdentityRejectsTokenSignedWithDifferentSecret() {
        String token = buildToken(1L, "admin");

        assertThrows(Exception.class, () -> JwtAuthFilter.parseIdentity(token, "wrong-secret"));
    }

    @Test
    void parseIdentityRejectsMalformedToken() {
        assertThrows(Exception.class, () -> JwtAuthFilter.parseIdentity("not.a.jwt", SECRET));
    }

    @Test
    void parseIdentityHandlesLargeUserId() {
        // userId 超出 Integer 范围时应仍能按 Long 取回
        String token = buildToken(9_999_999_999L, "业主");

        JwtAuthFilter.JwtIdentity identity = JwtAuthFilter.parseIdentity(token, SECRET);

        assertNotNull(identity.userId);
        assertEquals(9_999_999_999L, identity.userId);
    }
}
