package com.msb.hjycommunity.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msb.hjycommunity.common.utils.SecurityUtils;
import com.msb.hjycommunity.framework.security.service.AppOwnerTokenService;
import com.msb.hjycommunity.property.domain.HjyOwner;
import com.msb.hjycommunity.property.domain.dto.AppLoginRequest;
import com.msb.hjycommunity.property.domain.dto.AppRegisterRequest;
import com.msb.hjycommunity.property.service.HjyOwnerService;
import com.msb.hjycommunity.web.controller.app.AppOwnerController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** 仅使用 Mock，不连接或改写数据库、Redis。 */
class OwnerAccountSecurityTest {
    private HjyOwnerService owners;
    private AppOwnerTokenService tokens;
    private AppOwnerController controller;

    @BeforeEach
    void setUp() {
        owners = mock(HjyOwnerService.class);
        tokens = mock(AppOwnerTokenService.class);
        controller = new AppOwnerController();
        ReflectionTestUtils.setField(controller, "ownerService", owners);
        ReflectionTestUtils.setField(controller, "appOwnerTokenService", tokens);
    }

    @Test
    void publicRegistrationCannotClaimExistingOwner() {
        for (String password : new String[]{null, "", "   "}) {
            HjyOwner existing = new HjyOwner();
            existing.setOwnerId(42L);
            existing.setOwnerPassword(password);
            when(owners.selectOwnerByPhone("13800000000")).thenReturn(existing);
            assertFalse(controller.register(registration()).isSuccess());
        }
        verify(owners, never()).updateOwner(any());
        verify(owners, never()).insertOwner(any());
        verifyNoInteractions(tokens);
    }

    @Test
    void registeredOwnerCannotBeOverwritten() {
        HjyOwner owner = new HjyOwner();
        owner.setOwnerPassword("existing-hash");
        when(owners.selectOwnerByPhone("13800000000")).thenReturn(owner);
        assertFalse(controller.register(registration()).isSuccess());
        verify(owners, never()).updateOwner(any());
        verify(owners, never()).insertOwner(any());
    }

    @Test
    void newRegistrationStillHashesPassword() {
        assertTrue(controller.register(registration()).isSuccess());
        verify(owners).insertOwner(argThat(owner -> "Enable".equals(owner.getOwnerStatus())
                && new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
                .matches("sample-pass", owner.getOwnerPassword())));
        verify(owners, never()).updateOwner(any());
    }

    @Test
    void disabledOrUnknownStatusCannotLogin() {
        for (String status : new String[]{"Disable", null, "unexpected"}) {
            HjyOwner owner = loginOwner(status);
            when(owners.selectOwnerByPhone("13800000000")).thenReturn(owner);
            assertFalse(controller.login(login()).isSuccess());
        }
        verifyNoInteractions(tokens);
    }

    @Test
    void enabledOwnerCanLogin() {
        HjyOwner owner = loginOwner("Enable");
        when(owners.selectOwnerByPhone("13800000000")).thenReturn(owner);
        when(tokens.createToken(owner)).thenReturn("test-token");
        assertTrue(controller.login(login()).isSuccess());
        verify(tokens).createToken(owner);
    }

    @Test
    void passwordIsNeitherExposedNorWritableThroughEntityJson() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        HjyOwner owner = loginOwner("Enable");
        String json = mapper.writeValueAsString(owner);
        assertFalse(json.contains("ownerPassword"));
        assertFalse(json.contains(owner.getOwnerPassword()));
        HjyOwner input = mapper.readValue("{\"ownerPassword\":\"injected\",\"ownerRealName\":\"test\"}", HjyOwner.class);
        assertNull(input.getOwnerPassword());
        assertEquals("test", input.getOwnerRealName());
    }

    private AppRegisterRequest registration() {
        AppRegisterRequest body = new AppRegisterRequest();
        body.setPhone("13800000000");
        body.setPassword("sample-pass");
        body.setRealName("测试居民");
        return body;
    }

    private AppLoginRequest login() {
        AppLoginRequest body = new AppLoginRequest();
        body.setPhone("13800000000");
        body.setPassword("sample-pass");
        return body;
    }

    private HjyOwner loginOwner(String status) {
        HjyOwner owner = new HjyOwner();
        owner.setOwnerId(42L);
        owner.setOwnerStatus(status);
        owner.setOwnerPassword(SecurityUtils.encryptPassword("sample-pass"));
        return owner;
    }
}
