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
public class OwnerInfoTool {

    @Autowired
    private HjyCommunityClient communityClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Tool(description = "查询业主信息。用于回答'我的信息'、'业主信息'、'查一下我的信息'等问题")
    public String queryOwnerInfo(
            @ToolParam(description = "业主姓名") String ownerName,
            @ToolParam(description = "联系电话") String phone) {
        log.info("查询业主信息 - ownerName: {}, phone: {}", ownerName, phone);

        try {
            String result = communityClient.get("/system/owner/list");
            JsonNode root = objectMapper.readTree(result);
            JsonNode data = root.path("rows");

            if (!data.isArray() || data.isEmpty()) {
                return "当前暂无业主记录。";
            }

            StringBuilder sb = new StringBuilder();
            int count = 0;

            for (JsonNode item : data) {
                if (count >= 5) break;

                String itemName = item.path("ownerRealName").asText();
                String itemPhone = item.path("ownerPhoneNumber").asText("");

                if (ownerName != null && !ownerName.isEmpty() && !itemName.contains(ownerName)) {
                    continue;
                }
                if (phone != null && !phone.isEmpty() && !itemPhone.contains(phone)) {
                    continue;
                }

                if (count == 0) {
                    sb.append("【业主信息查询结果】\n\n");
                }

                sb.append("【业主信息】\n");
                sb.append("  业主ID：").append(item.path("ownerId").asText()).append("\n");
                sb.append("  姓名：").append(itemName).append("\n");
                sb.append("  性别：").append(formatGender(item.path("ownerGender").asText())).append("\n");
                sb.append("  联系电话：").append(itemPhone).append("\n");
                sb.append("  房屋信息：").append(item.path("roomName").asText("未绑定")).append("\n");
                sb.append("  业主状态：").append(formatStatus(item.path("ownerStatus").asText())).append("\n");
                sb.append("\n");
                count++;
            }

            if (count == 0) {
                return "未找到符合条件的业主信息\n";
            }

            sb.append("共查询到 ").append(count).append(" 条业主记录\n");
            return sb.toString();

        } catch (Exception e) {
            log.error("查询业主信息失败: {}", e.getMessage());
            return "查询业主信息失败，请稍后重试。";
        }
    }

    @Tool(description = "查询业主车辆信息。用于回答'车辆信息'、'车牌号'、'我的车'等问题")
    public String getOwnerVehicles(
            @ToolParam(description = "业主姓名") String ownerName) {
        log.info("查询业主车辆信息 - ownerName: {}", ownerName);

        if (ownerName == null || ownerName.isEmpty()) {
            return "请提供业主姓名。";
        }

        try {
            String result = communityClient.get("/system/owner/list");
            JsonNode root = objectMapper.readTree(result);
            JsonNode data = root.path("rows");

            if (!data.isArray() || data.isEmpty()) {
                return "当前暂无业主记录。";
            }

            for (JsonNode item : data) {
                String itemName = item.path("ownerRealName").asText();
                if (itemName.contains(ownerName)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("【业主车辆信息】\n\n");
                    sb.append("  业主：").append(itemName).append("\n");

                    String carNo = item.path("carNo").asText();
                    if (carNo != null && !carNo.isEmpty() && !"null".equals(carNo)) {
                        sb.append("  车牌号：").append(carNo).append("\n");
                        sb.append("  车辆品牌：").append(item.path("carBrand").asText("未知")).append("\n");
                        sb.append("  车辆颜色：").append(item.path("carColor").asText("未知")).append("\n");
                    } else {
                        sb.append("  暂无登记车辆信息\n");
                    }

                    return sb.toString();
                }
            }
            return "未找到该业主的车辆信息，请检查姓名是否正确。";

        } catch (Exception e) {
            log.error("查询车辆信息失败: {}", e.getMessage());
            return "查询车辆信息失败，请稍后重试。";
        }
    }

    @Tool(description = "查询家庭成员信息。用于回答'家庭成员'、'家人信息'、'有几口人'等问题")
    public String getFamilyMembers(
            @ToolParam(description = "业主姓名") String ownerName) {
        log.info("查询家庭成员 - ownerName: {}", ownerName);

        if (ownerName == null || ownerName.isEmpty()) {
            return "请提供业主姓名。";
        }

        try {
            String result = communityClient.get("/system/owner/list");
            JsonNode root = objectMapper.readTree(result);
            JsonNode data = root.path("rows");

            if (!data.isArray() || data.isEmpty()) {
                return "当前暂无业主记录。";
            }

            for (JsonNode item : data) {
                String itemName = item.path("ownerRealName").asText();
                if (itemName.contains(ownerName)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("【家庭成员信息】\n\n");
                    sb.append("  户主：").append(itemName).append("\n");
                    sb.append("  联系电话：").append(item.path("ownerPhoneNumber").asText()).append("\n");
                    sb.append("  房屋信息：").append(item.path("roomName").asText("未绑定")).append("\n");
                    sb.append("\n  注：更多家庭成员信息请联系物业服务中心查询\n");

                    return sb.toString();
                }
            }
            return "未找到该业主信息，请检查姓名是否正确。";

        } catch (Exception e) {
            log.error("查询家庭成员失败: {}", e.getMessage());
            return "查询家庭成员信息失败，请稍后重试。";
        }
    }

    @Tool(description = "查询访客登记信息。用于回答'访客'、'有谁来过了'、'访客记录'等问题")
    public String queryVisitors(
            @ToolParam(description = "业主姓名") String ownerName,
            @ToolParam(description = "日期，格式：yyyy-MM-dd") String date) {
        log.info("查询访客记录 - ownerName: {}, date: {}", ownerName, date);

        try {
            String result = communityClient.get("/system/visitor/list");
            JsonNode root = objectMapper.readTree(result);
            JsonNode data = root.path("rows");

            if (!data.isArray() || data.isEmpty()) {
                return "暂无访客登记记录。";
            }

            StringBuilder sb = new StringBuilder();
            int count = 0;

            for (JsonNode item : data) {
                if (count >= 5) break;

                if (ownerName != null && !ownerName.isEmpty()) {
                    String visitorOwner = item.path("ownerRealName").asText();
                    if (!visitorOwner.contains(ownerName)) {
                        continue;
                    }
                }

                if (count == 0) {
                    sb.append("【访客登记记录】\n\n");
                }

                sb.append("【访客】\n");
                sb.append("  访客姓名：").append(item.path("visitorName").asText()).append("\n");
                sb.append("  来访时间：").append(item.path("visitTime").asText()).append("\n");
                sb.append("  离开时间：").append(item.path("leaveTime").asText("未离开")).append("\n");
                sb.append("  来访事由：").append(item.path("visitReason").asText()).append("\n");
                sb.append("\n");
                count++;
            }

            if (count == 0) {
                return "未找到符合条件的访客记录。";
            }

            sb.append("共查询到 ").append(count).append(" 条访客记录");
            return sb.toString();

        } catch (Exception e) {
            log.error("查询访客记录失败: {}", e.getMessage());
            return "查询访客记录失败，请稍后重试。";
        }
    }

    @Tool(description = "登记访客信息。用于回答'登记访客'、'有人来访'等问题")
    public String registerVisitor(
            @ToolParam(description = "访客姓名") String visitorName,
            @ToolParam(description = "访客电话") String visitorPhone,
            @ToolParam(description = "被访业主姓名") String ownerName,
            @ToolParam(description = "来访事由") String reason) {
        log.info("登记访客 - visitorName: {}, ownerName: {}", visitorName, ownerName);

        if (visitorName == null || visitorName.isEmpty()) {
            return "请提供访客姓名。";
        }
        if (ownerName == null || ownerName.isEmpty()) {
            return "请提供被访业主姓名。";
        }

        try {
            java.util.Map<String, Object> body = new java.util.HashMap<>();
            body.put("visitorName", visitorName);
            body.put("visitorPhone", visitorPhone != null ? visitorPhone : "");
            body.put("ownerName", ownerName);
            body.put("visitReason", reason != null ? reason : "探访");
            body.put("visitTime", java.time.LocalDateTime.now().toString());

            String result = communityClient.post("/system/visitor", body);
            JsonNode root = objectMapper.readTree(result);

            if (root.path("code").asInt() == 200) {
                return String.format("""
                        访客登记成功！

                        【登记信息】
                        访客：%s
                        被访业主：%s
                        来访事由：%s
                        来访时间：%s

                        请提醒被访业主迎接。
                        """, visitorName, ownerName, 
                        reason != null ? reason : "探访",
                        java.time.LocalDateTime.now().toString());
            } else {
                return "访客登记失败：" + root.path("msg").asText();
            }

        } catch (Exception e) {
            log.error("登记访客失败: {}", e.getMessage());
            return "访客登记失败，请稍后重试。";
        }
    }

    private String formatGender(String gender) {
        if (gender == null || gender.isEmpty()) return "未知";
        return "M".equalsIgnoreCase(gender) ? "男" : "F".equalsIgnoreCase(gender) ? "女" : gender;
    }

    private String formatStatus(String status) {
        if (status == null || status.isEmpty()) return "正常";
        return "0".equals(status) ? "正常" : "1".equals(status) ? "禁用" : status;
    }
}