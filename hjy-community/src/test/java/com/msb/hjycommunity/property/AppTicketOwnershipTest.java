package com.msb.hjycommunity.property;

import com.msb.hjycommunity.framework.security.filter.AppAuthFilter;
import com.msb.hjycommunity.property.domain.*;
import com.msb.hjycommunity.property.service.*;
import com.msb.hjycommunity.web.controller.app.*;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.*;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import java.io.InputStream;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppTicketOwnershipTest {
    @Test void authenticatedResidentSubmissionStoresStableCreatorThroughRealFilterAndService() throws Exception {
        com.msb.hjycommunity.framework.security.service.AppOwnerTokenService tokens = mock(com.msb.hjycommunity.framework.security.service.AppOwnerTokenService.class);
        com.msb.hjycommunity.framework.security.domain.AppOwnerToken token = new com.msb.hjycommunity.framework.security.domain.AppOwnerToken();
        token.setOwnerId(42L); token.setRealName("测试居民"); token.setPhone("13800000000");
        when(tokens.getOwnerToken(any())).thenReturn(token);
        AppAuthFilter filter = new AppAuthFilter(); ReflectionTestUtils.setField(filter, "appOwnerTokenService", tokens);
        com.msb.hjycommunity.property.mapper.HjyRepairMapper mapper = mock(com.msb.hjycommunity.property.mapper.HjyRepairMapper.class);
        com.msb.hjycommunity.property.service.impl.HjyRepairServiceImpl repairs = new com.msb.hjycommunity.property.service.impl.HjyRepairServiceImpl();
        ReflectionTestUtils.setField(repairs, "repairMapper", mapper);
        AppRepairController controller = new AppRepairController(); ReflectionTestUtils.setField(controller, "repairService", repairs);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/app/repair");
        request.setServletPath("/app/repair");
        com.msb.hjycommunity.property.domain.dto.AppRepairRequest body = new com.msb.hjycommunity.property.domain.dto.AppRepairRequest();
        body.setRepairContent("测试故障");
        try {
            filter.doFilter(request, new org.springframework.mock.web.MockHttpServletResponse(), (req, res) -> controller.add(body, (javax.servlet.http.HttpServletRequest) req));
            verify(mapper).insertRepair(argThat(r -> "owner:42".equals(r.getCreateBy()) && "测试居民".equals(r.getOwnerRealName()) && "Pending".equals(r.getRepairState())));
        } finally { org.springframework.security.core.context.SecurityContextHolder.clearContext(); }
    }
    @Test void managementAccountCannotCollideWithResidentSubmitterNamespace() {
        com.msb.hjycommunity.system.service.impl.SysUserServiceImpl users = new com.msb.hjycommunity.system.service.impl.SysUserServiceImpl();
        com.msb.hjycommunity.system.domain.SysUser user = new com.msb.hjycommunity.system.domain.SysUser();
        user.setUserName(" OWNER:42");
        assertThrows(com.msb.hjycommunity.common.core.exception.CustomException.class, () -> users.insertUser(user));
        assertThrows(com.msb.hjycommunity.common.core.exception.CustomException.class, () -> users.updateUser(user));
        assertThrows(com.msb.hjycommunity.common.core.exception.CustomException.class, () -> users.updateUserProfile(user));
        assertThrows(com.msb.hjycommunity.common.core.exception.CustomException.class, () -> users.updateUserStatus(user));
        assertThrows(com.msb.hjycommunity.common.core.exception.CustomException.class, () -> users.resetPwd(user));
    }
    @AfterEach void cleanup() { RequestContextHolder.resetRequestAttributes(); com.github.pagehelper.PageHelper.clearPage(); }

    @Test void listsUseTrustedSubmitterEvenIfNameAndPhoneChange() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        request.setAttribute(AppAuthFilter.ATTR_OWNER_ID, 42L);
        request.setAttribute(AppAuthFilter.ATTR_OWNER_NAME, "%");
        request.setAttribute(AppAuthFilter.ATTR_OWNER_PHONE, "%");
        HjyRepairService repairs = mock(HjyRepairService.class);
        HjySuggestService suggestions = mock(HjySuggestService.class);
        when(repairs.selectRepairList(any())).thenReturn(Collections.emptyList());
        when(suggestions.selectSuggestList(any())).thenReturn(Collections.emptyList());
        AppRepairController rc = new AppRepairController(); ReflectionTestUtils.setField(rc, "repairService", repairs);
        AppSuggestController sc = new AppSuggestController(); ReflectionTestUtils.setField(sc, "suggestService", suggestions);
        rc.list(request); sc.list(request);
        verify(repairs).selectRepairList(argThat(q -> "owner:42".equals(q.getCreateBy()) && q.getOwnerRealName() == null && q.getOwnerPhoneNumber() == null));
        verify(suggestions).selectSuggestList(argThat(q -> "owner:42".equals(q.getCreateBy()) && q.getOwnerRealName() == null && q.getOwnerPhoneNumber() == null));
        request.removeAttribute(AppAuthFilter.ATTR_OWNER_ID);
        assertThrows(RuntimeException.class, () -> rc.list(request));
        assertThrows(RuntimeException.class, () -> sc.list(request));
    }

    @Test void bothListMappersUseExactSubmitterMatch() throws Exception {
        for (Class<?> type : new Class<?>[]{HjyRepair.class, HjySuggest.class}) {
            Configuration config = new Configuration(); config.getTypeAliasRegistry().registerAliases("com.msb.hjycommunity.property.domain");
            String file = "mapper/property/" + type.getSimpleName() + "Mapper.xml";
            try (InputStream stream = getClass().getClassLoader().getResourceAsStream(file)) {
                new XMLMapperBuilder(stream, config, file, config.getSqlFragments()).parse();
            }
            com.msb.hjycommunity.common.core.domain.BaseEntity query = (com.msb.hjycommunity.common.core.domain.BaseEntity) type.newInstance();
            query.setCreateBy("owner:42");
            String sql = config.getMappedStatement("com.msb.hjycommunity.property.mapper." + type.getSimpleName() + "Mapper.select" + type.getSimpleName().substring(3) + "List").getBoundSql(query).getSql();
            assertTrue(sql.contains("create_by = ?")); assertFalse(sql.contains("LIKE"));
        }
    }
}
