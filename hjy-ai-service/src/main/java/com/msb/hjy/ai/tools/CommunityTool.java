package com.msb.hjy.ai.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msb.hjy.ai.client.HjyCommunityClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Map;

/** 只展示主后端真实字段，不用示例数据填补业务空白。 */
@Component
public class CommunityTool {
    @Autowired private HjyCommunityClient communityClient;
    private final ObjectMapper mapper = new ObjectMapper();

    @Tool(description = "查询权限范围内的小区名称和详细地址；未配置的物业电话等不作推断")
    public String queryCommunityInfo(@ToolParam(description = "basic、property 或 contact") String infoType) {
        try {
            JsonNode root = mapper.readTree(communityClient.get("/community/list", Map.of("pageNum", 1, "pageSize", 10)));
            JsonNode rows = root.path("rows");
            if (!rows.isArray()) return "社区查询响应格式异常，请稍后重试。";
            if (rows.isEmpty()) return "当前查询范围内没有社区记录。";
            StringBuilder out = new StringBuilder("【社区资料，本页结果】\n");
            for (JsonNode row : rows) {
                out.append("小区：").append(row.path("communityName").asText("未登记"))
                   .append("；地址：").append(row.path("communityDetailedAddress").asText("未登记")).append("\n");
            }
            return out.append("总记录数：").append(root.path("total").asText("未知"))
                    .append("。户数、配套、物业电话等未接入，不能据此推断。").toString();
        } catch (Exception e) { return "社区查询失败或无权访问，不能据此判断没有社区。"; }
    }

    @Tool(description = "说明社区设施数据接入状态")
    public String getFacilities() { return "尚未接入可核验的设施台账，无法确认设施、开放时间或预约规则，请以物业公告为准。"; }

    @Tool(description = "说明周边配套数据接入状态")
    public String getNearbyFacilities(@ToolParam(description = "配套类型") String type) {
        return "尚未接入当前小区的周边配套数据，无法确认学校、医院、交通距离等信息。";
    }

    @Tool(description = "说明门禁办理能力边界")
    public String getAccessCardInfo(@ToolParam(description = "业主姓名，不用于确认身份") String ownerName) {
        return "尚未接入门禁系统或经核验的办理规则，无法确认权限、办理费用和服务时间，请联系实际物业服务人员。";
    }

    @Tool(description = "说明便民服务数据接入状态")
    public String getConvenientServices(@ToolParam(description = "服务类型") String serviceType) {
        return "尚未维护经核验的便民服务目录，不提供虚构商家、电话或价格。报修可通过业务页面提交。";
    }
}
