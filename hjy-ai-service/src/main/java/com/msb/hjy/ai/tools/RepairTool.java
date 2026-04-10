package com.msb.hjy.ai.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msb.hjy.ai.client.HjyCommunityClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RepairTool {

    @Autowired
    private HjyCommunityClient communityClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String queryRepairOrders(String status, String ownerName) {
        log.info("查询报修工单 - status: {}, ownerName: {}", status, ownerName);

        try {
            String result = communityClient.get("/system/repair/list");
            JsonNode root = objectMapper.readTree(result);
            JsonNode data = root.path("data");

            if (data.isArray() && data.size() > 0) {
                StringBuilder sb = new StringBuilder("【报修工单查询结果】\n\n");

                int count = 0;
                for (JsonNode item : data) {
                    if (count >= 5) break;

                    String repairState = item.path("repairState").asText();
                    String itemOwnerName = item.path("ownerName").asText("未知");

                    if (status != null && !status.isEmpty() && !repairState.equalsIgnoreCase(status)) {
                        continue;
                    }
                    if (ownerName != null && !ownerName.isEmpty() &&
                            !itemOwnerName.contains(ownerName)) {
                        continue;
                    }

                    sb.append("【报修单】\n");
                    sb.append("  工单号：").append(item.path("repairId").asText()).append("\n");
                    sb.append("  业主：").append(itemOwnerName).append("\n");
                    sb.append("  问题：").append(item.path("repairDescription").asText()).append("\n");
                    sb.append("  状态：").append(formatState(repairState)).append("\n");
                    sb.append("  提交时间：").append(item.path("createTime").asText()).append("\n");
                    sb.append("\n");
                    count++;
                }

                if (count == 0) {
                    sb.append("未找到符合条件的报修工单\n");
                } else {
                    sb.append("共查询到 ").append(count).append(" 条报修记录\n");
                }

                sb.append("\n如需了解更多信息，请告诉我具体工单号。");
                return sb.toString();
            } else {
                return "当前暂无报修工单记录。";
            }

        } catch (Exception e) {
            log.error("查询报修工单失败: {}", e.getMessage());
            return "查询报修工单失败，请稍后重试。错误信息: " + e.getMessage();
        }
    }

    public String createRepairOrder(String ownerName, String phone, String location, String problem, String category) {
        log.info("创建报修工单 - ownerName: {}, problem: {}", ownerName, problem);

        try {
            java.util.Map<String, Object> body = new java.util.HashMap<>();
            body.put("ownerName", ownerName);
            body.put("ownerPhone", phone);
            body.put("repairLocation", location);
            body.put("repairDescription", problem);
            body.put("repairCategory", category);

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
                        """, ownerName, phone, location, problem, category);
            } else {
                return "报修工单创建失败：" + root.path("msg").asText();
            }

        } catch (Exception e) {
            log.error("创建报修工单失败: {}", e.getMessage());
            return "创建报修工单失败，请稍后重试。";
        }
    }

    public String getRepairDetail(String repairId) {
        log.info("查询报修详情 - repairId: {}", repairId);

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
            sb.append("  业主：").append(data.path("ownerName").asText()).append("\n");
            sb.append("  联系电话：").append(data.path("ownerPhone").asText()).append("\n");
            sb.append("  报修位置：").append(data.path("repairLocation").asText()).append("\n");
            sb.append("  问题描述：").append(data.path("repairDescription").asText()).append("\n");
            sb.append("  报修类别：").append(data.path("repairCategory").asText()).append("\n");
            sb.append("  状态：").append(formatState(data.path("repairState").asText())).append("\n");
            sb.append("  提交时间：").append(data.path("createTime").asText()).append("\n");

            return sb.toString();

        } catch (Exception e) {
            log.error("查询报修详情失败: {}", e.getMessage());
            return "查询报修详情失败，请稍后重试。";
        }
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
            default -> state;
        };
    }
}
