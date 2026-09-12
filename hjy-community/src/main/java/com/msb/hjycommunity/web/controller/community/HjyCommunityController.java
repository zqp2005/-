package com.msb.hjycommunity.web.controller.community;

import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import com.msb.hjycommunity.community.domain.dto.HjyCommunityExcelDto;
import org.springframework.beans.BeanUtils;
import com.msb.hjycommunity.common.utils.ExcelUtils;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.msb.hjycommunity.common.annotation.Log;
import com.msb.hjycommunity.common.core.page.PageResult;
import com.msb.hjycommunity.common.enums.BusinessType;
import com.msb.hjycommunity.community.domain.vo.HjyCommunityVo;
import com.msb.hjycommunity.community.service.HjyCommunityService;
import com.msb.hjycommunity.common.core.controller.BaseController;
import com.msb.hjycommunity.common.core.domain.BaseResponse;
import com.msb.hjycommunity.community.domain.HjyCommunity;
import com.msb.hjycommunity.community.domain.dto.HjyCommunityDto;
//import com.msb.hjycommunity.community.domain.vo.HjyCommunityVo;
import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author spikeCong
 * @date 2023/3/7
 **/
@RestController
@RequestMapping("/community")
@Slf4j
public class HjyCommunityController extends BaseController {

    @Resource
    private HjyCommunityService hjyCommunityService;

    /**
     * 导出小区数据（Excel流下载）
     */
    @GetMapping("/export")
    public void export(HjyCommunity hjyCommunity, HttpServletResponse response) {
        List<HjyCommunityDto> list = hjyCommunityService.queryList(hjyCommunity);
        List<HjyCommunityExcelDto> dtoList = list.stream().map(item -> {
            HjyCommunityExcelDto dto = new HjyCommunityExcelDto();
            BeanUtils.copyProperties(item, dto);
            return dto;
        }).collect(Collectors.toList());
        ExcelUtils.exportExcel(dtoList, HjyCommunityExcelDto.class, "小区信息.xls", response,
                new ExportParams("小区信息列表", "小区信息"));
    }

    /**
     * 多条件分页查询
     * @param hjyCommunity
     * @return: com.msb.hjycommunity.common.core.page.PageResult
     */
    @GetMapping("/list")
    @PreAuthorize("@pe.hasPerms('system:community:list')")
    public PageResult list(HjyCommunity hjyCommunity){

        startPage();
        List<HjyCommunityDto> list = hjyCommunityService.queryList(hjyCommunity);
        return getData(list);
    }


    /**
     * 新增小区
     * @param hjyCommunity
     * @return: com.msb.hjycommunity.common.core.domain.BaseResponse
     */
    @PostMapping
    @Log(title = "社区管理", businessType = BusinessType.INSERT)
    public BaseResponse add(@RequestBody HjyCommunity hjyCommunity){

        return toAjax(hjyCommunityService.insertHjyCommunity(hjyCommunity));
    }

    @GetMapping("/{communityId}")
    public BaseResponse getInfo(@PathVariable("communityId") Long communityId ){

        return BaseResponse.success(hjyCommunityService.selectHjyCommunityById(communityId));
    }


    /**
     * 修改小区
     * @param hjyCommunity
     * @return: com.msb.hjycommunity.common.core.domain.BaseResponse
     */
    @PutMapping
    @Log(title = "社区管理", businessType = BusinessType.UPDATE)
    public BaseResponse edit(@RequestBody HjyCommunity hjyCommunity){

        return toAjax(hjyCommunityService.updateHjyCommunity(hjyCommunity));
    }
//
    /**
     * 根据Id删除小区数据
     * @param communityIds
     * @return: com.msb.hjycommunity.common.core.domain.BaseResponse
     */
    @DeleteMapping("/{communityIds}")
    @Log(title = "社区管理", businessType = BusinessType.DELETE)
    public BaseResponse delete(@PathVariable Long[] communityIds){

        return toAjax(hjyCommunityService.deleteHjyCommunity(communityIds));
    }
//

    /**
     * 小区下拉列表展示
     * @param hjyCommunity
     * @return: com.msb.hjycommunity.common.core.domain.BaseResponse
     */
    @GetMapping("/queryPullDown")
    public BaseResponse queryPullDown(HjyCommunity hjyCommunity){

        //打印入参日志
        log.info("log() called with parameters => [hjyCommunity = {}]",hjyCommunity);

        List<HjyCommunityVo> voList = null;
        try {
            voList = hjyCommunityService.queryPullDown(hjyCommunity);
        } catch (Exception e) {
            //e.printStackTrace();
            log.warn("获取小区下拉列表失败!",e);
        }

        //打印日志 返回结果
        log.info("log() returned: {} ",voList);
        return BaseResponse.success(voList);
    }
}
