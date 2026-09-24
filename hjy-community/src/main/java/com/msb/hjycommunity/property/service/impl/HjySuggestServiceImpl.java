package com.msb.hjycommunity.property.service.impl;

import com.msb.hjycommunity.common.core.exception.CustomException;
import com.msb.hjycommunity.common.utils.SecurityUtils;
import com.msb.hjycommunity.property.constant.SuggestState;
import com.msb.hjycommunity.property.domain.HjySuggest;
import com.msb.hjycommunity.property.mapper.HjySuggestMapper;
import com.msb.hjycommunity.property.service.HjySuggestService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 投诉建议Service实现
 */
@Service
public class HjySuggestServiceImpl implements HjySuggestService {

    /** 大陆 11 位手机号 */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    @Resource
    private HjySuggestMapper suggestMapper;

    @Override
    public List<HjySuggest> selectSuggestList(HjySuggest suggest) {
        return suggestMapper.selectSuggestList(suggest);
    }

    @Override
    public HjySuggest selectSuggestById(Long complaintSuggestId) {
        return suggestMapper.selectSuggestById(complaintSuggestId);
    }

    @Override
    @Transactional
    public int insertSuggest(HjySuggest suggest) {
        // 业主电话非空时校验手机号格式
        String phone = suggest.getOwnerPhoneNumber();
        if (phone != null && !phone.trim().isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
            throw new CustomException(500, "手机号格式不正确");
        }
        suggest.setCreateBy(SecurityUtils.getUserName());
        // 状态一律由后端控制为待受理
        suggest.setComplaintState(SuggestState.PENDING);
        suggest.setHandleBy(null);
        suggest.setHandleTime(null);
        suggest.setReplyContent(null);
        return suggestMapper.insertSuggest(suggest);
    }

    @Override
    @Transactional
    public int updateSuggest(HjySuggest suggest) {
        suggest.setExpectedState(null);
        // 流转字段只允许动作接口修改，普通编辑一律忽略（mapper 动态 SQL 判空自动跳过）
        suggest.setComplaintState(null);
        suggest.setHandleBy(null);
        suggest.setHandleTime(null);
        suggest.setReplyContent(null);
        suggest.setUpdateBy(SecurityUtils.getUserName());
        return suggestMapper.updateSuggest(suggest);
    }

    @Override
    @Transactional
    public int deleteSuggestById(Long complaintSuggestId) {
        return suggestMapper.deleteSuggestById(complaintSuggestId);
    }

    @Override
    @Transactional
    public int deleteSuggestByIds(Long[] complaintSuggestIds) {
        return suggestMapper.deleteSuggestByIds(complaintSuggestIds);
    }

    @Override
    @Transactional
    public int acceptSuggest(Long complaintSuggestId) {
        HjySuggest suggest = getSuggestOrThrow(complaintSuggestId);
        assertCanTransit(suggest, SuggestState.PROCESSING, "受理");

        HjySuggest update = new HjySuggest();
        update.setComplaintSuggestId(complaintSuggestId);
        update.setComplaintState(SuggestState.PROCESSING);
        update.setHandleBy(SecurityUtils.getUserName());
        update.setHandleTime(new Date());
        update.setUpdateBy(SecurityUtils.getUserName());
        update.setExpectedState(suggest.getComplaintState());
        int changed = suggestMapper.updateSuggest(update);
        if (changed != 1) throw new CustomException(409, "状态已变化，请刷新后重试");
        return changed;
    }

    @Override
    @Transactional
    public int replySuggest(Long complaintSuggestId, String replyContent) {
        if (replyContent == null || replyContent.trim().isEmpty()) {
            throw new CustomException(500, "回复内容不能为空");
        }
        HjySuggest suggest = getSuggestOrThrow(complaintSuggestId);
        assertCanTransit(suggest, SuggestState.REPLIED, "回复");

        HjySuggest update = new HjySuggest();
        update.setComplaintSuggestId(complaintSuggestId);
        update.setComplaintState(SuggestState.REPLIED);
        update.setReplyContent(replyContent);
        update.setUpdateBy(SecurityUtils.getUserName());
        update.setExpectedState(suggest.getComplaintState());
        int changed = suggestMapper.updateSuggest(update);
        if (changed != 1) throw new CustomException(409, "状态已变化，请刷新后重试");
        return changed;
    }

    @Override
    @Transactional
    public int closeSuggest(Long complaintSuggestId, String reason) {
        HjySuggest suggest = getSuggestOrThrow(complaintSuggestId);
        // Pending 直接关闭（不予受理）必须填原因
        if (SuggestState.PENDING.equals(suggest.getComplaintState())
                && (reason == null || reason.trim().isEmpty())) {
            throw new CustomException(500, "不予受理必须填写原因");
        }
        assertCanTransit(suggest, SuggestState.CLOSED, "关闭");

        HjySuggest update = new HjySuggest();
        update.setComplaintSuggestId(complaintSuggestId);
        update.setComplaintState(SuggestState.CLOSED);
        update.setRemark(reason);
        update.setUpdateBy(SecurityUtils.getUserName());
        update.setExpectedState(suggest.getComplaintState());
        int changed = suggestMapper.updateSuggest(update);
        if (changed != 1) throw new CustomException(409, "状态已变化，请刷新后重试");
        return changed;
    }

    /** 查询投诉建议，不存在则抛业务异常 */
    private HjySuggest getSuggestOrThrow(Long complaintSuggestId) {
        HjySuggest suggest = suggestMapper.selectSuggestById(complaintSuggestId);
        if (suggest == null) {
            throw new CustomException(500, "投诉建议不存在");
        }
        return suggest;
    }

    /** 校验当前状态允许流转到目标动作，否则抛业务异常 */
    private void assertCanTransit(HjySuggest suggest, String targetState, String actionName) {
        if (!SuggestState.canTransit(suggest.getComplaintState(), targetState)) {
            throw new CustomException(500, String.format("该投诉当前状态为[%s]，无法%s",
                    suggest.getComplaintState(), actionName));
        }
    }
}
