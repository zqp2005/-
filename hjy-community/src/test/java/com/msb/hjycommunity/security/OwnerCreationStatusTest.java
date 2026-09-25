package com.msb.hjycommunity.security;

import com.msb.hjycommunity.common.core.exception.CustomException;
import com.msb.hjycommunity.property.domain.HjyOwner;
import com.msb.hjycommunity.property.mapper.HjyOwnerMapper;
import com.msb.hjycommunity.property.service.impl.HjyOwnerServiceImpl;
import com.msb.hjycommunity.system.domain.LoginUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class OwnerCreationStatusTest {
    private final HjyOwnerMapper mapper = mock(HjyOwnerMapper.class);
    private final HjyOwnerServiceImpl service = new HjyOwnerServiceImpl();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "ownerMapper", mapper);
        LoginUser user = mock(LoginUser.class);
        when(user.getUsername()).thenReturn("test-admin");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void newOwnerDefaultsToEnabled() {
        HjyOwner owner = new HjyOwner();
        service.insertOwner(owner);
        assertEquals("Enable", owner.getOwnerStatus());
        verify(mapper).insertOwner(owner);
    }

    @Test
    void explicitlyDisabledOwnerStaysDisabled() {
        HjyOwner owner = new HjyOwner();
        owner.setOwnerStatus("Disable");
        service.insertOwner(owner);
        assertEquals("Disable", owner.getOwnerStatus());
        verify(mapper).insertOwner(owner);
    }

    @Test
    void duplicatePhoneIsRejectedOnCreateAndEdit() {
        HjyOwner existing = new HjyOwner();
        existing.setOwnerId(1L);
        when(mapper.selectOwnersByPhone("13800000000")).thenReturn(Arrays.asList(existing));
        HjyOwner owner = new HjyOwner();
        owner.setOwnerId(2L);
        owner.setOwnerPhoneNumber("13800000000");
        assertThrows(CustomException.class, () -> service.insertOwner(owner));
        assertThrows(CustomException.class, () -> service.updateOwner(owner));
        verify(mapper, never()).insertOwner(any());
        verify(mapper, never()).updateOwner(any());
    }

    @Test
    void existingDuplicatePhoneReturnsActionableError() {
        HjyOwner first = new HjyOwner();
        HjyOwner second = new HjyOwner();
        when(mapper.selectOwnersByPhone("13800000000")).thenReturn(Arrays.asList(first, second));
        assertThrows(CustomException.class, () -> service.selectOwnerByPhone("13800000000"));
    }

    @Test
    void unchangedPhoneMayBeSavedAndConcurrentDuplicateIsReported() {
        HjyOwner owner = new HjyOwner();
        owner.setOwnerId(2L);
        owner.setOwnerPhoneNumber("13800000000");
        when(mapper.selectOwnersByPhone("13800000000")).thenReturn(Arrays.asList(owner));
        when(mapper.updateOwner(owner)).thenThrow(new DuplicateKeyException("Duplicate entry for key 'uk_hjy_owner_phone'"));
        assertThrows(CustomException.class, () -> service.updateOwner(owner));
        verify(mapper).updateOwner(owner);
    }
}
