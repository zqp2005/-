package com.msb.hjy.ai.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msb.hjy.ai.client.HjyCommunityClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OwnerInfoTool {

    @Autowired
    private HjyCommunityClient communityClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String queryOwnerInfo(String ownerName, String phone) {
        log.info("查询业主信息 - ownerName: {}, phone: {}", ownerName, phone);

        try {
            String result = communityClient.get("/system/owner/list");
            JsonNode root = objectMapper.readTree(result);
            JsonNode data = root.path("data");

            if (data.isArray() && data.size() > 0) {
                StringBuilder sb = new StringBuilder("【业主信息查询结果】\n\n");

                int count = 0;
                for (JsonNode item : data) {
                    if (count >= 5) break;

                    String itemName = item.path("ownerName").asText();
                    String itemPhone = item.path("ownerPhone").asText("");

                    if (ownerName != null && !ownerName.isEmpty() &&
                            !itemName.contains(ownerName)) {
                        continue;
                    }
                    if (phone != null && !phone.isEmpty() &&
                            !itemPhone.contains(phone)) {
                        continue;
                    }

                    sb.append("【业主信息】\n");
                    sb.append("  业主ID：").append(item.path("ownerId").asText()).append("\n");
                    sb.append("  姓名：").append(itemName).append("\n");
                    sb.append("  性别：").append(item.path("ownerSex").asText("未知")).append("\n");
                    sb.append("  联系电话：").append(itemPhone).append("\n");
                    sb.append("  房屋信息：").append(item.path("roomName").asText("未绑定")).append("\n");
                    sb.append("\n");
                    count++;
                }

                if (count == 0) {
                    sb.append("未找到符合条件的业主信息\n");
                } else {
                    sb.append("共查询到 ").append(count).append(" 条业主记录\n");
                }

                return sb.toString();
            } else {
                return "当前暂无业主记录。";
            }

        } catch (Exception e) {
            log.error("查询业主信息失败: {}", e.getMessage());
            return "查询业主信息失败，请稍后重试。错误信息: " + e.getMessage();
        }
    }

    public String getOwnerVehicles(String ownerName) {
        log.info("查询业主车辆信息 - ownerName: {}", ownerName);

        try {
            String result = communityClient.get("/system/owner/list");
            JsonNode root = objectMapper.readTree(result);
            JsonNode data = root.path("data");

            if (data.isArray() && data.size() > 0) {
                for (JsonNode item : data) {
                    String itemName = item.path("ownerName").asText();
                    if (ownerName != null && !ownerName.isEmpty() &&
                            itemName.contains(ownerName)) {
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
            } else {
                return "当前暂无业主记录。";
            }

        } catch (Exception e) {
            log.error("查询车辆信息失败: {}", e.getMessage());
            return "查询车辆信息失败，请稍后重试。";
        }
    }

    public String getFamilyMembers(String ownerName) {
        log.info("查询家庭成员 - ownerName: {}", ownerName);

        try {
            String result = communityClient.get("/system/owner/list");
            JsonNode root = objectMapper.readTree(result);
            JsonNode data = root.path("data");

            if (data.isArray() && data.size() > 0) {
                for (JsonNode item : data) {
                    String itemName = item.path("ownerName").asText();
                    if (ownerName != null && !ownerName.isEmpty() &&
                            itemName.contains(ownerName)) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("【家庭成员信息】\n\n");
                        sb.append("  户主：").append(itemName).append("\n");
                        sb.append("  联系电话：").append(item.path("ownerPhone").asText()).append("\n");
                        sb.append("  房屋信息：").append(item.path("roomName").asText("未绑定")).append("\n");
                        sb.append("\n  注：更多家庭成员信息请联系物业服务中心查询\n");

                        return sb.toString();
                    }
                }
                return "未找到该业主信息，请检查姓名是否正确。";
            } else {
                return "当前暂无业主记录。";
            }

        } catch (Exception e) {
            log.error("查询家庭成员失败: {}", e.getMessage());
            return "查询家庭成员信息失败，请稍后重试。";
        }
    }
}
