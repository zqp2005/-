package com.msb.hjycommunity.web.controller.property;

import com.msb.hjycommunity.common.core.controller.BaseController;
import com.msb.hjycommunity.common.core.domain.BaseResponse;
import com.msb.hjycommunity.property.mapper.HjyOwnerMapper;
import com.msb.hjycommunity.property.mapper.HjyRepairMapper;
import com.msb.hjycommunity.property.mapper.HjySuggestMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 首页统计 Controller
 * 聚合首页仪表盘卡片数据：业主总数 / 投诉建议总数 / 报修总数。
 * 注："空闲车位"暂无对应业务模块，不提供统计（前端保持模板默认展示）。
 *
 * @author hjy
 * @date 2026/9/11
 **/
@RestController
@RequestMapping("/system/stats")
public class HjyStatsController extends BaseController {

    @Resource
    private HjyOwnerMapper ownerMapper;

    @Resource
    private HjySuggestMapper suggestMapper;

    @Resource
    private HjyRepairMapper repairMapper;

    /**
     * 首页统计（登录即可访问，不做细粒度权限）
     */
    @GetMapping("/home")
    public BaseResponse home() {
        Map<String, Object> data = new HashMap<>();
        data.put("ownerTotal", ownerMapper.selectCount(null));
        data.put("suggestTotal", suggestMapper.selectCount(null));
        data.put("repairTotal", repairMapper.selectCount(null));
        return BaseResponse.success(data);
    }
}
