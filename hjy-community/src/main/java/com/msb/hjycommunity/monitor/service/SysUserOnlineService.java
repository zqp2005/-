package com.msb.hjycommunity.monitor.service;

import com.msb.hjycommunity.monitor.domain.SysUserOnline;

import java.util.List;

/**
 * 在线用户Service接口
 */
public interface SysUserOnlineService {

    /**
     * 查询在线用户列表
     */
    List<SysUserOnline> selectOnlineUserList(SysUserOnline sysUserOnline);

    /**
     * 强退用户
     */
    int forceLogout(String tokenId);
}
