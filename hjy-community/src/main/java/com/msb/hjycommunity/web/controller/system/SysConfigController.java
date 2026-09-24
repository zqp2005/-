package com.msb.hjycommunity.web.controller.system;

import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import com.msb.hjycommunity.system.domain.dto.SysConfigExcelDto;
import org.springframework.beans.BeanUtils;
import com.msb.hjycommunity.common.utils.ExcelUtils;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.msb.hjycommunity.common.core.controller.BaseController;
import com.msb.hjycommunity.common.core.domain.BaseResponse;
import com.msb.hjycommunity.common.core.page.PageResult;
import com.msb.hjycommunity.common.utils.SecurityUtils;
import com.msb.hjycommunity.system.domain.SysConfig;
import com.msb.hjycommunity.system.service.SysConfigService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 参数配置Controller
 */
@RestController
@RequestMapping("/system/config")
public class SysConfigController extends BaseController {

    @Resource
    private SysConfigService configService;

    /**
     * 导出参数配置数据（Excel流下载）
     */
    @GetMapping("/export")
    @PreAuthorize("@pe.hasPerms('system:config:export')")
    public void export(SysConfig config, HttpServletResponse response) {
        List<SysConfig> list = configService.selectConfigList(config);
        List<SysConfigExcelDto> dtoList = list.stream().map(item -> {
            SysConfigExcelDto dto = new SysConfigExcelDto();
            BeanUtils.copyProperties(item, dto);
            return dto;
        }).collect(Collectors.toList());
        ExcelUtils.exportExcel(dtoList, SysConfigExcelDto.class, "参数配置.xls", response,
                new ExportParams("参数配置列表", "参数配置"));
    }

    /**
     * 获取参数配置列表
     */
    @GetMapping("/list")
    @PreAuthorize("@pe.hasPerms('system:config:list')")
    public PageResult list(SysConfig config) {
        startPage();
        List<SysConfig> list = configService.selectConfigList(config);
        return getData(list);
    }

    /**
     * 获取参数配置详情
     */
    @GetMapping("/{configId}")
    @PreAuthorize("@pe.hasPerms('system:config:query')")
    public BaseResponse getInfo(@PathVariable Long configId) {
        return BaseResponse.success(configService.selectConfigById(configId));
    }

    /**
     * 根据键名获取参数值
     */
    @GetMapping("/configKey/{configKey}")
    public BaseResponse getConfigKey(@PathVariable String configKey) {
        return BaseResponse.success(configService.selectConfigByKey(configKey));
    }

    /**
     * 新增参数配置
     */
    @PostMapping
    @PreAuthorize("@pe.hasPerms('system:config:add')")
    public BaseResponse add(@RequestBody SysConfig config) {
        config.setCreateBy(SecurityUtils.getUserName());
        return toAjax(configService.insertConfig(config));
    }

    /**
     * 修改参数配置
     */
    @PutMapping
    @PreAuthorize("@pe.hasPerms('system:config:edit')")
    public BaseResponse edit(@RequestBody SysConfig config) {
        config.setUpdateBy(SecurityUtils.getUserName());
        return toAjax(configService.updateConfig(config));
    }

    /**
     * 删除参数配置
     */
    @DeleteMapping("/{configIds}")
    @PreAuthorize("@pe.hasPerms('system:config:remove')")
    public BaseResponse remove(@PathVariable Long[] configIds) {
        return toAjax(configService.deleteConfigByIds(configIds));
    }

    /**
     * 清空缓存
     */
    @DeleteMapping("/clearCache")
    @PreAuthorize("@pe.hasPerms('system:config:remove')")
    public BaseResponse<?> clearCache() {
        configService.clearCache();
        return BaseResponse.success("清空缓存成功");
    }
}
