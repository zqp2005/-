package com.msb.hjycommunity.security;

import com.msb.hjycommunity.common.core.page.PageResult;
import com.msb.hjycommunity.framework.security.service.PermsExpressionService;
import com.msb.hjycommunity.system.domain.LoginUser;
import com.msb.hjycommunity.system.domain.SysUser;
import com.msb.hjycommunity.system.service.*;
import com.msb.hjycommunity.web.controller.system.*;
import com.msb.hjycommunity.web.controller.property.*;
import com.msb.hjycommunity.web.controller.community.HjyCommunityController;
import com.msb.hjycommunity.web.controller.common.ExportExcelController;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** 验证真实 Spring 方法安全代理；不启动服务、不连接数据库或 Redis。 */
class ManagementAuthorizationTest {
    private AnnotationConfigApplicationContext context;
    private SysUserController controller;
    private SysUserService users;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigApplicationContext(Config.class);
        controller = context.getBean(SysUserController.class);
        users = context.getBean(SysUserService.class);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        authenticate(2L, "system:owner:list");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
        com.github.pagehelper.PageHelper.clearPage();
        context.close();
    }

    @Test
    void ordinaryUserCannotManageAccountsOrReadFullUserList() {
        assertThrows(AccessDeniedException.class, () -> controller.list(new SysUser()));
        assertThrows(AccessDeniedException.class, () -> controller.restPwd(new SysUser()));
        assertThrows(AccessDeniedException.class, () -> controller.add(new SysUser()));
        assertThrows(AccessDeniedException.class, () -> controller.edit(new SysUser()));
        assertThrows(AccessDeniedException.class, () -> controller.remove(new Long[]{3L}));
        verifyNoInteractions(users);
    }

    @Test
    void delegatedPermissionAloneCannotEscalateAccounts() {
        authenticate(2L, "*:*:*");
        assertThrows(AccessDeniedException.class, () -> controller.restPwd(new SysUser()));
        assertThrows(AccessDeniedException.class, () -> context.getBean(SysRoleController.class).edit(null));
        assertThrows(AccessDeniedException.class, () -> context.getBean(SysMenuController.class).add(null));
        verifyNoInteractions(users);
    }

    @Test
    void platformAdminCanResetNonAdminPassword() {
        authenticate(1L, "*:*:*");
        SysUser target = new SysUser();
        target.setUserId(3L);
        target.setPassword("sample-pass");
        when(users.resetPwd(any())).thenReturn(1);
        assertTrue(controller.restPwd(target).isSuccess());
        verify(users).resetPwd(target);
        assertNotEquals("sample-pass", target.getPassword());
    }

    @Test
    void repairDispatcherReceivesOnlySafeOptions() {
        authenticate(2L, "system:repair:assign");
        SysUser worker = new SysUser();
        worker.setUserId(5L);
        worker.setUserName("worker");
        worker.setNickName("维修人员");
        worker.setPhonenumber("13800000000");
        worker.setEmail("private@example.invalid");
        when(users.selectUserList(any())).thenReturn(Collections.singletonList(worker));
        SysUser maliciousFilter = new SysUser();
        maliciousFilter.setStatus("1");
        PageResult result = controller.list(maliciousFilter);
        verify(users).selectUserList(argThat(q -> "0".equals(q.getStatus())));
        Map<?, ?> option = (Map<?, ?>) result.getRows().get(0);
        assertEquals(new HashSet<>(Arrays.asList("userId", "userName", "nickName")), option.keySet());
        assertEquals(1, result.getTotal());
        assertEquals(5L, option.get("userId"));
        assertThrows(AccessDeniedException.class, () -> controller.export(new SysUser(), null));
    }

    @Test
    void authorizedUserListKeepsExistingResponse() {
        authenticate(2L, "system:user:list");
        SysUser user = new SysUser();
        when(users.selectUserList(any())).thenReturn(Collections.singletonList(user));
        assertSame(user, controller.list(new SysUser()).getRows().get(0));
    }

    @Test
    void reviewedControllersHaveGuardsOnAllWritesAndExports() {
        Class<?>[] types = {SysUserController.class, SysRoleController.class, SysMenuController.class,
                SysDeptController.class, SysPostController.class, SysDictDataController.class,
                SysDictTypeController.class, SysConfigController.class, HjyCommunityController.class,
                ExportExcelController.class, HjyBuildingController.class, HjyInteractionController.class,
                HjyOwnerController.class, HjyOwnerRoomController.class, HjyRepairController.class,
                HjyRoomController.class, HjySuggestController.class, HjyUnitController.class, HjyVisitorController.class};
        for (Class<?> type : types) {
            for (Method method : type.getDeclaredMethods()) {
                if (method.isAnnotationPresent(PostMapping.class) || method.isAnnotationPresent(PutMapping.class)
                        || method.isAnnotationPresent(DeleteMapping.class) || method.getName().startsWith("export")) {
                    assertNotNull(method.getAnnotation(PreAuthorize.class), type.getSimpleName() + "." + method.getName());
                }
            }
        }
    }

    @Test
    void missingIdentityIsNotAdmin() {
        when(context.getBean(TokenService.class).getLoginUser(any())).thenReturn(null);
        assertFalse(context.getBean(PermsExpressionService.class).isAdmin());
    }

    private void authenticate(Long id, String... permissions) {
        SysUser user = new SysUser();
        user.setUserId(id);
        user.setUserName("test-user");
        LoginUser login = new LoginUser(user, new HashSet<>(Arrays.asList(permissions)));
        when(context.getBean(TokenService.class).getLoginUser(any())).thenReturn(login);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(login, null, Collections.emptyList()));
    }

    @Configuration
    @EnableGlobalMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
    static class Config {
        @Bean TokenService tokens() { return mock(TokenService.class); }
        @Bean SysUserService users() { return mock(SysUserService.class); }
        @Bean SysRoleService roles() { return mock(SysRoleService.class); }
        @Bean SysPostService posts() { return mock(SysPostService.class); }
        @Bean SysMenuService menus() { return mock(SysMenuService.class); }
        @Bean com.msb.hjycommunity.framework.service.SysPermissionService permissions() {
            return mock(com.msb.hjycommunity.framework.service.SysPermissionService.class);
        }
        @Bean(name = "pe") PermsExpressionService pe() { return new PermsExpressionService(); }
        @Bean SysUserController usersController() { return new SysUserController(); }
        @Bean SysRoleController rolesController() { return new SysRoleController(); }
        @Bean SysMenuController menusController() { return new SysMenuController(); }
    }
}
