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
public class AnnouncementTool {
    @Autowired private HjyCommunityClient communityClient;
    private final ObjectMapper mapper = new ObjectMapper();

    @Tool(description = "查询已发布的通知或公告，结果明确分页范围")
    public String queryAnnouncements(@ToolParam(description = "notice=通知，announcement=公告，留空查询全部") String category) {
        Map<String, Object> params = new HashMap<>(Map.of("pageNum", 1, "pageSize", 10, "status", "0"));
        if (category != null && !category.isBlank()) {
            String type = switch (category.toLowerCase()) {
                case "notice", "1" -> "1";
                case "announcement", "2" -> "2";
                default -> null;
            };
            if (type == null) return "目前公告仅支持通知和公告分类，活动与新闻分类未接入。";
            params.put("noticeType", type);
        }
        return query(params, false);
    }

    @Tool(description = "按标题搜索已发布公告；标题不唯一时列出匹配记录，不猜测目标")
    public String getAnnouncementDetail(@ToolParam(description = "公告标题") String title) {
        if (title == null || title.isBlank()) return "请提供公告标题。";
        return query(Map.of("pageNum", 1, "pageSize", 10, "status", "0", "noticeTitle", title), true);
    }

    @Tool(description = "说明社区活动日程接入状态")
    public String getActivities(@ToolParam(description = "活动状态") String status) {
        return "尚未接入独立活动日程，不能判断即将开始、进行中或已结束；可查询已发布公告中的活动说明。";
    }

    private String query(Map<String, Object> params, boolean full) {
        try {
            JsonNode root = mapper.readTree(communityClient.get("/system/notice/list", params));
            JsonNode rows = root.path("rows");
            if (!rows.isArray()) return "公告响应格式异常，请稍后重试。";
            if (rows.isEmpty()) return "当前查询条件下没有已发布公告。";
            StringBuilder out = new StringBuilder("【已发布公告，本页结果】\n");
            for (JsonNode row : rows) {
                if (!"0".equals(row.path("status").asText())) continue;
                String content = row.path("noticeContent").asText();
                out.append("编号：").append(row.path("noticeId").asText()).append("；标题：")
                   .append(row.path("noticeTitle").asText()).append("\n类型：")
                   .append("1".equals(row.path("noticeType").asText()) ? "通知" : "公告")
                   .append("；发布时间：").append(row.path("createTime").asText("未登记"))
                   .append("\n内容：").append(full || content.length() <= 100 ? content : content.substring(0, 100) + "…").append("\n");
            }
            return out.append("符合查询条件总数：").append(root.path("total").asText("未知")).toString();
        } catch (Exception e) { return "公告查询失败或无权访问，不能据此判断没有公告。"; }
    }
}
