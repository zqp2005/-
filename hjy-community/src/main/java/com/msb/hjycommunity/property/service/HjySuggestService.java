package com.msb.hjycommunity.property.service;

import com.msb.hjycommunity.property.domain.HjySuggest;

import java.util.List;

/**
 * 投诉建议Service接口
 */
public interface HjySuggestService {

    /**
     * 查询投诉建议列表
     */
    List<HjySuggest> selectSuggestList(HjySuggest suggest);

    /**
     * 根据ID查询投诉建议
     */
    HjySuggest selectSuggestById(Long complaintSuggestId);

    /**
     * 新增投诉建议
     */
    int insertSuggest(HjySuggest suggest);

    /**
     * 修改投诉建议
     */
    int updateSuggest(HjySuggest suggest);

    /**
     * 删除投诉建议
     */
    int deleteSuggestById(Long complaintSuggestId);

    /**
     * 批量删除投诉建议
     */
    int deleteSuggestByIds(Long[] complaintSuggestIds);
}
