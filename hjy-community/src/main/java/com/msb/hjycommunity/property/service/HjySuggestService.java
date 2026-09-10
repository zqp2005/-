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

    /** 受理：Pending -> Processing，记录处理人与时间 */
    int acceptSuggest(Long complaintSuggestId);

    /** 回复：Processing -> Replied，记录回复内容 */
    int replySuggest(Long complaintSuggestId, String replyContent);

    /** 关闭：Pending/Replied -> Closed，Pending 直接关闭需填原因 */
    int closeSuggest(Long complaintSuggestId, String reason);
}
