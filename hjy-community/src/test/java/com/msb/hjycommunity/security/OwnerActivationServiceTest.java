package com.msb.hjycommunity.security;

import com.msb.hjycommunity.common.core.exception.CustomException;
import com.msb.hjycommunity.property.domain.HjyOwner;
import com.msb.hjycommunity.property.mapper.HjyOwnerMapper;
import com.msb.hjycommunity.property.service.OwnerActivationService;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OwnerActivationServiceTest {
    private final HjyOwnerMapper mapper = mock(HjyOwnerMapper.class);
    private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
    private final OwnerActivationService service = new OwnerActivationService(mapper, redis);

    @Test
    void issueOnlyForUnactivatedProfileAndStoresExpiringDigest() {
        HjyOwner owner = owner();
        when(mapper.selectOwnerById(42L)).thenReturn(owner);
        @SuppressWarnings("unchecked") ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        String code = service.issue(42L);
        assertEquals(16, code.length());
        verify(values).set(eq("owner:activation:42"), argThat(value -> !value.equals(code) && value.length() == 64),
                eq(15L), eq(TimeUnit.MINUTES));

        owner.setOwnerPassword("existing-hash");
        assertThrows(CustomException.class, () -> service.issue(42L));
        verifyNoMoreInteractions(values);

        owner.setOwnerPassword(null);
        owner.setOwnerStatus("Disable");
        assertThrows(CustomException.class, () -> service.issue(42L));
        assertFalse(service.activate(owner, code, "hash"));
    }

    @Test
    void activationConsumesCodeOnceAndDoesNotOverwritePassword() {
        HjyOwner owner = owner();
        when(redis.execute(any(), anyList(), anyString())).thenReturn(1L, 0L);
        when(mapper.activateOwner(42L, "hash")).thenReturn(1);
        assertTrue(service.activate(owner, "code", "hash"));
        assertFalse(service.activate(owner, "code", "hash"));
        verify(mapper, times(1)).activateOwner(42L, "hash");

        owner.setOwnerPassword("existing-hash");
        assertFalse(service.activate(owner, "code", "hash"));
        verifyNoMoreInteractions(mapper);
    }

    private HjyOwner owner() {
        HjyOwner owner = new HjyOwner();
        owner.setOwnerId(42L);
        owner.setOwnerPhoneNumber("13800000000");
        owner.setOwnerStatus("Enable");
        return owner;
    }
}
