package com.msb.hjy.ai.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msb.hjy.ai.client.HjyCommunityClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ComplaintTool {

    @Autowired
    private HjyCommunityClient communityClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String queryComplaints(String status, String type) {
        log.info("查询投诉列表 - status: {}, type: {}", status, type);

        try {
            String result = communityClient.get("/system/suggest/list");
            JsonNode root = objectMapper.readTree(result);
            JsonNode data = root.path("data");

            if (data.isArray() && data.size() > 0) {
                StringBuilder sb = new StringBuilder("【投诉建议列表】\n\n");

                int count = 0;
                for (JsonNode item : data) {
                    if (count >= 5) break;

                    String suggestType = item.path("suggestType").asText();
                    String suggestState = item.path("suggestState").asText("Pending");

                    if (status != null && !status.isEmpty() &&
                            !suggestState.equalsIgnoreCase(status)) {
                        continue;
                    }
                    if (type != null && !type.isEmpty() &&
                            !suggestType.contains(type)) {
                        continue;
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
                    sb.append("未找到符合条件的投诉建议\n");
                } else {
                    sb.append("共查询到 ").append(count).append(" 条记录\n");
                }

                return sb.toString();
            } else {
                return "当前暂无投诉建议记录。";
            }

        } catch (Exception e) {
            log.error("查询投诉建议失败: {}", e.getMessage());
            return "查询投诉建议失败，请稍后重试。";
        }
    }

    public String submitComplaint(String ownerName, String phone, String type, String content, String location) {
        log.info("提交投诉 - ownerName: {}, type: {}", ownerName, type);

        try {
            java.util.Map<String, Object> body = new java.util.HashMap<>();
            body.put("suggestName", ownerName);
            body.put("suggestPhone", phone);
            body.put("suggestType", type);
            body.put("suggestContent", content);
            body.put("suggestLocation", location);

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
                        """, ownerName, phone, formatType(type), content,
                        location != null ? location : "未指定");
            } else {
                return "投诉建议提交失败：" + root.path("msg").asText();
            }

        } catch (Exception e) {
            log.error("提交投诉建议失败: {}", e.getMessage());
            return "投诉建议提交失败，请稍后重试。";
        }
    }

    public String getComplaintDetail(String complaintId) {
        log.info("查询投诉详情 - complaintId: {}", complaintId);

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

    private String formatType(String type) {
        if (type == null) return "未知";
        return switch (type.toLowerCase()) {
            case "service" -> "服务态度";
            case "environment" -> "环境卫生";
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
