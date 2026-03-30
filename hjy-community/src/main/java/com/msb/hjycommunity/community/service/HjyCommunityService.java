package com.msb.hjycommunity.community.service;

import com.msb.hjycommunity.community.domain.HjyCommunity;
import com.msb.hjycommunity.community.domain.dto.HjyCommunityDto;
import com.msb.hjycommunity.community.domain.vo.HjyCommunityVo;

import java.util.List;

public interface HjyCommunityService {
    List<HjyCommunityDto> queryList(HjyCommunity hjyCommunity);
    int insertHjyCommunity(HjyCommunity hjyCommunity);

    /**
     * 根据Id获取小区详情
     * @param communityId
     * @return: com.msb.hjycommunity.community.domain.HjyCommunity
     */
    HjyCommunity selectHjyCommunityById(Long communityId);


    /**
     * 修改小区
     * @param hjyCommunity
     * @return: int
     */
    int updateHjyCommunity(HjyCommunity hjyCommunity);


    /**
     * 删除操作
     * @param communityIds
     * @return: int
     */
    int deleteHjyCommunity(Long[] communityIds);


    /**
     * 获取小区下拉列表
     * @param hjyCommunity
     * @return: java.util.List<com.msb.hjycommunity.community.domain.vo.HjyCommunityVo>
     */
   List<HjyCommunityVo> queryPullDown(HjyCommunity hjyCommunity);
}
