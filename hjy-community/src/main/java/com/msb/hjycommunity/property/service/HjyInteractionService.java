package com.msb.hjycommunity.property.service;

import com.msb.hjycommunity.property.domain.HjyComment;
import com.msb.hjycommunity.property.domain.HjyInteraction;

import java.util.List;

/**
 * 社区互动Service接口
 */
public interface HjyInteractionService {

    /**
     * 查询互动列表
     */
    List<HjyInteraction> selectInteractionList(HjyInteraction interaction);

    /**
     * 根据ID查询互动
     */
    HjyInteraction selectInteractionById(Long interactionId);

    /**
     * 新增互动
     */
    int insertInteraction(HjyInteraction interaction);

    /**
     * 修改互动
     */
    int updateInteraction(HjyInteraction interaction);

    /**
     * 删除互动
     */
    int deleteInteractionById(Long interactionId);

    /**
     * 批量删除互动
     */
    int deleteInteractionByIds(Long[] interactionIds);

    /**
     * 查询评论列表
     */
    List<HjyComment> selectCommentList(Long interactionId);

    /**
     * 新增评论
     */
    int insertComment(HjyComment comment);

    /**
     * 删除评论
     */
    int deleteCommentById(Long commentId);
}
