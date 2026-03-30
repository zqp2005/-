package com.msb.hjycommunity.property.service.impl;

import com.msb.hjycommunity.common.utils.SecurityUtils;
import com.msb.hjycommunity.property.domain.HjyComment;
import com.msb.hjycommunity.property.domain.HjyInteraction;
import com.msb.hjycommunity.property.mapper.HjyInteractionMapper;
import com.msb.hjycommunity.property.service.HjyInteractionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 社区互动Service实现
 */
@Service
public class HjyInteractionServiceImpl implements HjyInteractionService {

    @Resource
    private HjyInteractionMapper interactionMapper;

    @Override
    public List<HjyInteraction> selectInteractionList(HjyInteraction interaction) {
        return interactionMapper.selectInteractionList(interaction);
    }

    @Override
    public HjyInteraction selectInteractionById(Long interactionId) {
        return interactionMapper.selectInteractionById(interactionId);
    }

    @Override
    @Transactional
    public int insertInteraction(HjyInteraction interaction) {
        interaction.setCreateBy(SecurityUtils.getUserName());
        return interactionMapper.insertInteraction(interaction);
    }

    @Override
    @Transactional
    public int updateInteraction(HjyInteraction interaction) {
        interaction.setUpdateBy(SecurityUtils.getUserName());
        return interactionMapper.updateInteraction(interaction);
    }

    @Override
    @Transactional
    public int deleteInteractionById(Long interactionId) {
        return interactionMapper.deleteInteractionById(interactionId);
    }

    @Override
    @Transactional
    public int deleteInteractionByIds(Long[] interactionIds) {
        return interactionMapper.deleteInteractionByIds(interactionIds);
    }

    @Override
    public List<HjyComment> selectCommentList(Long interactionId) {
        return interactionMapper.selectCommentList(interactionId);
    }

    @Override
    @Transactional
    public int insertComment(HjyComment comment) {
        comment.setCreateBy(SecurityUtils.getUserName());
        return interactionMapper.insertComment(comment);
    }

    @Override
    @Transactional
    public int deleteCommentById(Long commentId) {
        return interactionMapper.deleteCommentById(commentId);
    }
}
