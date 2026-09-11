package com.msb.hjycommunity.web.controller.app;

import com.msb.hjycommunity.common.core.controller.BaseController;
import com.msb.hjycommunity.common.core.domain.BaseResponse;
import com.msb.hjycommunity.common.core.page.PageResult;
import com.msb.hjycommunity.framework.security.filter.AppAuthFilter;
import com.msb.hjycommunity.property.domain.HjySuggest;
import com.msb.hjycommunity.property.domain.dto.AppSuggestRequest;
import com.msb.hjycommunity.property.service.HjySuggestService;
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
 * 业主端（小程序）投诉/建议 Controller
 * <p>
 * 业主姓名/手机号由登录态注入（AppAuthFilter request attribute），不可由请求体伪造；
 * 提交复用 insertSuggest（自动 Pending 状态）。
 *
 * @author hjy
 * @date 2026/9/11
 **/
@RestController
@RequestMapping("/app/suggest")
public class AppSuggestController extends BaseController {

    /** 投诉建议合法类型 */
    private static final String TYPE_COMPLAINT = "Complaint";

    private static final String TYPE_SUGGEST = "Suggest";

    @Resource
    private HjySuggestService suggestService;

    /**
     * 提交投诉/建议
     */
    @PostMapping
    public BaseResponse add(@RequestBody AppSuggestRequest body, HttpServletRequest request) {
        String type = trimToNull(body.getComplaintSuggestType());
        if (!TYPE_COMPLAINT.equals(type) && !TYPE_SUGGEST.equals(type)) {
            return BaseResponse.fail("类型只能为 Complaint 或 Suggest");
        }
        String content = trimToNull(body.getComplaintSuggestContent());
        if (content == null) {
            return BaseResponse.fail("内容不能为空");
        }

        HjySuggest suggest = new HjySuggest();
        suggest.setComplaintSuggestType(type);
        suggest.setComplaintSuggestContent(content);
        suggest.setOwnerRealName((String) request.getAttribute(AppAuthFilter.ATTR_OWNER_NAME));
        suggest.setOwnerPhoneNumber((String) request.getAttribute(AppAuthFilter.ATTR_OWNER_PHONE));

        suggestService.insertSuggest(suggest);

        Map<String, Object> data = new HashMap<>();
        data.put("complaintSuggestId", String.valueOf(suggest.getComplaintSuggestId()));
        return BaseResponse.success(data);
    }

    /**
     * 我的投诉/建议列表（按登录业主姓名+手机号联查隔离）
     */
    @GetMapping("/list")
    public PageResult list(HttpServletRequest request) {
        HjySuggest query = new HjySuggest();
        query.setOwnerRealName((String) request.getAttribute(AppAuthFilter.ATTR_OWNER_NAME));
        query.setOwnerPhoneNumber((String) request.getAttribute(AppAuthFilter.ATTR_OWNER_PHONE));

        startPage();
        List<HjySuggest> list = suggestService.selectSuggestList(query);
        return getData(list);
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
