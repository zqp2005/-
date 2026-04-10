package com.msb.hjy.ai.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msb.hjy.ai.client.HjyCommunityClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AnnouncementTool {

    @Autowired
    private HjyCommunityClient communityClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String queryAnnouncements(String category) {
        log.info("查询公告 - category: {}", category);

        try {
            String result = communityClient.get("/system/notice/list");
            JsonNode root = objectMapper.readTree(result);
            JsonNode data = root.path("data");

            if (data.isArray() && data.size() > 0) {
                StringBuilder sb = new StringBuilder("【社区公告列表】\n\n");

                int count = 0;
                for (JsonNode item : data) {
                    if (count >= 10) break;

                    String noticeType = item.path("noticeType").asText();

                    if (category != null && !category.isEmpty() &&
                            !noticeType.contains(category)) {
                        continue;
                    }

                    sb.append("【公告】\n");
                    sb.append("  标题：").append(item.path("noticeTitle").asText()).append("\n");
                    sb.append("  类型：").append(formatType(noticeType)).append("\n");
                    sb.append("  内容：").append(item.path("noticeContent").asText()).append("\n");
                    sb.append("  发布时间：").append(item.path("createTime").asText()).append("\n");
                    sb.append("  发布人：").append(item.path("createBy").asText()).append("\n");
                    sb.append("\n");
                    count++;
                }

                if (count == 0) {
                    sb.append("未找到符合条件的公告\n");
                } else {
                    sb.append("共查询到 ").append(count).append(" 条公告\n");
                }

                sb.append("\n如需查看公告详情，请告诉我公告标题。");
                return sb.toString();
            } else {
                return "当前暂无公告通知。";
            }

        } catch (Exception e) {
            log.error("查询公告失败: {}", e.getMessage());
            return "查询公告失败，请稍后重试。错误信息: " + e.getMessage();
        }
    }

    public String getAnnouncementDetail(String title) {
        log.info("查询公告详情 - title: {}", title);

        try {
            String result = communityClient.get("/system/notice/list");
            JsonNode root = objectMapper.readTree(result);
            JsonNode data = root.path("data");

            if (data.isArray() && data.size() > 0) {
                for (JsonNode item : data) {
                    String noticeTitle = item.path("noticeTitle").asText();
                    if (title != null && !title.isEmpty() &&
                            noticeTitle.contains(title)) {

                        StringBuilder sb = new StringBuilder();
                        sb.append("【公告详情】\n\n");
                        sb.append("  标题：").append(noticeTitle).append("\n");
                        sb.append("  类型：").append(formatType(item.path("noticeType").asText())).append("\n");
                        sb.append("  内容：\n").append(item.path("noticeContent").asText()).append("\n\n");
                        sb.append("  发布时间：").append(item.path("createTime").asText()).append("\n");
                        sb.append("  发布人：").append(item.path("createBy").asText()).append("\n");

                        return sb.toString();
                    }
                }
                return "未找到该公告，请检查标题是否正确。";
            } else {
                return "当前暂无公告通知。";
            }

        } catch (Exception e) {
            log.error("查询公告详情失败: {}", e.getMessage());
            return "查询公告详情失败，请稍后重试。";
        }
    }

    public String getActivities(String status) {
        log.info("查询社区活动 - status: {}", status);

        try {
            String result = communityClient.get("/system/notice/list");
            JsonNode root = objectMapper.readTree(result);
            JsonNode data = root.path("data");

            if (data.isArray() && data.size() > 0) {
                StringBuilder sb = new StringBuilder("【社区活动列表】\n\n");

                int count = 0;
                for (JsonNode item : data) {
                    if (count >= 5) break;

                    String noticeType = item.path("noticeType").asText();
                    if ("activity".equalsIgnoreCase(noticeType) || "活动".equals(noticeType)) {
                        sb.append("【活动】\n");
                        sb.append("  标题：").append(item.path("noticeTitle").asText()).append("\n");
                        sb.append("  内容：").append(item.path("noticeContent").asText()).append("\n");
                        sb.append("  发布时间：").append(item.path("createTime").asText()).append("\n");
                        sb.append("\n");
                        count++;
                    }
                }

                if (count == 0) {
                    sb.append("当前暂无社区活动通知\n");
                    sb.append("\n【其他通知】\n");
                    int otherCount = 0;
                    for (JsonNode item : data) {
                        if (otherCount >= 3) break;
                        sb.append("  - ").append(item.path("noticeTitle").asText()).append("\n");
                        otherCount++;
                    }
                }

                return sb.toString();
            } else {
                return "当前暂无社区活动通知。\n" +
                       "敬请期待即将到来的社区活动！";
            }

        } catch (Exception e) {
            log.error("查询活动失败: {}", e.getMessage());
            return "查询社区活动失败，请稍后重试。";
        }
    }

    private String formatType(String type) {
        if (type == null) return "未知";
        return switch (type.toLowerCase()) {
            case "notice" -> "通知";
            case "announcement" -> "公告";
            case "activity" -> "活动";
            case "news" -> "新闻";
            default -> type;
        };
    }
}
