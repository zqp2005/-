package com.msb.hjycommunity.security;

import com.alibaba.fastjson.JSON;
import com.msb.hjycommunity.common.utils.RedisCache;
import com.msb.hjycommunity.framework.security.domain.AppOwnerToken;
import com.msb.hjycommunity.framework.security.service.AppOwnerTokenService;
import com.msb.hjycommunity.framework.service.SysPermissionService;
import com.msb.hjycommunity.property.domain.HjyOwner;
import com.msb.hjycommunity.property.mapper.HjyOwnerMapper;
import com.msb.hjycommunity.system.domain.*;
import com.msb.hjycommunity.system.mapper.SysUserMapper;
import com.msb.hjycommunity.system.service.impl.TokenServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class SessionRevocationTest {
    @Test void ownerSessionTracksStatusPasswordAndDeletion() {
        RedisCache redis = mock(RedisCache.class);
        HjyOwnerMapper mapper = mock(HjyOwnerMapper.class);
        AppOwnerTokenService service = new AppOwnerTokenService();
        wire(service, redis);
        ReflectionTestUtils.setField(service, "ownerMapper", mapper);
        HjyOwner owner = new HjyOwner();
        owner.setOwnerId(42L); owner.setOwnerStatus("Enable"); owner.setOwnerPassword("hash-v1");
        String token = service.createToken(owner);
        ArgumentCaptor<AppOwnerToken> saved = ArgumentCaptor.forClass(AppOwnerToken.class);
        verify(redis).setCacheObject(anyString(), saved.capture(), eq(7), eq(TimeUnit.DAYS));
        when(redis.getStringValue(anyString())).thenReturn(JSON.toJSONString(saved.getValue()));
        when(mapper.selectOwnerById(42L)).thenReturn(owner);
        MockHttpServletRequest request = request(token);
        assertNotNull(service.getOwnerToken(request));
        owner.setOwnerStatus("Disable");
        assertNull(service.getOwnerToken(request));
        owner.setOwnerStatus("Enable"); owner.setOwnerPassword("hash-v2");
        assertNull(service.getOwnerToken(request));
        when(mapper.selectOwnerById(42L)).thenReturn(null);
        assertNull(service.getOwnerToken(request));
        verify(redis, times(3)).deleteObject(anyString());
    }

    @Test void oldOrExpiredOwnerSessionCannotBeRenewed() {
        RedisCache redis = mock(RedisCache.class);
        HjyOwnerMapper mapper = mock(HjyOwnerMapper.class);
        AppOwnerTokenService service = new AppOwnerTokenService(); wire(service, redis);
        ReflectionTestUtils.setField(service, "ownerMapper", mapper);
        HjyOwner owner = new HjyOwner();
        owner.setOwnerId(42L); owner.setOwnerStatus("Enable"); owner.setOwnerPassword("hash");
        String token = service.createToken(owner);
        ArgumentCaptor<AppOwnerToken> saved = ArgumentCaptor.forClass(AppOwnerToken.class);
        verify(redis).setCacheObject(anyString(), saved.capture(), eq(7), eq(TimeUnit.DAYS));
        when(mapper.selectOwnerById(42L)).thenReturn(owner);
        saved.getValue().setExpireTime(1L);
        when(redis.getStringValue(anyString())).thenReturn(JSON.toJSONString(saved.getValue()));
        assertNull(service.getOwnerToken(request(token)));
        saved.getValue().setExpireTime(System.currentTimeMillis() + 600000);
        saved.getValue().setCredentialFingerprint(null);
        when(redis.getStringValue(anyString())).thenReturn(JSON.toJSONString(saved.getValue()));
        assertNull(service.getOwnerToken(request(token)));
        verify(redis, times(1)).setCacheObject(anyString(), any(), anyInt(), any());
    }

    @Test void adminSessionReloadsPermissionsAndRejectsChangedCredentials() {
        RedisCache redis = mock(RedisCache.class);
        SysUserMapper mapper = mock(SysUserMapper.class);
        SysPermissionService permissions = mock(SysPermissionService.class);
        TokenServiceImpl service = new TokenServiceImpl(); wire(service, redis);
        ReflectionTestUtils.setField(service, "expireTime", 30);
        ReflectionTestUtils.setField(service, "userMapper", mapper);
        ReflectionTestUtils.setField(service, "permissionService", permissions);
        SysUser user = new SysUser(); user.setUserId(2L); user.setUserName("tester");
        user.setStatus("0"); user.setDelFlag("0"); user.setPassword("hash-v1");
        LoginUser login = new LoginUser(user, Collections.singleton("old:permission"));
        String token = service.createToken(login);
        when(redis.getStringValue(anyString())).thenReturn(JSON.toJSONString(login));
        when(mapper.selectUserById(2L)).thenReturn(user);
        when(permissions.getMenuPermission(user)).thenReturn(Collections.singleton("new:permission"));
        assertEquals(Collections.singleton("new:permission"), service.getLoginUser(request(token)).getPermissions());
        user.setPassword("hash-v2"); assertNull(service.getLoginUser(request(token)));
        user.setPassword("hash-v1"); user.setStatus("1"); assertNull(service.getLoginUser(request(token)));
        user.setStatus("0"); user.setDelFlag("2"); assertNull(service.getLoginUser(request(token)));
        assertNull(service.getLoginUser(request("not.a.jwt")));
    }

    private void wire(Object service, RedisCache redis) {
        ReflectionTestUtils.setField(service, "redisCache", redis);
        ReflectionTestUtils.setField(service, "secret", "test-secret");
        ReflectionTestUtils.setField(service, "header", "Authorization");
    }
    private MockHttpServletRequest request(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token); return request;
    }
}
