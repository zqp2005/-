package com.msb.hjy.ai.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msb.hjy.ai.client.HjyCommunityClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 公告通知工具类 —— 提供社区公告、通知、活动的查询功能
 * 通过 Spring AI @Tool 注解注册为 AI 可调用的函数
 */
@Slf4j
@Component
public class AnnouncementTool {

    /** 社区后端 HTTP 客户端，用于调用 hjy-community 的 API */
    @Autowired
    private HjyCommunityClient communityClient;

    /** Jackson JSON 解析器 */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 查询社区公告列表，可按类型筛选
     *
     * @param category 公告类型：notice(通知), announcement(公告), activity(活动), news(新闻)
     * @return 格式化后的公告列表文本
     */
    @Tool(description = "查询社区公告列表，可以按类型筛选。用于回答'公告'、'通知'、'社区最新消息'、'有什么通知'等问题")
    public String queryAnnouncements(
            @ToolParam(description = "公告类型：notice(通知), announcement(公告), activity(活动), news(新闻)") String category) {
        log.info("查询公告 - category: {}", category);

        try {
            String result = communityClient.get("/system/notice/list");
            JsonNode root = objectMapper.readTree(result);
            JsonNode data = root.path("rows");

            if (!data.isArray() || data.isEmpty()) {
                return "当前暂无公告通知。";
            }

            StringBuilder sb = new StringBuilder();
            int count = 0;

            for (JsonNode item : data) {
                if (count >= 10) break;

                String noticeType = item.path("noticeType").asText();

                if (category != null && !category.isEmpty() && !noticeType.contains(category)) {
                    continue;
                }

                if (count == 0) {
                    sb.append("【社区公告列表】\n\n");
                }

                sb.append("【公告】\n");
                sb.append("  标题：").append(item.path("noticeTitle").asText()).append("\n");
                sb.append("  类型：").append(formatType(noticeType)).append("\n");
                sb.append("  内容：").append(substring(item.path("noticeContent").asText(), 50)).append("\n");
                sb.append("  发布时间：").append(item.path("createTime").asText()).append("\n");
                sb.append("\n");
                count++;
            }

            if (count == 0) {
                return "未找到符合条件的公告\n";
            }

            sb.append("共查询到 ").append(count).append(" 条公告\n");
            sb.append("\n如需查看公告详情，请告诉我公告标题。");
            return sb.toString();

        } catch (Exception e) {
            log.error("查询公告失败: {}", e.getMessage());
            return "查询公告失败，请稍后重试。";
        }
    }

    /**
     * 根据标题获取公告详情
     *
     * @param title 公告标题（支持模糊匹配）
     * @return 公告详情文本，包含标题、类型、内容和发布时间
     */
    @Tool(description = "获取公告详情。用于回答查看具体公告内容、'公告详情'、'查看xx通知'等问题")
    public String getAnnouncementDetail(
            @ToolParam(description = "公告标题") String title) {
        log.info("查询公告详情 - title: {}", title);

        if (title == null || title.isEmpty()) {
            return "请提供公告标题。";
        }

        try {
            String result = communityClient.get("/system/notice/list");
            JsonNode root = objectMapper.readTree(result);
            JsonNode data = root.path("rows");

            if (!data.isArray() || data.isEmpty()) {
                return "当前暂无公告通知。";
            }

            for (JsonNode item : data) {
                String noticeTitle = item.path("noticeTitle").asText();
                if (noticeTitle.contains(title)) {
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

        } catch (Exception e) {
            log.error("查询公告详情失败: {}", e.getMessage());
            return "查询公告详情失败，请稍后重试。";
        }
    }

    /**
     * 查询社区活动列表
     *
     * @param status 活动状态：upcoming(即将开始), ongoing(进行中), ended(已结束)
     * @return 格式化后的活动列表文本
     */
    @Tool(description = "查询社区活动。用于回答'有什么活动'、'社区活动'、'有什么有趣的活动'等问题")
    public String getActivities(
            @ToolParam(description = "活动状态：upcoming(即将开始), ongoing(进行中), ended(已结束)") String status) {
        log.info("查询社区活动 - status: {}", status);

        try {
            String result = communityClient.get("/system/notice/list");
            JsonNode root = objectMapper.readTree(result);
            JsonNode data = root.path("rows");

            if (!data.isArray() || data.isEmpty()) {
                return getDefaultActivities();
            }

            StringBuilder sb = new StringBuilder();
            int count = 0;

            for (JsonNode item : data) {
                if (count >= 5) break;

                String noticeType = item.path("noticeType").asText();
                if ("activity".equalsIgnoreCase(noticeType) || "活动".equals(noticeType)) {
                    if (count == 0) {
                        sb.append("【社区活动列表】\n\n");
                    }

                    sb.append("【活动】\n");
                    sb.append("  标题：").append(item.path("noticeTitle").asText()).append("\n");
                    sb.append("  内容：").append(substring(item.path("noticeContent").asText(), 50)).append("\n");
                    sb.append("  发布时间：").append(item.path("createTime").asText()).append("\n");
                    sb.append("\n");
                    count++;
                }
            }

            if (count == 0) {
                return getDefaultActivities();
            }

            return sb.toString();

        } catch (Exception e) {
            log.error("查询活动失败: {}", e.getMessage());
            return getDefaultActivities();
        }
    }

    /**
     * 获取默认活动信息（当接口不可用时降级返回）
     */
    private String getDefaultActivities() {
        return """
                【社区活动列表】

                您好！社区活动查询功能正在建设中。

                我们将在近期推出丰富多彩的社区活动，包括：
                - 健康讲座
                - 亲子活动
                - 节日庆典
                - 兴趣班课程

                敬请期待！如需了解最新活动信息，请关注社区公告。
                """;
    }

    /**
     * 将公告类型的英文标识转为中文显示
     *
     * @param type 英文类型标识
     * @return 中文类型名称
     */
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

    /**
     * 截断字符串至指定长度并追加省略号
     *
     * @param str       原始字符串
     * @param maxLength 最大长度
     * @return 截断后的字符串
     */
    private String substring(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...";
    }
}