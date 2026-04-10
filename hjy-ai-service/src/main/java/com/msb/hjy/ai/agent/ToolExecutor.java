package com.msb.hjy.ai.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.msb.hjy.ai.client.HjyCommunityClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class ToolExecutor {

    @Autowired
    private HjyCommunityClient communityClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String execute(String toolName, Map<String, Object> arguments) {
        log.info("执行工具: {} - 参数: {}", toolName, arguments);

        try {
            return switch (toolName) {
                case "query_repair_orders" -> queryRepairOrders(arguments);
                case "create_complaint" -> createComplaint(arguments);
                case "query_property_fee" -> queryPropertyFee(arguments);
                case "query_announcements" -> queryAnnouncements(arguments);
                case "query_owner_info" -> queryOwnerInfo(arguments);
                default -> "未知工具: " + toolName;
            };
        } catch (Exception e) {
            log.error("工具执行失败: {} - {}", toolName, e.getMessage());
            return "工具执行失败: " + e.getMessage();
        }
    }

    private String queryRepairOrders(Map<String, Object> args) throws Exception {
        String status = (String) args.get("status");
        String ownerName = (String) args.get("owner_name");

        String result = communityClient.get("/system/repair/list");
        JsonNode root = objectMapper.readTree(result);
        JsonNode data = root.path("data");

        if (!data.isArray() || data.isEmpty()) {
            return "当前暂无报修工单记录。";
        }

        StringBuilder sb = new StringBuilder();
        int count = 0;

        for (JsonNode item : data) {
            if (count >= 10) break;

            String itemStatus = item.path("repairState").asText();
            String itemOwnerName = item.path("ownerName").asText("未知");

            if (status != null && !status.isEmpty() && !itemStatus.equalsIgnoreCase(status)) {
                continue;
            }
            if (ownerName != null && !ownerName.isEmpty() &&
                    !itemOwnerName.contains(ownerName)) {
                continue;
            }

            if (count == 0) {
                sb.append("【报修工单查询结果】\n\n");
            }

            sb.append("工单号：").append(item.path("repairId").asText()).append("\n");
            sb.append("业主：").append(itemOwnerName).append("\n");
            sb.append("位置：").append(item.path("repairLocation").asText()).append("\n");
            sb.append("问题：").append(item.path("repairDescription").asText()).append("\n");
            sb.append("状态：").append(formatRepairState(itemStatus)).append("\n");
            sb.append("时间：").append(item.path("createTime").asText()).append("\n");
            sb.append("\n");
            count++;
        }

        if (count == 0) {
            return "未找到符合条件的报修工单。";
        }

        sb.append("共查询到 ").append(count).append(" 条报修记录。");
        return sb.toString();
    }

    private String createComplaint(Map<String, Object> args) throws Exception {
        String ownerName = (String) args.get("owner_name");
        String phone = (String) args.get("phone");
        String content = (String) args.get("content");
        String category = (String) args.get("category");

        if (ownerName == null || content == null) {
            return "请提供业主姓名和投诉内容。";
        }

        Map<String, Object> body = new java.util.HashMap<>();
        body.put("ownerName", ownerName);
        if (phone != null) body.put("ownerPhone", phone);
        body.put("suggestContent", content);
        if (category != null) body.put("suggestType", category);

        String result = communityClient.post("/system/suggest", body);
        JsonNode root = objectMapper.readTree(result);

        if (root.path("code").asInt() == 200) {
            return String.format("""
                    投诉建议提交成功！

                    【提交信息】
                    业主：%s
                    联系方式：%s
                    内容：%s

                    我们将尽快处理您的反馈，感谢您的宝贵意见！
                    """,
                    ownerName,
                    phone != null ? phone : "未提供",
                    content);
        } else {
            return "投诉提交失败：" + root.path("msg").asText();
        }
    }

    private String queryPropertyFee(Map<String, Object> args) throws Exception {
        String result = communityClient.get("/system/propertyFee/list");
        JsonNode root = objectMapper.readTree(result);
        JsonNode data = root.path("data");

        if (!data.isArray() || data.isEmpty()) {
            return "当前暂无物业费账单记录。";
        }

        StringBuilder sb = new StringBuilder();
        int count = 0;

        for (JsonNode item : data) {
            if (count >= 10) break;

            String feeStatus = item.path("payState").asText();

            if (count == 0) {
                sb.append("【物业费账单查询结果】\n\n");
            }

            sb.append("账单号：").append(item.path("feeId").asText()).append("\n");
            sb.append("房号：").append(item.path("buildingName").asText())
              .append(item.path("roomName").asText()).append("\n");
            sb.append("金额：").append(item.path("propertyAmount").asText()).append(" 元\n");
            sb.append("状态：").append(formatFeeState(feeStatus)).append("\n");
            sb.append("缴费期限：").append(item.path("deadline").asText()).append("\n");
            sb.append("\n");
            count++;
        }

        if (count == 0) {
            return "未找到物业费账单记录。";
        }

        sb.append("共查询到 ").append(count).append(" 条账单记录。");
        return sb.toString();
    }

    private String queryAnnouncements(Map<String, Object> args) throws Exception {
        String title = (String) args.get("title");

        String result = communityClient.get("/system/notice/list");
        JsonNode root = objectMapper.readTree(result);
        JsonNode data = root.path("data");

        if (!data.isArray() || data.isEmpty()) {
            return "当前暂无公告通知。";
        }

        StringBuilder sb = new StringBuilder();
        int count = 0;

        for (JsonNode item : data) {
            if (count >= 10) break;

            String itemTitle = item.path("noticeTitle").asText();
            if (title != null && !title.isEmpty() &&
                    !itemTitle.contains(title)) {
                continue;
            }

            if (count == 0) {
                sb.append("【社区公告】\n\n");
            }

            sb.append("📢 ").append(itemTitle).append("\n");
            sb.append("类型：").append(item.path("noticeType").asText()).append("\n");
            sb.append("时间：").append(item.path("createTime").asText()).append("\n");
            String content = item.path("noticeContent").asText();
            if (!content.isEmpty()) {
                sb.append("内容：").append(content.length() > 100 ?
                        content.substring(0, 100) + "..." : content).append("\n");
            }
            sb.append("\n");
            count++;
        }

        if (count == 0) {
            return "未找到相关公告。";
        }

        return sb.toString();
    }

    private String queryOwnerInfo(Map<String, Object> args) throws Exception {
        String result = communityClient.get("/system/owner/list");
        JsonNode root = objectMapper.readTree(result);
        JsonNode data = root.path("data");

        if (!data.isArray() || data.isEmpty()) {
            return "当前暂无业主信息记录。";
        }

        String ownerName = (String) args.get("owner_name");
        String phone = (String) args.get("phone");

        StringBuilder sb = new StringBuilder();

        for (JsonNode item : data) {
            String itemName = item.path("ownerName").asText();
            String itemPhone = item.path("ownerPhone").asText();

            if (ownerName != null && !ownerName.isEmpty() &&
                    !itemName.contains(ownerName)) {
                continue;
            }
            if (phone != null && !phone.isEmpty() &&
                    !itemPhone.contains(phone)) {
                continue;
            }

            if (sb.length() == 0) {
                sb.append("【业主信息查询结果】\n\n");
            }

            sb.append("姓名：").append(itemName).append("\n");
            sb.append("电话：").append(itemPhone).append("\n");
            sb.append("房号：").append(item.path("buildingName").asText())
              .append(item.path("roomName").asText()).append("\n");
            sb.append("入住时间：").append(item.path("checkInTime").asText()).append("\n");
            sb.append("\n");
            break;
        }

        if (sb.length() == 0) {
            return "未找到符合条件的业主信息。";
        }

        return sb.toString();
    }

    private String formatRepairState(String state) {
        if (state == null) return "未知";
        return switch (state.toLowerCase()) {
            case "pending" -> "待处理";
            case "allocated" -> "已派单";
            case "processing" -> "处理中";
            case "processed" -> "已处理";
            case "completed" -> "已完成";
            case "rated" -> "已评价";
            default -> state;
        };
    }

    private String formatFeeState(String state) {
        if (state == null) return "未知";
        return switch (state.toLowerCase()) {
            case "unpaid" -> "未支付";
            case "paid" -> "已支付";
            case "overdue" -> "已逾期";
            default -> state;
        };
    }
}
