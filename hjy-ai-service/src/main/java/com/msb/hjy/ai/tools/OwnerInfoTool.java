package com.msb.hjy.ai.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msb.hjy.ai.client.HjyCommunityClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class OwnerInfoTool {
    @Autowired private HjyCommunityClient communityClient;
    private final ObjectMapper mapper = new ObjectMapper();

    @Tool(description = "管理人员按条件查询授权范围内业主档案；姓名手机号只是检索条件，不是本人身份证明")
    public String queryOwnerInfo(@ToolParam(description = "业主姓名") String ownerName,
                                 @ToolParam(description = "联系电话") String phone) {
        if ((ownerName == null || ownerName.isBlank()) && (phone == null || phone.isBlank())) {
            return "请提供档案检索条件；管理账号不等于居民账号，不能推断哪条档案是本人。";
        }
        Map<String, Object> params = new HashMap<>(Map.of("pageNum", 1, "pageSize", 5));
        if (ownerName != null && !ownerName.isBlank()) params.put("ownerRealName", ownerName);
        if (phone != null && !phone.isBlank()) params.put("ownerPhoneNumber", phone);
        try {
            JsonNode root = mapper.readTree(communityClient.get("/system/owner/list", params));
            JsonNode rows = root.path("rows");
            if (!rows.isArray()) return "业主查询响应格式异常。";
            if (rows.isEmpty()) return "当前查询条件下没有业主记录。";
            StringBuilder out = new StringBuilder("【业主档案，本页结果；不代表已确认本人身份】\n");
            for (JsonNode row : rows) {
                String contact = row.path("ownerPhoneNumber").asText("");
                String masked = contact.matches("\\d{11}") ? contact.substring(0, 3) + "****" + contact.substring(7) : "未展示";
                out.append("编号：").append(row.path("ownerId").asText()).append("；姓名：")
                   .append(row.path("ownerRealName").asText()).append("；电话：").append(masked)
                   .append("；状态：").append(row.path("ownerStatus").asText("未知"))
                   .append("；关联房屋：").append(row.path("roomName").asText("未提供")).append("\n");
            }
            return out.append("符合查询条件总数：").append(root.path("total").asText("未知")).toString();
        } catch (Exception e) { return "业主查询失败或无权访问，不能据此判断没有档案。"; }
    }

    @Tool(description = "说明车辆台账接入状态")
    public String getOwnerVehicles(@ToolParam(description = "业主姓名") String ownerName) {
        return "尚未接入车辆台账，不能用业主档案缺少车辆字段推断没有车辆。";
    }
    @Tool(description = "说明家庭关系接入状态")
    public String getFamilyMembers(@ToolParam(description = "业主姓名") String ownerName) {
        return "尚未接入可核验的家庭关系，房屋共同关联不等于家庭成员，不提供推测名单。";
    }
    @Tool(description = "说明按居民查询访客记录的能力边界")
    public String queryVisitors(@ToolParam(description = "业主姓名") String ownerName,
                               @ToolParam(description = "日期") String date) {
        return "访客记录尚无可靠的被访居民关联及离场核验，不能按姓名推断谁来访或是否离开，请在授权的访客管理页面核验。";
    }
    @Tool(description = "说明访客登记入口")
    public String registerVisitor(@ToolParam(description = "访客姓名") String visitorName,
                                  @ToolParam(description = "访客电话") String visitorPhone,
                                  @ToolParam(description = "被访人姓名") String ownerName,
                                  @ToolParam(description = "事由") String reason) {
        return "AI 访客登记尚未开放，请在业务页面核对资料并提交。";
    }
}
