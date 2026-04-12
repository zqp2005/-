package com.msb.hjy.ai.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msb.hjy.ai.client.HjyCommunityClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class RepairTool {

    @Autowired
    private HjyCommunityClient communityClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Tool(description = "查询报修工单列表，可以按状态或业主名筛选。用于回答'报修进度'、'我的报修'、'有哪些报修'等问题")
    public String queryRepairOrders(
            @ToolParam(description = "报修状态筛选：pending(待处理), allocated(已派单), processing(处理中), completed(已完成), rated(已评价)") String status,
            @ToolParam(description = "业主姓名，用于筛选特定业主的报修单") String ownerName) {
        log.info("查询报修工单 - status: {}, ownerName: {}", status, ownerName);

        try {
            String result = communityClient.get("/system/repair/list");
            JsonNode root = objectMapper.readTree(result);
            JsonNode data = root.path("rows");

            if (!data.isArray() || data.isEmpty()) {
                return "当前暂无报修工单记录。";
            }

            StringBuilder sb = new StringBuilder();
            int count = 0;

            for (JsonNode item : data) {
                if (count >= 5) break;

                String repairState = item.path("repairState").asText();
                String itemOwnerName = item.path("ownerRealName").asText("未知");

                if (status != null && !status.isEmpty() && !repairState.equalsIgnoreCase(status)) {
                    continue;
                }
                if (ownerName != null && !ownerName.isEmpty() && !itemOwnerName.contains(ownerName)) {
                    continue;
                }

                if (count == 0) {
                    sb.append("【报修工单查询结果】\n\n");
                }

                sb.append("【报修单】\n");
                sb.append("  工单号：").append(item.path("repairId").asText()).append("\n");
                sb.append("  业主：").append(itemOwnerName).append("\n");
                sb.append("  问题：").append(substring(item.path("repairContent").asText(), 30)).append("\n");
                sb.append("  状态：").append(formatState(repairState)).append("\n");
                sb.append("  提交时间：").append(item.path("createTime").asText()).append("\n");
                sb.append("\n");
                count++;
            }

            if (count == 0) {
                return "未找到符合条件的报修工单\n";
            }

            sb.append("共查询到 ").append(count).append(" 条报修记录\n");
            sb.append("\n如需了解更多信息，请告诉我具体工单号。");
            return sb.toString();

        } catch (Exception e) {
            log.error("查询报修工单失败: {}", e.getMessage());
            return "查询报修工单失败，请稍后重试。错误信息: " + e.getMessage();
        }
    }

    @Tool(description = "创建新的报修工单。用于回答'我要报修'、'提交报修'、'报修水管'等问题，需要业主姓名、联系电话、报修位置和问题描述")
    public String createRepairOrder(
            @ToolParam(description = "业主姓名，必填") String ownerName,
            @ToolParam(description = "联系电话") String phone,
            @ToolParam(description = "报修位置，如：A栋101") String location,
            @ToolParam(description = "问题描述，必填") String problem,
            @ToolParam(description = "报修类别：water(水电), facility(设施), door(门锁), other(其他)") String category) {
        log.info("创建报修工单 - ownerName: {}, problem: {}", ownerName, problem);

        if (ownerName == null || ownerName.isEmpty()) {
            return "请提供业主姓名。";
        }
        if (problem == null || problem.isEmpty()) {
            return "请描述需要报修的问题。";
        }

        try {
            Map<String, Object> body = new HashMap<>();
            body.put("ownerRealName", ownerName);
            body.put("ownerPhoneNumber", phone != null ? phone : "");
            body.put("address", location != null ? location : "");
            body.put("repairContent", problem);
            body.put("repairCategory", category != null ? category : "other");

            String result = communityClient.post("/system/repair", body);
            JsonNode root = objectMapper.readTree(result);

            if (root.path("code").asInt() == 200) {
                return String.format("""
                        报修工单创建成功！

                        【工单信息】
                        业主：%s
                        联系电话：%s
                        报修位置：%s
                        问题描述：%s
                        报修类别：%s

                        我们将尽快安排维修人员与您联系，请保持电话畅通。
                        """, ownerName, phone != null ? phone : "未留", 
                        location != null ? location : "未指定",
                        problem, category != null ? category : "其他");
            } else {
                return "报修工单创建失败：" + root.path("msg").asText();
            }

        } catch (Exception e) {
            log.error("创建报修工单失败: {}", e.getMessage());
            return "创建报修工单失败，请稍后重试。";
        }
    }

    @Tool(description = "获取报修工单详情。用于回答'报修详情'、'工单号xxx'等问题")
    public String getRepairDetail(
            @ToolParam(description = "报修工单号") String repairId) {
        log.info("查询报修详情 - repairId: {}", repairId);

        if (repairId == null || repairId.isEmpty()) {
            return "请提供报修工单号。";
        }

        try {
            String result = communityClient.get("/system/repair/" + repairId);
            JsonNode root = objectMapper.readTree(result);
            JsonNode data = root.path("data");

            if (data.isEmpty() || data.isNull()) {
                return "未找到该报修工单，请检查工单号是否正确。";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("【报修单详情】\n");
            sb.append("  工单号：").append(data.path("repairId").asText()).append("\n");
            sb.append("  业主：").append(data.path("ownerRealName").asText()).append("\n");
            sb.append("  联系电话：").append(data.path("ownerPhoneNumber").asText()).append("\n");
            sb.append("  报修位置：").append(data.path("address").asText()).append("\n");
            sb.append("  问题描述：").append(data.path("repairContent").asText()).append("\n");
            sb.append("  报修类别：").append(data.path("repairCategory").asText()).append("\n");
            sb.append("  状态：").append(formatState(data.path("repairState").asText())).append("\n");
            sb.append("  提交时间：").append(data.path("createTime").asText()).append("\n");

            return sb.toString();

        } catch (Exception e) {
            log.error("查询报修详情失败: {}", e.getMessage());
            return "查询报修详情失败，请稍后重试。";
        }
    }

    @Tool(description = "取消报修工单。用于回答'取消报修'、'撤销工单'等问题")
    public String cancelRepairOrder(
            @ToolParam(description = "报修工单号") String repairId,
            @ToolParam(description = "取消原因") String reason) {
        log.info("取消报修工单 - repairId: {}, reason: {}", repairId, reason);

        if (repairId == null || repairId.isEmpty()) {
            return "请提供报修工单号。";
        }

        try {
            Map<String, Object> body = new HashMap<>();
            body.put("repairId", repairId);
            body.put("repairState", "Cancelled");
            body.put("cancelReason", reason != null ? reason : "用户取消");

            String result = communityClient.put("/system/repair", body);
            JsonNode root = objectMapper.readTree(result);

            if (root.path("code").asInt() == 200) {
                return String.format("""
                        报修工单已取消

                        【工单信息】
                        工单号：%s
                        取消原因：%s

                        如需重新报修，请再次提交。
                        """, repairId, reason != null ? reason : "用户取消");
            } else {
                return "取消报修工单失败：" + root.path("msg").asText();
            }

        } catch (Exception e) {
            log.error("取消报修工单失败: {}", e.getMessage());
            return "取消报修工单失败，请稍后重试。";
        }
    }

    @Tool(description = "评价报修服务。用于回答'评价报修'、'服务满意'等问题")
    public String rateRepair(
            @ToolParam(description = "报修工单号") String repairId,
            @ToolParam(description = "评分：1-5分") Integer rating,
            @ToolParam(description = "评价内容") String comment) {
        log.info("评价报修 - repairId: {}, rating: {}", repairId, rating);

        if (repairId == null || repairId.isEmpty()) {
            return "请提供报修工单号。";
        }
        if (rating == null || rating < 1 || rating > 5) {
            return "请提供有效评分（1-5分）。";
        }

        String ratingText = switch (rating) {
            case 1 -> "非常不满意";
            case 2 -> "不满意";
            case 3 -> "一般";
            case 4 -> "满意";
            case 5 -> "非常满意";
            default -> "未评价";
        };

        return String.format("""
                【报修评价成功】

                【评价信息】
                工单号：%s
                评分：%d分（%s）
                评价内容：%s

                感谢您的宝贵意见！
                我们将��续��进服务质量。
                """, repairId, rating, ratingText, 
                comment != null ? comment : "未填写");
    }

    private String formatState(String state) {
        if (state == null) return "未知";
        return switch (state.toLowerCase()) {
            case "pending" -> "待处理";
            case "allocated" -> "已派单";
            case "processing" -> "处理中";
            case "processed" -> "已处理";
            case "completed" -> "已完成";
            case "rated" -> "已评价";
            case "cancelled" -> "已取消";
            default -> state;
        };
    }

    private String substring(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...";
    }
}