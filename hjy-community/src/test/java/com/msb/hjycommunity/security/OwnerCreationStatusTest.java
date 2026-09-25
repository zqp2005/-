package com.msb.hjycommunity.security;

import com.msb.hjycommunity.property.domain.HjyOwner;
import com.msb.hjycommunity.property.mapper.HjyOwnerMapper;
import com.msb.hjycommunity.property.service.impl.HjyOwnerServiceImpl;
import com.msb.hjycommunity.system.domain.LoginUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}
