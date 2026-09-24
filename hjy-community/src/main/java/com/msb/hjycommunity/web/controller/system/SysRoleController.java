package com.msb.hjycommunity.web.controller.system;

import org.springframework.security.access.prepost.PreAuthorize;

import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import com.msb.hjycommunity.system.domain.dto.SysRoleExcelDto;
import org.springframework.beans.BeanUtils;
import com.msb.hjycommunity.common.utils.ExcelUtils;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.msb.hjycommunity.common.constant.UserConstants;
import com.msb.hjycommunity.common.core.controller.BaseController;
import com.msb.hjycommunity.common.core.domain.BaseResponse;
import com.msb.hjycommunity.common.core.page.PageResult;
import com.msb.hjycommunity.common.utils.SecurityUtils;
import com.msb.hjycommunity.common.utils.ServletUtils;
import com.msb.hjycommunity.framework.service.SysPermissionService;
import com.msb.hjycommunity.system.domain.LoginUser;
import com.msb.hjycommunity.system.domain.SysRole;
import com.msb.hjycommunity.system.service.SysRoleService;
import com.msb.hjycommunity.system.service.SysUserService;
import com.msb.hjycommunity.system.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * @author spikeCong
 * @date 2023/5/26
 **/
@RestController
@RequestMapping("/system/role")
public class SysRoleController extends BaseController {

    @Autowired
    private SysRoleService roleService;

    /**
     * 导出角色信息数据（Excel流下载）
     */
    @GetMapping("/export")
    @PreAuthorize("@pe.hasPerms('system:role:export')")
    public void export(SysRole role, HttpServletResponse response) {
        List<SysRole> list = roleService.selectRoleList(role);
        List<SysRoleExcelDto> dtoList = list.stream().map(item -> {
            SysRoleExcelDto dto = new SysRoleExcelDto();
            BeanUtils.copyProperties(item, dto);
            return dto;
        }).collect(Collectors.toList());
        ExcelUtils.exportExcel(dtoList, SysRoleExcelDto.class, "角色信息.xls", response,
                new ExportParams("角色信息列表", "角色信息"));
    }

    @Autowired
    private TokenService tokenService;

    @Autowired
    private SysPermissionService permissionService;

    @Autowired
    private SysUserService userService;

    @RequestMapping("/list")
    @PreAuthorize("@pe.hasPerms('system:role:list')")
    public PageResult list(SysRole role){
        startPage();
        List<SysRole> sysRoles = roleService.selectRoleList(role);
        return getData(sysRoles);
    }

    @GetMapping(value = "/{roleId}")
    @PreAuthorize("@pe.hasPerms('system:role:query')")
    public BaseResponse getInfo(@PathVariable Long roleId){
        return BaseResponse.success(roleService.selectRoleById(roleId));
    }

    @PutMapping("/changeStatus")
    @PreAuthorize("@pe.isAdmin() and @pe.hasPerms('system:role:edit')")
    public BaseResponse changeStatus(@RequestBody SysRole role){
        roleService.checkRoleAllowed(role);
        role.setUpdateBy(SecurityUtils.getUserName());
        return toAjax(roleService.updateRoleStatus(role));
    }

    @DeleteMapping("/{roleIds}")
    @PreAuthorize("@pe.isAdmin() and @pe.hasPerms('system:role:remove')")
    public BaseResponse remove(@PathVariable Long[] roleIds){
        return toAjax(roleService.deleteRoleByIds(roleIds));
    }

    //获取角色选择框列表
    @GetMapping("/optionselect")
    @PreAuthorize("@pe.hasAnyPerms('system:role:query,system:user:add,system:user:edit')")
    public BaseResponse optionSelect(){
        return BaseResponse.success(roleService.selectRoleAll());
    }


    @PostMapping
    @PreAuthorize("@pe.isAdmin() and @pe.hasPerms('system:role:add')")
    public BaseResponse add(@RequestBody SysRole role){
        if(UserConstants.NOT_UNIQUE.equals(roleService.checkRoleNameUnique(role))){
            return BaseResponse.fail("新增角色" + role.getRoleName() + "失败,角色名称已存在");
        }
        else if(UserConstants.NOT_UNIQUE.equals(roleService.checkRoleKeyUnique(role))){
            return BaseResponse.fail("新增角色" + role.getRoleKey() + "失败,角色权限已存在");
        }

        role.setCreateBy(SecurityUtils.getUserName());
        return toAjax(roleService.insertRole(role));
    }

    /**
     * 修改角色
     */
    @PutMapping
    @PreAuthorize("@pe.isAdmin() and @pe.hasPerms('system:role:edit')")
    public BaseResponse edit(@RequestBody SysRole role){

        roleService.checkRoleAllowed(role);
        if(UserConstants.NOT_UNIQUE.equals(roleService.checkRoleNameUnique(role))){
            return BaseResponse.fail("修改角色" + role.getRoleName() + "失败,角色名称已存在");
        }
        else if(UserConstants.NOT_UNIQUE.equals(roleService.checkRoleKeyUnique(role))){
            return BaseResponse.fail("修改角色" + role.getRoleKey() + "失败,角色权限已存在");
        }

        role.setUpdateBy(SecurityUtils.getUserName());

        if(roleService.updateRole(role) > 0){
            //更新用户缓存
            LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
            if(!Objects.isNull(loginUser) && !loginUser.getUser().isAdmin()){
                //获取当前用户菜单权限
                loginUser.setPermissions(permissionService.getMenuPermission(loginUser.getUser()));
                loginUser.setUser(userService.selectUserByUserName(loginUser.getUser().getUserName()));
                tokenService.setLoginUser(loginUser);
            }

            return BaseResponse.success("修改角色成功");
        }

        return BaseResponse.fail("修改角色" + role.getRoleName() + "失败,请联系管理员");
    }
}
