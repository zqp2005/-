package com.msb.hjy.ai.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msb.hjy.ai.common.result.Result;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * AI 服务 JWT 鉴权过滤器
 * <p>
 * 前端在 /ai 请求头中携带主后端（hjy-community）登录后签发的 JWT：
 * <ul>
 *   <li>用与主后端一致的密钥验签（hjy.ai.auth.token-secret）；</li>
 *   <li>从 claims 中取出 login_user_key（uuid）与 userId/userName；</li>
 *   <li>到共享 Redis 校验 {@code login_tokens:{uuid}} 登录态是否存活（登出/过期即失效）；</li>
 *   <li>通过后把身份放入 request attribute（aiUserId/aiUserName），供 Controller 建立按用户隔离的会话。</li>
 * </ul>
 * 任一步失败返回 401 JSON。旧版 token（无 userId 声明）返回 401 提示重新登录。
 */
@Slf4j
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    /** 主后端登录态在 Redis 中的 key 前缀（与 hjy-community Constants.LOGIN_TOKEN_KEY 一致） */
    public static final String LOGIN_TOKEN_KEY_PREFIX = "login_tokens:";

    /** 主后端 JWT 中存放 uuid 的 claim 名（与 hjy-community Constants.LOGIN_USER_KEY 一致） */
    public static final String CLAIM_USER_KEY = "login_user_key";

    /** 通过鉴权后写入 request attribute 的用户 ID 键名 */
    public static final String ATTR_USER_ID = "aiUserId";

    /** 通过鉴权后写入 request attribute 的用户名键名 */
    public static final String ATTR_USER_NAME = "aiUserName";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    /** 与主后端 token.secret 一致的 JWT 签名密钥（真实值在 application-local.yml 或 TOKEN_SECRET 环境变量） */
    @Value("${hjy.ai.auth.token-secret:}")
    private String tokenSecret;

    public JwtAuthFilter(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 启动自检：密钥未配置时在日志中给出明确警告（此时所有 /ai 请求会被拒绝，
     * 提示复制 src/main/resources/application-local.yml.template 并填入与主后端一致的密钥）
     */
    @jakarta.annotation.PostConstruct
    public void warnIfSecretMissing() {
        if (!StringUtils.hasText(tokenSecret)) {
            log.warn("======== [AI服务鉴权] hjy.ai.auth.token-secret 未配置！所有 /ai 对话请求将被 401 拒绝。"
                    + " 请复制 application-local.yml.template 为 application-local.yml，"
                    + "填入与 hjy-community 相同的 JWT 密钥（TOKEN_SECRET）后重启 ========");
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();

        // 只鉴权 /ai/ 前缀的请求；健康检查与 CORS 预检直接放行
        if (!uri.startsWith("/ai/")
                || HttpMethod.OPTIONS.matches(request.getMethod())
                || (HttpMethod.GET.matches(request.getMethod()) && uri.equals("/ai/health"))) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            writeUnauthorized(response, "未登录或登录已过期");
            return;
        }

        // 密钥未配置时直接拒绝，避免用空密钥验签被伪造 token 绕过
        if (!StringUtils.hasText(tokenSecret)) {
            log.error("hjy.ai.auth.token-secret 未配置，拒绝 AI 请求 - uri: {}", uri);
            writeUnauthorized(response, "AI服务鉴权密钥未配置，请联系管理员");
            return;
        }

        try {
            JwtIdentity identity = parseIdentity(authHeader.substring("Bearer ".length()), tokenSecret);
            if (identity.userId == null) {
                // 旧版 token 没有 userId 声明，无法按用户隔离会话
                writeUnauthorized(response, "请重新登录后再使用AI助手");
                return;
            }
            // 校验主后端登录态是否存活（用户登出或过期后 Redis key 被删除/过期）
            Boolean alive = stringRedisTemplate.hasKey(LOGIN_TOKEN_KEY_PREFIX + identity.userKey);
            if (!Boolean.TRUE.equals(alive)) {
                writeUnauthorized(response, "未登录或登录已过期");
                return;
            }
            request.setAttribute(ATTR_USER_ID, identity.userId);
            request.setAttribute(ATTR_USER_NAME, identity.userName);
        } catch (Exception e) {
            // 验签失败、token 格式错误、claim 缺失等一律视为未认证
            log.warn("AI服务鉴权失败 - uri: {}, error: {}", uri, e.getMessage());
            writeUnauthorized(response, "未登录或登录已过期");
            return;
        }

        chain.doFilter(request, response);
    }

    /**
     * 以 401 状态返回统一 JSON 错误（与项目 Result 结构一致）
     */
    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(401, message)));
    }

    /**
     * 解析 JWT 中的用户身份（uuid、userId、userName）
     * <p>
     * 独立为静态方法便于单元测试：验证 jjwt 0.9.1 + jaxb 在 Java 17 下
     * 能正确解析主后端 TokenServiceImpl 以相同 API 签发的 token。
     * 注意：JSON 数字反序列化可能是 Integer/Long，统一按 Number 转 Long。
     */
    public static JwtIdentity parseIdentity(String token, String secret) {
        Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
        String userKey = claims.get(CLAIM_USER_KEY, String.class);
        Long userId = toLong(claims.get("userId"));
        String userName = claims.get("userName", String.class);
        return new JwtIdentity(userKey, userId, userName);
    }

    private static Long toLong(Object value) {
        return value instanceof Number ? ((Number) value).longValue() : null;
    }

    /** 从 JWT 解析出的用户身份 */
    public static class JwtIdentity {

        public final String userKey;
        public final Long userId;
        public final String userName;

        public JwtIdentity(String userKey, Long userId, String userName) {
            this.userKey = userKey;
            this.userId = userId;
            this.userName = userName;
        }
    }
}
