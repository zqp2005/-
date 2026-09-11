package com.msb.hjycommunity.web.controller.app;

import com.msb.hjycommunity.common.core.controller.BaseController;
import com.msb.hjycommunity.common.core.domain.BaseResponse;
import com.msb.hjycommunity.common.core.page.PageResult;
import com.msb.hjycommunity.framework.security.filter.AppAuthFilter;
import com.msb.hjycommunity.property.domain.HjyRepair;
import com.msb.hjycommunity.property.domain.dto.AppRepairRequest;
import com.msb.hjycommunity.property.service.HjyRepairService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 业主端（小程序）报修 Controller
 * <p>
 * 业主姓名/手机号由登录态注入（AppAuthFilter request attribute），不可由请求体伪造；
 * 提交复用 insertRepair（自动 Pending 状态 + RX 工单号）。
 *
 * @author hjy
 * @date 2026/9/11
 **/
@RestController
@RequestMapping("/app/repair")
public class AppRepairController extends BaseController {

    @Resource
    private HjyRepairService repairService;

    /**
     * 提交报修
     */
    @PostMapping
    public BaseResponse add(@RequestBody AppRepairRequest body, HttpServletRequest request) {
        String repairContent = trimToNull(body.getRepairContent());
        if (repairContent == null) {
            return BaseResponse.fail("报修内容不能为空");
        }

        HjyRepair repair = new HjyRepair();
        repair.setRepairContent(repairContent);
        repair.setAddress(trimToNull(body.getAddress()));
        repair.setCommunityId(body.getCommunityId());
        repair.setOwnerRealName((String) request.getAttribute(AppAuthFilter.ATTR_OWNER_NAME));
        repair.setOwnerPhoneNumber((String) request.getAttribute(AppAuthFilter.ATTR_OWNER_PHONE));

        repairService.insertRepair(repair);

        // insertRepair 已生成工单号并经 useGeneratedKeys 回填 repairId
        Map<String, Object> data = new HashMap<>();
        data.put("repairId", String.valueOf(repair.getRepairId()));
        data.put("repairNum", repair.getRepairNum());
        return BaseResponse.success(data);
    }

    /**
     * 我的报修列表（按登录业主姓名+手机号联查隔离）
     */
    @GetMapping("/list")
    public PageResult list(HttpServletRequest request) {
        HjyRepair query = new HjyRepair();
        query.setOwnerRealName((String) request.getAttribute(AppAuthFilter.ATTR_OWNER_NAME));
        query.setOwnerPhoneNumber((String) request.getAttribute(AppAuthFilter.ATTR_OWNER_PHONE));

        startPage();
        List<HjyRepair> list = repairService.selectRepairList(query);
        return getData(list);
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
