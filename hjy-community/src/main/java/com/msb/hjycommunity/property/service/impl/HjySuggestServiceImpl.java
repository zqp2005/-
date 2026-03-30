package com.msb.hjycommunity.property.service.impl;

import com.msb.hjycommunity.common.utils.SecurityUtils;
import com.msb.hjycommunity.property.domain.HjySuggest;
import com.msb.hjycommunity.property.mapper.HjySuggestMapper;
import com.msb.hjycommunity.property.service.HjySuggestService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 投诉建议Service实现
 */
@Service
public class HjySuggestServiceImpl implements HjySuggestService {

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
        suggest.setCreateBy(SecurityUtils.getUserName());
        return suggestMapper.insertSuggest(suggest);
    }

    @Override
    @Transactional
    public int updateSuggest(HjySuggest suggest) {
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
}
