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
public class CommunityTool {

    @Autowired
    private HjyCommunityClient communityClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Tool(description = "查询社区基本信息。用于回答'小区介绍'、'社区信息'、'小区怎么样'等问题")
    public String queryCommunityInfo(
            @ToolParam(description = "信息类型：basic(基本信息), property(物业信息), contact(联系方式)") String infoType) {
        log.info("查询社区信息 - infoType: {}", infoType);

        try {
            String result = communityClient.get("/system/community/list");
            JsonNode root = objectMapper.readTree(result);
            JsonNode data = root.path("rows");

            if (!data.isArray() || data.isEmpty()) {
                return getDefaultCommunityInfo();
            }

            for (JsonNode item : data) {
                String communityName = item.path("communityName").asText("合家云社区");
                String address = item.path("communityAddress").asText("智慧小区");
                String builtYear = item.path("builtYear").asText("2020");
                String totalHouseholds = item.path("totalHouseholds").asText("1500");
                String area = item.path("area").asText("8.5");
                String greenRate = item.path("greenRate").asText("35");
                String parkingSpaces = item.path("parkingSpaces").asText("700");

                StringBuilder sb = new StringBuilder();
                sb.append("【社区基本信息】\n\n");
                sb.append("  小区名称：").append(communityName).append("\n");
                sb.append("  地址：").append(address).append("\n");
                sb.append("  竣工时间：").append(builtYear).append("年\n");
                sb.append("  总户数：").append(totalHouseholds).append("户\n");
                sb.append("  占地面积：").append(area).append("万平方米\n");
                sb.append("  绿化率：").append(greenRate).append("%\n");
                sb.append("  停车位：").append(parkingSpaces).append("个\n");
                sb.append("\n【配套情况】\n");
                sb.append("  - 电梯：每栋配备\n");
                sb.append("  - 监控：全小区覆盖\n");
                sb.append("  - 门禁：智能化管理\n");
                sb.append("\n如需了解更多，请告诉我具体想了解哪方面。");

                return sb.toString();
            }

            return getDefaultCommunityInfo();

        } catch (Exception e) {
            log.error("查询社区信息失败: {}", e.getMessage());
            return getDefaultCommunityInfo();
        }
    }

    @Tool(description = "查询社区设施信息。用于回答'有什么设施'、'健身房'、'游乐场'、'游泳池'等问题")
    public String getFacilities() {
        log.info("查询社区设施");

        return """
                【社区设施信息】

                【休闲设施】
                - 中心花园：开放时间 6:00-22:00
                - 儿童游乐场：开放时间 8:00-20:00
                - 健身器材区：开放时间 6:00-22:00

                【运动设施】
                - 篮球场：开放时间 8:00-22:00
                - 羽毛球场：需提前预约
                - 乒乓球室：开放时间 8:00-21:00
                - 健身房：会员制，物业前台办理

                【生活设施】
                - 快递驿站：9:00-21:00
                - 便利超市：7:00-23:00
                - 社区餐厅：早餐 7:00-9:00，午餐 11:30-13:30，晚餐 17:30-20:00
                - 美容美发：9:00-21:00

                【公共设施】
                - 地下车库：24小时开放
                - 电梯：24小时运行
                - 监控系统：24小时运行
                - 门禁系统：24小时运行

                如需预约场地或了解更多设施使用规则，请告诉我。
                """;
    }

    @Tool(description = "查询周边配套信息。用于回答'周边有什么'、'附近配套'、'地铁站'、'学校'等问题")
    public String getNearbyInfo(
            @ToolParam(description = "配套类别：traffic(交通), education(教育), medical(医疗), shopping(商业), park(公园)") String category) {
        log.info("查询周边配套 - category: {}", category);

        try {
            String result = communityClient.get("/system/community/nearby");
            JsonNode root = objectMapper.readTree(result);
            JsonNode data = root.path("rows");

            if (!data.isArray() || data.isEmpty()) {
                return getDefaultNearbyInfo();
            }

            StringBuilder sb = new StringBuilder();
            sb.append("【周边配套信息】\n\n");

            for (JsonNode item : data) {
                String type = item.path("type").asText();
                String name = item.path("name").asText();
                String distance = item.path("distance").asText();
                String description = item.path("description").asText();

                if (category != null && !category.isEmpty() && !type.contains(category)) {
                    continue;
                }

                sb.append(formatNearbyType(type)).append("\n");
                sb.append("  名称：").append(name).append("\n");
                sb.append("  距离：").append(distance).append("\n");
                sb.append("  说明：").append(description).append("\n\n");
            }

            return sb.toString();

        } catch (Exception e) {
            log.error("查询周边配套失败: {}", e.getMessage());
            return getDefaultNearbyInfo();
        }
    }

    @Tool(description = "预约社区设施。用于回答'预约场地'、'预约设施'、'预定篮球场'等问题")
    public String getFacilitiesReservation(
            @ToolParam(description = "设施名称，如：篮球场、健身房") String facility,
            @ToolParam(description = "预约日期，格式：yyyy-MM-dd") String date,
            @ToolParam(description = "时间段，如：14:00-16:00") String timeSlot) {
        log.info("预约设施 - facility: {}, date: {}, timeSlot: {}", facility, date, timeSlot);

        if (facility == null || facility.isEmpty()) {
            return "请提供要预约的设施名称。";
        }
        if (date == null || date.isEmpty()) {
            return "请提供预约日期。";
        }
        if (timeSlot == null || timeSlot.isEmpty()) {
            return "请提供预约时间段。";
        }

        try {
            Map<String, Object> body = new HashMap<>();
            body.put("facilityName", facility);
            body.put("reservationDate", date);
            body.put("timeSlot", timeSlot);
            body.put("status", "Pending");

            String result = communityClient.post("/system/facility/reserve", body);
            JsonNode root = objectMapper.readTree(result);

            if (root.path("code").asInt() == 200) {
                return String.format("""
                        【设施预约成功】

                        【预约信息】
                        设施：%s
                        日期：%s
                        时间：%s

                        请按预约时间前往使用设施。
                        如需取消或更改预约，请提前联系物业服务中心。
                        """, facility, date, timeSlot);
            } else {
                return String.format("""
                        【设施预约】

                        您好！设施预约功能正在建设中，暂时无法在线预约。

                        如需预约设施，请拨打服务热线：400-888-8888
                        或前往物业服务中心（1号楼B1层）办理。

                        预约信息：
                        设施：%s
                        日期：%s
                        时间：%s

                        我们将尽快完善在线预约功能，感谢您的理解！
                        """, facility, date, timeSlot);
            }

        } catch (Exception e) {
            log.error("预约设施失败: {}", e.getMessage());
            return String.format("""
                    【设施预约】

                    您好！设施预约功能正在建设中，暂时无法在线预约。

                    如需预约设施，请拨打服务热线：400-888-8888
                    或前往物业服务中心（1号楼B1层）办理。

                    预约信息：
                    设施：%s
                    日期：%s
                    时间：%s

                    我们将尽快完善在线预约功能，感谢您的理解！
                    """, facility, date, timeSlot);
        }
    }

    @Tool(description = "查询门禁卡办理信息。用于回答'门禁卡'、'门禁权限'、'如何开门'等问题")
    public String getAccessCardInfo(
            @ToolParam(description = "业主姓名") String ownerName) {
        log.info("查询门禁卡信息 - ownerName: {}", ownerName);

        try {
            String result = communityClient.get("/system/owner/list");
            JsonNode root = objectMapper.readTree(result);
            JsonNode data = root.path("rows");

            if (!data.isArray() || data.isEmpty()) {
                return "未找到业主信息。";
            }

            for (JsonNode item : data) {
                String itemName = item.path("ownerName").asText();
                if (ownerName != null && !ownerName.isEmpty() && !itemName.contains(ownerName)) {
                    continue;
                }

                return """
                        【门禁卡办理信息】

                        【办理方式】
                        1. 携带业主身份证原件
                        2. 前往物业服务中心办理
                        3. 现场采集人脸信息（可选）

                        【费用说明】
                        - 首次办理：免费
                        - 补办工本费：10元/张

                        【使用方式】
                        - 刷卡开门
                        - 手机APP远程开门
                        - 人脸识别（需开通）

                        【服务时间】
                        - 物业服务中心：工作日 8:30-17:30
                        - 24小时服务热线：400-888-8888

                        如需帮助，请拨打服务热线。
                        """;
            }

            return "未找到该业主的信息。";

        } catch (Exception e) {
            log.error("查询门禁卡信息失败: {}", e.getMessage());
            return """
                    【门禁卡办理信息】

                    【办理方式】
                    1. 携带业主身份证原件
                    2. 前往物业服务中心办理

                    【费用说明】
                    - 首次办理：免费
                    - 补办工本费：10元/张

                    【服务时间】
                    工作日 8:30-17:30
                    """;
        }
    }

    @Tool(description = "查询便民服务信息。用于回答'便民服务'、'便民'、'维修'、'家政'等问题")
    public String getConvenientServices(
            @ToolParam(description = "服务类型：repair(维修), cleaning(保洁), moving(搬家), nursing(护理)") String serviceType) {
        log.info("查询便民服务 - serviceType: {}", serviceType);

        StringBuilder sb = new StringBuilder();
        sb.append("【便民服务信息】\n\n");

        if (serviceType == null || serviceType.isEmpty() || "repair".equalsIgnoreCase(serviceType)) {
            sb.append("【便民维修】\n");
            sb.append("  - 水电维修：专业师傅，快速响应\n");
            sb.append("  - 门窗维修：更换锁芯、五金配件\n");
            sb.append("  - 家电维修：空调、冰箱、洗衣机\n");
            sb.append("  预约电话：400-888-8888\n\n");
        }

        if (serviceType == null || serviceType.isEmpty() || "cleaning".equalsIgnoreCase(serviceType)) {
            sb.append("【保洁服务】\n");
            sb.append("  - 日常保洁：每小时50元\n");
            sb.append("  - 深度清洁：每次200元起\n");
            sb.append("  - 开荒保洁：每平方米3元\n\n");
        }

        if (serviceType == null || serviceType.isEmpty() || "moving".equalsIgnoreCase(serviceType)) {
            sb.append("【搬家服务】\n");
            sb.append("  - 居民搬家：200元/次起\n");
            sb.append("  - 货物运输：面议\n");
            sb.append("  合作商家：蚂蚁搬家\n\n");
        }

        if (serviceType == null || serviceType.isEmpty() || "nursing".equalsIgnoreCase(serviceType)) {
            sb.append("【护理服务】\n");
            sb.append("  - 老人护理：专业护工\n");
            sb.append("  - 病人陪护：医院/居家\n");
            sb.append("  - 母婴护理：月嫂、育儿嫂\n");
            sb.append("  合作商家：爱心护理中心\n\n");
        }

        sb.append("【服务须知】\n");
        sb.append("  - 以上服务需提前预约\n");
        sb.append("  - 详细费用以实际为准\n");
        sb.append("  - 服务热线：400-888-8888\n");

        return sb.toString();
    }

    private String getDefaultCommunityInfo() {
        return """
                【社区基本信息】

                小区名称：合家云社区
                竣工时间：2020年
                总户数：1500户
                占地面积：8.5万平方米
                绿化率：35%
                停车位：地下车库500个，地面车位200个

                物业管理：合家云物业服务中心
                服务电话：400-888-8888
                物业地址：1号楼B1层
                服务时间：24小时
                """;
    }

    private String getDefaultNearbyInfo() {
        return """
                【周边配套信息】

                【交通配套】
                - 地铁站：2号线XX路站，约800米
                - 公交站：XX路XX路站，多条线路经过
                - 自驾路线：小区出入口连接主干道，出行便利

                【教育资源】
                - 幼儿园：XX幼儿园（省级示范），约500米
                - 小学：XX小学（市重点），约1公里
                - 中学：XX中学，约2公里

                【医疗资源】
                - 社区医院：约500米
                - 三甲医院：XX医院，约3公里
                - 药店：小区便利店旁有2家

                【商业配套】
                - 大型超市：XX超市，约1公里
                - 购物中心：XX广场，约2公里
                - 银行：XX银行XX支行，约800米
                - 菜市场：约600米

                【休闲公园】
                - XX公园：约1公里
                - XX滨江步道：约2公里

                如需了解更多信息，请告诉我。
                """;
    }

    private String formatNearbyType(String type) {
        if (type == null) return "";
        return switch (type.toLowerCase()) {
            case "traffic" -> "【交通】";
            case "education" -> "【教育】";
            case "medical" -> "【医疗】";
            case "shopping" -> "【商业】";
            case "park" -> "【公园】";
            default -> "【配套】";
        };
    }
}