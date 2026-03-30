package com.msb.hjycommunity.web.controller.system;

import com.msb.hjycommunity.common.core.controller.BaseController;
import com.msb.hjycommunity.common.core.domain.BaseResponse;
import com.msb.hjycommunity.common.core.page.PageResult;
import com.msb.hjycommunity.common.utils.SecurityUtils;
import com.msb.hjycommunity.system.domain.SysNotice;
import com.msb.hjycommunity.system.service.SysNoticeService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 通知公告Controller
 */
@RestController
@RequestMapping("/system/notice")
public class SysNoticeController extends BaseController {

    @Resource
    private SysNoticeService noticeService;

    /**
     * 获取通知公告列表
     */
    @GetMapping("/list")
    @PreAuthorize("@pe.hasPerms('system:notice:list')")
    public PageResult list(SysNotice notice) {
        startPage();
        List<SysNotice> list = noticeService.selectNoticeList(notice);
        return getData(list);
    }

    /**
     * 获取通知公告详情
     */
    @GetMapping("/{noticeId}")
    @PreAuthorize("@pe.hasPerms('system:notice:query')")
    public BaseResponse getInfo(@PathVariable Long noticeId) {
        return BaseResponse.success(noticeService.selectNoticeById(noticeId));
    }

    /**
     * 新增通知公告
     */
    @PostMapping
    @PreAuthorize("@pe.hasPerms('system:notice:add')")
    public BaseResponse add(@RequestBody SysNotice notice) {
        notice.setCreateBy(SecurityUtils.getUserName());
        return toAjax(noticeService.insertNotice(notice));
    }

    /**
     * 修改通知公告
     */
    @PutMapping
    @PreAuthorize("@pe.hasPerms('system:notice:edit')")
    public BaseResponse edit(@RequestBody SysNotice notice) {
        notice.setUpdateBy(SecurityUtils.getUserName());
        return toAjax(noticeService.updateNotice(notice));
    }

    /**
     * 删除通知公告
     */
    @DeleteMapping("/{noticeIds}")
    @PreAuthorize("@pe.hasPerms('system:notice:remove')")
    public BaseResponse remove(@PathVariable Long[] noticeIds) {
        return toAjax(noticeService.deleteNoticeByIds(noticeIds));
    }
}
