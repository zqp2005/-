package com.msb.hjy.ai.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msb.hjy.ai.client.HjyCommunityClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ComplaintTool {

    @Autowired
    private HjyCommunityClient communityClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Tool(description = "查询投诉建议列表，可以按状态或类型筛选。用于回答'投诉进度'、'我的建议'、'有哪些投诉'等问题")
    public String queryComplaints(
            @ToolParam(description = "状态筛选：pending(待处理), processing(处理中), resolved(已解决), closed(已关闭)") String status,
            @ToolParam(description = "类型筛选：service(服务态度), sanitation(环境卫生), facility(设施设备), noise(噪音), other(其他)") String type) {
        log.info("查询投诉列表 - status: {}, type: {}", status, type);

        try {
            String result = communityClient.get("/system/suggest/list");
            JsonNode root = objectMapper.readTree(result);
            JsonNode data = root.path("rows");

            if (!data.isArray() || data.isEmpty()) {
                return "当前暂无投诉建议记录。";
            }

            StringBuilder sb = new StringBuilder();
            int count = 0;

            for (JsonNode item : data) {
                if (count >= 5) break;

                String suggestType = item.path("suggestType").asText();
                String suggestState = item.path("suggestState").asText("Pending");

                if (status != null && !status.isEmpty() && !suggestState.equalsIgnoreCase(status)) {
                    continue;
                }
                if (type != null && !type.isEmpty() && !suggestType.contains(type)) {
                    continue;
                }

                if (count == 0) {
                    sb.append("【投诉建议列表】\n\n");
                }

                sb.append("【投诉建议】\n");
                sb.append("  单号：").append(item.path("complaintSuggestId").asText()).append("\n");
                sb.append("  类型：").append(formatType(suggestType)).append("\n");
                sb.append("  内容：").append(item.path("suggestContent").asText()).append("\n");
                sb.append("  状态：").append(formatState(suggestState)).append("\n");
                sb.append("  提交时间：").append(item.path("createTime").asText()).append("\n");
                sb.append("\n");
                count++;
            }

            if (count == 0) {
                return "未找到符合条件的投诉建议\n";
            }

            sb.append("共查询到 ").append(count).append(" 条记录\n");
            return sb.toString();

        } catch (Exception e) {
            log.error("查询投诉建议失败: {}", e.getMessage());
            return "查询投诉建议失败，请稍后重试。";
        }
    }

    @Tool(description = "提交投诉建议。用于回答'我要投诉'、'提建议'、'反馈问题'等问题，需要投诉���信息、类型和内容")
    public String submitComplaint(
            @ToolParam(description = "投诉人姓名，必填") String ownerName,
            @ToolParam(description = "联系电话") String phone,
            @ToolParam(description = "投诉类型：service, sanitation, facility, noise, other，必填") String type,
            @ToolParam(description = "投诉内容，必填") String content,
            @ToolParam(description = "投诉地点") String location) {
        log.info("提交投诉 - ownerName: {}, type: {}", ownerName, type);

        if (ownerName == null || ownerName.isEmpty()) {
            return "请提供投诉人姓名。";
        }
        if (type == null || type.isEmpty()) {
            return "请提供投诉类型。";
        }
        if (content == null || content.isEmpty()) {
            return "请提供投诉内容。";
        }

        try {
            java.util.Map<String, Object> body = new java.util.HashMap<>();
            body.put("ownerRealName", ownerName);
            body.put("ownerPhoneNumber", phone != null ? phone : "");
            body.put("complaintSuggestType", type);
            body.put("complaintSuggestContent", content);
            body.put("suggestLocation", location != null ? location : "");

            String result = communityClient.post("/system/suggest", body);
            JsonNode root = objectMapper.readTree(result);

            if (root.path("code").asInt() == 200) {
                return String.format("""
                        投诉建议提交成功！

                        【提交信息】
                        姓名：%s
                        联系电话：%s
                        类型：%s
                        内容：%s
                        地点：%s

                        我们将认真处理您的投诉建议，一般处理时限为3个工作日。
                        感谢您对物业工作的监督与支持！
                        """, ownerName, phone != null ? phone : "未留", formatType(type), content,
                        location != null ? location : "未指定");
            } else {
                return "投诉建议提交失败：" + root.path("msg").asText();
            }

        } catch (Exception e) {
            log.error("提交投诉建议失败: {}", e.getMessage());
            return "投诉建议提交失败，请稍后重试。";
        }
    }

    @Tool(description = "获取投诉建议详情。用于回答'投诉详情'、'建议详情'、'我的投诉xxx'等问题")
    public String getComplaintDetail(
            @ToolParam(description = "投诉建议编号") String complaintId) {
        log.info("查询投诉详情 - complaintId: {}", complaintId);

        if (complaintId == null || complaintId.isEmpty()) {
            return "请提供投��建议编号。";
        }

        try {
            String result = communityClient.get("/system/suggest/" + complaintId);
            JsonNode root = objectMapper.readTree(result);
            JsonNode data = root.path("data");

            if (data.isEmpty() || data.isNull()) {
                return "未找到该投诉建议，请检查单号是否正确。";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("【投诉建议详情】\n");
            sb.append("  单号：").append(data.path("complaintSuggestId").asText()).append("\n");
            sb.append("  姓名：").append(data.path("suggestName").asText()).append("\n");
            sb.append("  联系电话：").append(data.path("suggestPhone").asText()).append("\n");
            sb.append("  类型：").append(formatType(data.path("suggestType").asText())).append("\n");
            sb.append("  内容：").append(data.path("suggestContent").asText()).append("\n");
            sb.append("  地点：").append(data.path("suggestLocation").asText()).append("\n");
            sb.append("  状态：").append(formatState(data.path("suggestState").asText())).append("\n");
            sb.append("  提交时间：").append(data.path("createTime").asText()).append("\n");

            return sb.toString();

        } catch (Exception e) {
            log.error("查询投诉详情失败: {}", e.getMessage());
            return "查询投诉详情失败，请稍后重试。";
        }
    }

    @Tool(description = "评价投诉处理结果。用于回答'评价投诉'、'投诉处理满意'等问题")
    public String rateComplaint(
            @ToolParam(description = "投诉编号") String complaintId,
            @ToolParam(description = "评分：1-5分") Integer rating,
            @ToolParam(description = "评价内容") String comment) {
        log.info("评价投诉 - complaintId: {}, rating: {}", complaintId, rating);

        if (complaintId == null || complaintId.isEmpty()) {
            return "请提供投诉编号。";
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
                【投诉评价成功】

                【评价信息】
                投诉编号：%s
                评分：%d分（%s）
                评价内容：%s

                感谢您的宝贵意见！
                我们将继续改进服务质量。
                """, complaintId, rating, ratingText, 
                comment != null ? comment : "未填写");
    }

    private String formatType(String type) {
        if (type == null) return "未知";
        return switch (type.toLowerCase()) {
            case "service" -> "服务态度";
            case "sanitation" -> "环境卫生";
            case "facility" -> "设施设备";
            case "noise" -> "噪音扰民";
            case "other" -> "其他";
            default -> type;
        };
    }

    private String formatState(String state) {
        if (state == null) return "未知";
        return switch (state.toLowerCase()) {
            case "pending" -> "待处理";
            case "processing" -> "处理中";
            case "resolved" -> "已解决";
            case "closed" -> "已关闭";
            default -> state;
        };
    }
}