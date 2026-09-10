package com.msb.hjycommunity.system.service.impl;

import com.msb.hjycommunity.common.constant.Constants;
import com.msb.hjycommunity.common.core.exception.CaptchaNotMatchException;
import com.msb.hjycommunity.common.core.exception.CustomException;
import com.msb.hjycommunity.common.utils.IpUtils;
import com.msb.hjycommunity.common.utils.RedisCache;
import com.msb.hjycommunity.common.utils.ServletUtils;
import com.msb.hjycommunity.monitor.domain.SysLogininfor;
import com.msb.hjycommunity.monitor.mapper.SysLogininforMapper;
import com.msb.hjycommunity.system.domain.LoginUser;
import com.msb.hjycommunity.system.service.SysLoginService;
import com.msb.hjycommunity.system.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * @author spikeCong
 * @date 2023/5/5
 **/
@Slf4j
@Component
public class SysLoginServiceImpl implements SysLoginService {

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private SysLogininforMapper logininforMapper;

    /**
     * 带验证码登录
     * @param username
     * @param password
     * @param code
     * @param uuid
     * @return: java.lang.String
     */
    @Override
    public String login(String username, String password, String code, String uuid) {

       // 1.从redis中获取验证码,判断是否正确
        String key = Constants.CAPTCHA_CODE_KEY + uuid;
        String captcha = redisCache.getCacheObject(key);
        redisCache.deleteObject(key);

        if(captcha == null || !captcha.equalsIgnoreCase(code)){
            recordLogininfor(username, "1", captcha == null ? "验证码已过期" : "验证码错误");
            throw new CaptchaNotMatchException();
        }

        //2.进行用户认证
        Authentication authentication = null;
        try{
            authentication = authenticationManager.
                    authenticate(new UsernamePasswordAuthenticationToken(username,password));
        }catch (Exception e){
            recordLogininfor(username, "1", "用户不存在/密码错误");
            throw new CustomException(400,"用户不存在/密码错误");
        }

        //3.获取用户经过身份验证的用户的主体信息
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();

        String token = tokenService.createToken(loginUser);
        recordLogininfor(username, "0", "登录成功");
        return token;
    }

    /**
     * AI服务登录（无需验证码，机器调用不记录登录日志）
     */
    @Override
    public String aiLogin(String username, String password) {
        Authentication authentication = null;
        try{
            authentication = authenticationManager.
                    authenticate(new UsernamePasswordAuthenticationToken(username,password));
        }catch (Exception e){
            return null;
        }
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        return tokenService.createToken(loginUser);
    }

    /**
     * 记录登录日志（status: 0成功 1失败），记录失败不影响登录主流程
     */
    private void recordLogininfor(String username, String status, String msg) {
        try {
            SysLogininfor logininfor = new SysLogininfor();
            logininfor.setUserName(username);
            logininfor.setStatus(status);
            logininfor.setMsg(msg);
            logininfor.setLoginTime(new Date());

            ServletRequestAttributes attributes = ServletUtils.getRequestAttributes();
            HttpServletRequest request = attributes == null ? null : attributes.getRequest();
            logininfor.setIpaddr(IpUtils.getIpAddr(request));
            logininfor.setLoginLocation(IpUtils.getLoginLocation(logininfor.getIpaddr()));
            logininfor.setBrowser(IpUtils.getBrowser(request));
            logininfor.setOs(IpUtils.getOs(request));

            logininforMapper.insertLogininfor(logininfor);
        } catch (Exception e) {
            log.error("记录登录日志失败, userName: {}", username, e);
        }
    }
}
