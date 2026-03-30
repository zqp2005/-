package com.msb.hjycommunity.monitor.service.impl;

import com.msb.hjycommunity.system.domain.LoginUser;
import com.msb.hjycommunity.common.utils.SecurityUtils;
import com.msb.hjycommunity.monitor.domain.SysUserOnline;
import com.msb.hjycommunity.monitor.service.SysUserOnlineService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * 在线用户Service实现
 */
@Service
public class SysUserOnlineServiceImpl implements SysUserOnlineService {

    private static final String LOGIN_TOKEN_KEY = "login_tokens:";

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public List<SysUserOnline> selectOnlineUserList(SysUserOnline sysUserOnline) {
        List<SysUserOnline> onlineList = new ArrayList<>();
        Set<String> keys = redisTemplate.keys(LOGIN_TOKEN_KEY + "*");
        if (keys != null && keys.size() > 0) {
            for (String key : keys) {
                Object obj = redisTemplate.opsForValue().get(key);
                if (obj != null) {
                    try {
                        LoginUser loginUser = (LoginUser) obj;
                        SysUserOnline online = new SysUserOnline();
                        online.setTokenId(loginUser.getToken());
                        online.setUserName(loginUser.getUsername());
                        if (loginUser.getUser() != null) {
                            online.setIpaddr(loginUser.getUser().getLoginIp());
                        }
                        online.setLoginTime(new Date(loginUser.getLoginTime()));

                        boolean match = true;
                        if (StringUtils.isNotEmpty(sysUserOnline.getIpaddr()) 
                                && !sysUserOnline.getIpaddr().equals(online.getIpaddr())) {
                            match = false;
                        }
                        if (match && StringUtils.isNotEmpty(sysUserOnline.getUserName()) 
                                && !sysUserOnline.getUserName().equals(online.getUserName())) {
                            match = false;
                        }

                        if (match) {
                            onlineList.add(online);
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
        }
        return onlineList;
    }

    @Override
    public int forceLogout(String tokenId) {
        String key = LOGIN_TOKEN_KEY + tokenId;
        if (redisTemplate.hasKey(key)) {
            redisTemplate.delete(key);
            return 1;
        }
        return 0;
    }
}
