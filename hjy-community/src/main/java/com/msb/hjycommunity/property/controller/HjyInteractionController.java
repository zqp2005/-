package com.msb.hjycommunity.property.controller;

import com.msb.hjycommunity.common.core.controller.BaseController;
import com.msb.hjycommunity.common.core.domain.BaseResponse;
import com.msb.hjycommunity.common.core.page.PageResult;
import com.msb.hjycommunity.common.utils.SecurityUtils;
import com.msb.hjycommunity.property.domain.HjyComment;
import com.msb.hjycommunity.property.domain.HjyInteraction;
import com.msb.hjycommunity.property.service.HjyInteractionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 社区互动Controller
 */
@RestController
@RequestMapping("/system/interaction")
public class HjyInteractionController extends BaseController {

    @Resource
    private HjyInteractionService interactionService;

    /**
     * 获取互动列表
     */
    @GetMapping("/list")
    @PreAuthorize("@pe.hasPerms('system:interaction:list')")
    public PageResult list(HjyInteraction interaction) {
        startPage();
        List<HjyInteraction> list = interactionService.selectInteractionList(interaction);
        return getData(list);
    }

    /**
     * 获取互动详情
     */
    @GetMapping("/{interactionId}")
    @PreAuthorize("@pe.hasPerms('system:interaction:query')")
    public BaseResponse getInfo(@PathVariable Long interactionId) {
        HjyInteraction interaction = interactionService.selectInteractionById(interactionId);
        List<HjyComment> comments = interactionService.selectCommentList(interactionId);
        Map<String, Object> result = new HashMap<>();
        result.put("data", interaction);
        result.put("comments", comments);
        return BaseResponse.success(result);
    }

    /**
     * 新增互动
     */
    @PostMapping
    @PreAuthorize("@pe.hasPerms('system:interaction:add')")
    public BaseResponse add(@RequestBody HjyInteraction interaction) {
        interaction.setCreateBy(SecurityUtils.getUserName());
        return toAjax(interactionService.insertInteraction(interaction));
    }

    /**
     * 修改互动
     */
    @PutMapping
    @PreAuthorize("@pe.hasPerms('system:interaction:edit')")
    public BaseResponse edit(@RequestBody HjyInteraction interaction) {
        interaction.setUpdateBy(SecurityUtils.getUserName());
        return toAjax(interactionService.updateInteraction(interaction));
    }

    /**
     * 删除互动
     */
    @DeleteMapping("/{interactionIds}")
    @PreAuthorize("@pe.hasPerms('system:interaction:remove')")
    public BaseResponse remove(@PathVariable Long[] interactionIds) {
        return toAjax(interactionService.deleteInteractionByIds(interactionIds));
    }

    /**
     * 新增评论
     */
    @PostMapping("/comment")
    @PreAuthorize("@pe.hasPerms('system:interaction:add')")
    public BaseResponse addComment(@RequestBody HjyComment comment) {
        comment.setCreateBy(SecurityUtils.getUserName());
        return toAjax(interactionService.insertComment(comment));
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/comment/{commentId}")
    @PreAuthorize("@pe.hasPerms('system:interaction:remove')")
    public BaseResponse removeComment(@PathVariable Long commentId) {
        return toAjax(interactionService.deleteCommentById(commentId));
    }
}
