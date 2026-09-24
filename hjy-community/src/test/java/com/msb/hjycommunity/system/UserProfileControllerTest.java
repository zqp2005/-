package com.msb.hjycommunity.system;

import com.msb.hjycommunity.system.domain.LoginUser;
import com.msb.hjycommunity.system.domain.SysUser;
import com.msb.hjycommunity.system.service.SysPostService;
import com.msb.hjycommunity.system.service.SysRoleService;
import com.msb.hjycommunity.system.service.SysUserService;
import com.msb.hjycommunity.system.service.TokenService;
import com.msb.hjycommunity.web.controller.system.SysUserController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserProfileControllerTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void profilePathReturnsCurrentUserInsteadOfTreatingProfileAsUserId() throws Exception {
        SysUserService users = mock(SysUserService.class);
        SysUser current = new SysUser();
        current.setUserId(1L);
        current.setUserName("admin");
        current.setNickName("msb");
        when(users.selectUserById(1L)).thenReturn(current);
        when(users.selectUserRoleGroup("admin")).thenReturn("超级管理员");
        when(users.selectUserPostGroup("admin")).thenReturn("董事长");

        LoginUser loginUser = new LoginUser(current, Collections.singleton("*:*:*"));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList()));

        SysUserController controller = new SysUserController();
        ReflectionTestUtils.setField(controller, "userService", users);
        ReflectionTestUtils.setField(controller, "roleService", mock(SysRoleService.class));
        ReflectionTestUtils.setField(controller, "postService", mock(SysPostService.class));
        ReflectionTestUtils.setField(controller, "tokenService", mock(TokenService.class));
        ReflectionTestUtils.setField(controller, "uploadPath", "./uploads");
        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).build();

        mvc.perform(get("/system/user/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userName").value("admin"))
                .andExpect(jsonPath("$.roleGroup").value("超级管理员"))
                .andExpect(jsonPath("$.postGroup").value("董事长"));
    }
}
