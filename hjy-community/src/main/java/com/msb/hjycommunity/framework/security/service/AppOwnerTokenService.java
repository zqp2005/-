package com.msb.hjycommunity.framework.security.service;

import com.alibaba.fastjson.JSON;
import com.msb.hjycommunity.common.constant.Constants;
import com.msb.hjycommunity.common.utils.RedisCache;
import com.msb.hjycommunity.common.utils.UUIDUtils;
import com.msb.hjycommunity.framework.security.domain.AppOwnerToken;
import com.msb.hjycommunity.property.domain.HjyOwner;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * 业主端（小程序 /app 接口）令牌服务
 * <p>
 * 与管理端 TokenServiceImpl 同一套 JWT 基础设施（HS256 + token.secret），
 * 但登录态独立存放在 {@code owner_tokens:{uuid}}，有效期 7 天，剩余不足 1 天时自动续期。
 *
 * @author hjy
 * @date 2026/9/11
 **/
@Component
public class AppOwnerTokenService {

    /** 登录态有效期（天）：小程序场景比管理端 30 分钟长，避免频繁重登 */
    private static final int TOKEN_EXPIRE_DAYS = 7;

    /** 登录态有效期（毫秒） */
    private static final long TOKEN_EXPIRE_MILLIS = TOKEN_EXPIRE_DAYS * 24 * 60 * 60 * 1000L;

    /** 剩余有效期不足该阈值（毫秒）时自动续期 */
    private static final long RENEW_THRESHOLD_MILLIS = 24 * 60 * 60 * 1000L;

    @Autowired
    private RedisCache redisCache;

    /** 请求头名称（与管理端一致，默认 Authorization） */
    @Value("${token.header}")
    private String header;

    /** 令牌秘钥（真实值在 application-local.yml，与管理端共用） */
    @Value("${token.secret}")
    private String secret;

    /**
     * 为业主创建登录态：Redis 存 AppOwnerToken，返回只携带 uuid 的 JWT
     *
     * @param owner 已通过密码校验的业主
     * @return: java.lang.String JWT
     */
    public String createToken(HjyOwner owner) {
        String uuid = UUIDUtils.randomUUID();

        AppOwnerToken ownerToken = new AppOwnerToken();
        ownerToken.setToken(uuid);
        ownerToken.setOwnerId(owner.getOwnerId());
        ownerToken.setPhone(owner.getOwnerPhoneNumber());
        ownerToken.setRealName(owner.getOwnerRealName());
        ownerToken.setLoginTime(System.currentTimeMillis());
        ownerToken.setExpireTime(ownerToken.getLoginTime() + TOKEN_EXPIRE_MILLIS);

        redisCache.setCacheObject(Constants.OWNER_TOKEN_KEY + uuid, ownerToken, TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);

        HashMap<String, Object> claims = new HashMap<>();
        claims.put(Constants.OWNER_USER_KEY, uuid);

        return Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS256, secret).compact();
    }

    /**
     * 解析请求中的业主令牌，校验 Redis 登录态是否存活
     * <p>
     * 读取 Redis 走「先取 JSON 字符串再反序列化」的写法（同 TokenServiceImpl），
     * 规避 DevTools 热部署的类加载器问题。剩余不足 1 天时自动续期为 7 天。
     *
     * @return: com.msb.hjycommunity.framework.security.domain.AppOwnerToken
     * 令牌缺失/验签失败/登录态过期一律返回 null，由调用方决定 401
     */
    public AppOwnerToken getOwnerToken(HttpServletRequest request) {
        String token = getToken(request);
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
            String uuid = (String) claims.get(Constants.OWNER_USER_KEY);
            if (StringUtils.isEmpty(uuid)) {
                // 管理端 JWT 或伪造令牌：没有业主声明，视为未认证
                return null;
            }
            String userKey = Constants.OWNER_TOKEN_KEY + uuid;
            String jsonString = redisCache.getStringValue(userKey);
            if (jsonString == null || jsonString.isEmpty()) {
                return null;
            }
            AppOwnerToken ownerToken = JSON.parseObject(jsonString, AppOwnerToken.class);
            if (ownerToken == null) {
                return null;
            }
            // 剩余不足1天时滑动续期
            if (ownerToken.getExpireTime() == null
                    || ownerToken.getExpireTime() - System.currentTimeMillis() <= RENEW_THRESHOLD_MILLIS) {
                ownerToken.setLoginTime(System.currentTimeMillis());
                ownerToken.setExpireTime(ownerToken.getLoginTime() + TOKEN_EXPIRE_MILLIS);
                redisCache.setCacheObject(userKey, ownerToken, TOKEN_EXPIRE_DAYS, TimeUnit.DAYS);
            }
            return ownerToken;
        } catch (Exception e) {
            // 验签失败、token 格式错误等一律视为未认证
            return null;
        }
    }

    /**
     * 从请求头中获取 token（支持 Authorization: Bearer xxx 标准写法）
     */
    private String getToken(HttpServletRequest request) {
        String token = request.getHeader(this.header);
        if (!StringUtils.isEmpty(token) && token.startsWith(Constants.TOKEN_PREFIX)) {
            token = token.replace(Constants.TOKEN_PREFIX, "");
        }
        return token;
    }
}
