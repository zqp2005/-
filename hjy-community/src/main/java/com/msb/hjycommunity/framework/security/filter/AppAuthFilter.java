package com.msb.hjycommunity.framework.security.filter;

import com.alibaba.fastjson.JSON;
import com.msb.hjycommunity.common.constant.Constants;
import com.msb.hjycommunity.common.constant.HttpStatus;
import com.msb.hjycommunity.common.core.domain.BaseResponse;
import com.msb.hjycommunity.framework.security.domain.AppOwnerToken;
import com.msb.hjycommunity.framework.security.service.AppOwnerTokenService;
import com.msb.hjycommunity.system.domain.LoginUser;
import com.msb.hjycommunity.system.domain.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 业主端（小程序 /app 接口）认证过滤器
 * <p>
 * /app/** 在 SecurityConfig 中放行（permitAll），认证由本过滤器自管：
 * <ul>
 *   <li>非 /app/ 前缀与 OPTIONS 预检请求直接放行，不影响管理端；</li>
 *   <li>白名单（/app/register、/app/login）放行，并置一个无权限的占位身份，
 *       使复用的业务 Service（insertOwner/insertRepair 等内部经 SecurityUtils.getUserName()
 *       记录 create_by）在无管理端登录态时不抛异常，create_by 记为 "app"；</li>
 *   <li>其余 /app 请求要求 Authorization: Bearer JWT（业主令牌，owner_tokens:{uuid} 存活）；</li>
 *   <li>通过后把业主身份写入 request attribute（appOwnerId/appOwnerRealName/appOwnerPhone），
 *       同时置入 SecurityContext（无任何权限，仅供复用 Service 取操作人）。</li>
 * </ul>
 * 认证失败返回 HTTP 401 + BaseResponse JSON（与管理端 unauthorizedHandler 风格一致）。
 *
 * @author hjy
 * @date 2026/9/11
 **/
@Component
public class AppAuthFilter extends OncePerRequestFilter {

    /** /app 前缀 */
    private static final String APP_PREFIX = "/app/";

    /** 白名单：注册/登录不需要令牌 */
    private static final String[] WHITE_LIST = {"/app/register", "/app/login"};

    /** 占位操作人（写入 create_by/update_by） */
    private static final String APP_OPERATOR = "app";

    /** 通过鉴权后写入 request attribute 的业主ID键名 */
    public static final String ATTR_OWNER_ID = "appOwnerId";

    /** 通过鉴权后写入 request attribute 的业主姓名键名 */
    public static final String ATTR_OWNER_NAME = "appOwnerRealName";

    /** 通过鉴权后写入 request attribute 的业主手机号键名 */
    public static final String ATTR_OWNER_PHONE = "appOwnerPhone";

    @Autowired
    private AppOwnerTokenService appOwnerTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();

        // 只管 /app/ 前缀；CORS 预检直接放行（不影响管理端任何请求）
        if (!uri.startsWith(APP_PREFIX) || HttpMethod.OPTIONS.matches(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // 注册/登录白名单：置占位身份后放行
        if (isWhiteList(uri)) {
            setSecurityContext(APP_OPERATOR, null);
            filterChain.doFilter(request, response);
            return;
        }

        // 校验业主令牌
        AppOwnerToken ownerToken = appOwnerTokenService.getOwnerToken(request);
        if (ownerToken == null) {
            writeUnauthorized(response);
            return;
        }

        // 业主身份注入：request attribute 供 Controller 使用，SecurityContext 供复用的业务 Service 记录操作人
        request.setAttribute(ATTR_OWNER_ID, ownerToken.getOwnerId());
        request.setAttribute(ATTR_OWNER_NAME, ownerToken.getRealName());
        request.setAttribute(ATTR_OWNER_PHONE, ownerToken.getPhone());
        setSecurityContext(ownerToken.getRealName(), ownerToken.getOwnerId());

        filterChain.doFilter(request, response);
    }

    private boolean isWhiteList(String uri) {
        for (String path : WHITE_LIST) {
            if (path.equals(uri)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 置一个无任何权限的占位身份（覆盖安全链留下的匿名身份，避免复用 Service 取操作人失败）
     * <p>
     * 授权判定在此之前已完成（/app/** permitAll），此处身份仅供业务层记录 create_by/update_by；
     * 占位身份没有任何权限，即使被 @PreAuthorize 校验也只会被拒绝，无法越权访问管理端接口。
     */
    private void setSecurityContext(String userName, Long userId) {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(userId);
        sysUser.setUserName(userName);
        LoginUser loginUser = new LoginUser(sysUser);
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    /**
     * 以 401 状态返回统一 JSON 错误（msg 与管理端 AuthenticationEntryPointImpl 一致）
     */
    private void writeUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().print(JSON.toJSONString(
                BaseResponse.fail(String.valueOf(HttpStatus.UNAUTHORIZED), "认证失败,无法访问系统资源")));
    }
}
