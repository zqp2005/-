package com.msb.hjycommunity.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msb.hjycommunity.common.utils.SecurityUtils;
import com.msb.hjycommunity.framework.security.service.AppOwnerTokenService;
import com.msb.hjycommunity.property.domain.HjyOwner;
import com.msb.hjycommunity.property.domain.dto.AppActivationRequest;
import com.msb.hjycommunity.property.domain.dto.AppLoginRequest;
import com.msb.hjycommunity.property.domain.dto.AppRegisterRequest;
import com.msb.hjycommunity.property.service.HjyOwnerService;
import com.msb.hjycommunity.property.service.OwnerActivationService;
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
    private OwnerActivationService activation;
    private AppOwnerController controller;

    @BeforeEach
    void setUp() {
        owners = mock(HjyOwnerService.class);
        tokens = mock(AppOwnerTokenService.class);
        activation = mock(OwnerActivationService.class);
        controller = new AppOwnerController();
        ReflectionTestUtils.setField(controller, "ownerService", owners);
        ReflectionTestUtils.setField(controller, "appOwnerTokenService", tokens);
        ReflectionTestUtils.setField(controller, "ownerActivationService", activation);
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
    void activationRequiresMatchingExistingProfileAndCode() {
        HjyOwner owner = new HjyOwner();
        owner.setOwnerId(42L);
        owner.setOwnerRealName("测试居民");
        when(owners.selectOwnerByPhone("13800000000")).thenReturn(owner);
        AppRegisterRequest request = registration();
        request.setActivationCode("one-time-code");
        when(activation.activate(eq(owner), eq("one-time-code"), anyString())).thenReturn(true);
        assertTrue(controller.register(request).isSuccess());
        verify(activation).activate(eq(owner), eq("one-time-code"), argThat(hash ->
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().matches("sample-pass", hash)));
        verify(owners, never()).insertOwner(any());

        when(activation.activate(eq(owner), eq("one-time-code"), anyString())).thenReturn(false);
        assertFalse(controller.register(request).isSuccess());

        request.setRealName("其他人");
        assertFalse(controller.register(request).isSuccess());
        verify(activation, times(2)).activate(eq(owner), eq("one-time-code"), anyString());
    }

    @Test
    void activationCodeCannotCreateAnotherProfile() {
        AppRegisterRequest request = registration();
        request.setActivationCode("one-time-code");
        assertFalse(controller.register(request).isSuccess());
        verify(owners, never()).insertOwner(any());
        verify(activation, never()).activate(any(), anyString(), anyString());
    }

    @Test
    void codeOnlyActivationUsesServerProfileAndIgnoresClientFields() {
        HjyOwner owner = new HjyOwner();
        owner.setOwnerId(42L);
        owner.setOwnerRealName("测试居民");
        owner.setOwnerPhoneNumber("13800000000");
        AppRegisterRequest request = new AppRegisterRequest();
        request.setActivationCode("one-time-code");
        request.setPassword("sample-pass");
        when(activation.findPendingOwner("one-time-code")).thenReturn(owner);
        when(activation.activate(eq(owner), eq("one-time-code"), anyString())).thenReturn(true);
        assertTrue(controller.register(request).isSuccess());
        verify(owners, never()).insertOwner(any());
        verify(owners, never()).updateOwner(any());
    }

    @Test
    void activationPreviewOnlyShowsNameAndMaskedPhone() {
        HjyOwner owner = new HjyOwner();
        owner.setOwnerRealName("测试居民");
        owner.setOwnerPhoneNumber("13800000000");
        owner.setOwnerIdCard("123456789012345678");
        AppActivationRequest request = new AppActivationRequest();
        request.setActivationCode("one-time-code");
        when(activation.findPendingOwner("one-time-code")).thenReturn(owner);
        String response = new ObjectMapper().valueToTree(controller.previewActivation(request).getData()).toString();
        assertTrue(response.contains("测试居民"));
        assertTrue(response.contains("138****0000"));
        assertFalse(response.contains("13800000000"));
        assertFalse(response.contains("123456789012345678"));
        when(activation.findPendingOwner("one-time-code")).thenReturn(null);
        assertFalse(controller.previewActivation(request).isSuccess());
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
        assertTrue(json.contains("\"appLoginEnabled\":true"));
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
