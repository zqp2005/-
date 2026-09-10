package com.msb.hjycommunity.community.service.impl;

//import com.msb.hjycommunity.common.utils.OrikaUtils;
import com.msb.hjycommunity.common.core.exception.CustomException;
import com.msb.hjycommunity.common.utils.OrikaUtils;
import com.msb.hjycommunity.community.domain.vo.HjyCommunityVo;
import com.msb.hjycommunity.community.service.HjyCommunityService;
import com.msb.hjycommunity.community.domain.HjyCommunity;
import com.msb.hjycommunity.community.domain.dto.HjyCommunityDto;
//import com.msb.hjycommunity.community.domain.vo.HjyCommunityVo;
import com.msb.hjycommunity.community.mapper.HjyCommunityMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author spikeCong
 * @date 2023/3/6
 **/
@Service
public class HjyCommunityServiceImpl implements HjyCommunityService {

    @Resource
//    @Autowired
    private HjyCommunityMapper hjyCommunityMapper;

    private static final String CODE_PREFIX = "COMMUNITY_";

    @Override
    public List<HjyCommunityDto> queryList(HjyCommunity hjyCommunity) {
        return hjyCommunityMapper.queryList(hjyCommunity);
    }


    @Override
    public int insertHjyCommunity(HjyCommunity hjyCommunity) {
        //设置小区编码
        hjyCommunity.setCommunityCode(CODE_PREFIX+System.currentTimeMillis());
        return hjyCommunityMapper.insert(hjyCommunity);
    }

    @Override
    public HjyCommunity selectHjyCommunityById(Long communityId) {
        return hjyCommunityMapper.selectById(communityId);
    }


    @Override
    public int updateHjyCommunity(HjyCommunity hjyCommunity) {
        return hjyCommunityMapper.updateById(hjyCommunity);
    }

    @Override
    @Transactional
    public int deleteHjyCommunity(Long[] communityIds) {
        // 级联删除校验：小区下存在楼栋则整批拒绝删除
        for (Long communityId : communityIds) {
            long buildingCount = hjyCommunityMapper.countBuildingByCommunity(communityId);
            if (buildingCount > 0) {
                HjyCommunity community = hjyCommunityMapper.selectById(communityId);
                throw new CustomException(500, String.format("小区[%s]下存在 %d 栋楼栋，无法删除",
                        community != null ? community.getCommunityName() : communityId, buildingCount));
            }
        }
        return hjyCommunityMapper.deleteBatchIds(Arrays.asList(communityIds));
    }

    @Override
    public List<HjyCommunityVo> queryPullDown(HjyCommunity hjyCommunity) {

        List<HjyCommunityDto> dtoList = hjyCommunityMapper.queryList(hjyCommunity);

        //对象拷贝
        List<HjyCommunityVo> voList = dtoList.stream().map(dto -> {

            //使用orik完成对象拷贝
            HjyCommunityVo communityVo = OrikaUtils.convert(dto, HjyCommunityVo.class);
            return communityVo;
        }).collect(Collectors.toList());

        return voList;
    }


}
