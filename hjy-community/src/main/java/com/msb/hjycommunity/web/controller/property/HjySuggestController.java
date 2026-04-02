package com.msb.hjycommunity.web.controller.property;

import com.msb.hjycommunity.common.core.controller.BaseController;
import com.msb.hjycommunity.common.core.domain.BaseResponse;
import com.msb.hjycommunity.common.core.page.PageResult;
import com.msb.hjycommunity.common.utils.SecurityUtils;
import com.msb.hjycommunity.property.domain.HjySuggest;
import com.msb.hjycommunity.property.service.HjySuggestService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 投诉建议Controller
 */
@RestController
@RequestMapping("/system/suggest")
public class HjySuggestController extends BaseController {

    @Resource
    private HjySuggestService suggestService;

    /**
     * 获取投诉建议列表
     */
    @GetMapping("/list")
    @PreAuthorize("@pe.hasPerms('system:suggest:list')")
    public PageResult list(HjySuggest suggest) {
        startPage();
        List<HjySuggest> list = suggestService.selectSuggestList(suggest);
        return getData(list);
    }

    /**
     * 获取投诉建议详情
     */
    @GetMapping("/{complaintSuggestId}")
    @PreAuthorize("@pe.hasPerms('system:suggest:query')")
    public BaseResponse getInfo(@PathVariable Long complaintSuggestId) {
        return BaseResponse.success(suggestService.selectSuggestById(complaintSuggestId));
    }

    /**
     * 新增投诉建议
     */
    @PostMapping
    @PreAuthorize("@pe.hasPerms('system:suggest:add')")
    public BaseResponse add(@RequestBody HjySuggest suggest) {
        suggest.setCreateBy(SecurityUtils.getUserName());
        return toAjax(suggestService.insertSuggest(suggest));
    }

    /**
     * 修改投诉建议
     */
    @PutMapping
    @PreAuthorize("@pe.hasPerms('system:suggest:edit')")
    public BaseResponse edit(@RequestBody HjySuggest suggest) {
        suggest.setUpdateBy(SecurityUtils.getUserName());
        return toAjax(suggestService.updateSuggest(suggest));
    }

    /**
     * 删除投诉建议
     */
    @DeleteMapping("/{complaintSuggestIds}")
    @PreAuthorize("@pe.hasPerms('system:suggest:remove')")
    public BaseResponse remove(@PathVariable Long[] complaintSuggestIds) {
        return toAjax(suggestService.deleteSuggestByIds(complaintSuggestIds));
    }
}
