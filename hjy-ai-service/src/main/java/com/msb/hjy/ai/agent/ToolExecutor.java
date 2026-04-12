package com.msb.hjy.ai.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msb.hjy.ai.client.HjyCommunityClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
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
                case "create_repair_order" -> createRepairOrder(arguments);
                case "get_repair_detail" -> getRepairDetail(arguments);
                case "query_complaints" -> queryComplaints(arguments);
                case "submit_complaint" -> submitComplaint(arguments);
                case "get_complaint_detail" -> getComplaintDetail(arguments);
                case "query_property_fee" -> queryPropertyFee(arguments);
                case "get_payment_guide" -> getPaymentGuide(arguments);
                case "get_payment_history" -> getPaymentHistory(arguments);
                case "get_arrears_info" -> getArrearsInfo(arguments);
                case "query_owner_info" -> queryOwnerInfo(arguments);
                case "get_owner_vehicles" -> getOwnerVehicles(arguments);
                case "get_family_members" -> getFamilyMembers(arguments);
                case "query_announcements" -> queryAnnouncements(arguments);
                case "get_announcement_detail" -> getAnnouncementDetail(arguments);
                case "get_activities" -> getActivities(arguments);
                case "query_community_info" -> queryCommunityInfo(arguments);
                case "get_facilities" -> getFacilities(arguments);
                case "get_nearby_info" -> getNearbyInfo(arguments);
                case "reserve_facility" -> reserveFacility(arguments);
                default -> "未知工具: " + toolName;
            };
        } catch (Exception e) {
            log.error("工具执行失败: {} - {}", toolName, e.getMessage());
            return "工具执行失败: " + e.getMessage();
        }
    }

    // ========== 报修服务 ==========

    private String queryRepairOrders(Map<String, Object> args) {
        try {
            String status = (String) args.get("status");
            String ownerName = (String) args.get("owner_name");

            String result = communityClient.get("/system/repair/list");
            JsonNode root = objectMapper.readTree(result);
            JsonNode data = root.path("rows");

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
                if (ownerName != null && !ownerName.isEmpty() && !itemOwnerName.contains(ownerName)) {
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
        } catch (Exception e) {
            log.error("查询报修工单失败: {}", e.getMessage());
            return "查询报修工单失败，请稍后重试。";
        }
    }

    private String createRepairOrder(Map<String, Object> args) {
        try {
            String ownerName = getStringArg(args, "owner_name");
            String phone = getStringArg(args, "phone");
            String location = getStringArg(args, "location");
            String problem = getStringArg(args, "problem");
            String category = getStringArg(args, "category");

            if (ownerName == null || problem == null) {
                return "请提供业主姓名和问题描述。";
            }

            Map<String, Object> body = new HashMap<>();
            body.put("ownerName", ownerName);
            body.put("ownerPhone", phone != null ? phone : "");
            body.put("repairLocation", location != null ? location : "");
            body.put("repairDescription", problem);
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

                        我们将尽快安排维修人员与您联系，请保持电话畅通。
                        """, ownerName, phone, location, problem);
            } else {
                return "报修工单创建失败：" + root.path("msg").asText();
            }
        } catch (Exception e) {
            log.error("创建报修工单失败: {}", e.getMessage());
            return "创建报修工单失败，请稍后重试。";
        }
    }

    private String getRepairDetail(Map<String, Object> args) {
        try {
            String repairId = getStringArg(args, "repair_id");
            if (repairId == null) {
                return "请提供报修工单号。";
            }

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
            sb.append("  状态：").append(formatRepairState(data.path("repairState").asText())).append("\n");
            sb.append("  提交时间：").append(data.path("createTime").asText()).append("\n");

            return sb.toString();
        } catch (Exception e) {
            log.error("查询报修详情失败: {}", e.getMessage());
            return "查询报修详情失败，请稍后重试。";
        }
    }

    // ========== 投诉建议 ==========

    private String queryComplaints(Map<String, Object> args) {
        try {
            String status = getStringArg(args, "status");
            String type = getStringArg(args, "type");

            String result = communityClient.get("/system/suggest/list");
            JsonNode root = objectMapper.readTree(result);
            JsonNode data = root.path("rows");

            if (!data.isArray() || data.isEmpty()) {
                return "当前暂无投诉建议记录。";
            }

            StringBuilder sb = new StringBuilder();
            int count = 0;

            for (JsonNode item : data) {
                if (count >= 10) break;

                String itemType = item.path("suggestType").asText();
                String itemStatus = item.path("suggestState").asText("Pending");

                if (status != null && !status.isEmpty() && !itemStatus.equalsIgnoreCase(status)) {
                    continue;
                }
                if (type != null && !type.isEmpty() && !itemType.contains(type)) {
                    continue;
                }

                if (count == 0) {
                    sb.append("【投诉建议列表】\n\n");
                }

                sb.append("【建议】\n");
                sb.append("  单号：").append(item.path("complaintSuggestId").asText()).append("\n");
                sb.append("  类型：").append(formatComplaintType(itemType)).append("\n");
                sb.append("  内容：").append(item.path("suggestContent").asText()).append("\n");
                sb.append("  状态：").append(formatComplaintState(itemStatus)).append("\n");
                sb.append("  提交时间：").append(item.path("createTime").asText()).append("\n");
                sb.append("\n");
                count++;
            }

            if (count == 0) {
                return "未找到符合条件的投诉建议。";
            }

            sb.append("共查询到 ").append(count).append(" 条记录。");
            return sb.toString();
        } catch (Exception e) {
            log.error("查询投诉建议失败: {}", e.getMessage());
            return "查询投诉建议失败，请稍后重试。";
        }
    }

    private String submitComplaint(Map<String, Object> args) {
        try {
            String ownerName = getStringArg(args, "owner_name");
            String phone = getStringArg(args, "phone");
            String type = getStringArg(args, "type");
            String content = getStringArg(args, "content");
            String location = getStringArg(args, "location");

            if (ownerName == null || content == null || type == null) {
                return "请提供投诉人姓名、类型和内容。";
            }

            Map<String, Object> body = new HashMap<>();
            body.put("ownerName", ownerName);
            body.put("ownerPhone", phone != null ? phone : "");
            body.put("suggestType", type);
            body.put("suggestContent", content);
            body.put("suggestLocation", location != null ? location : "");

            String result = communityClient.post("/system/suggest", body);
            JsonNode root = objectMapper.readTree(result);

            if (root.path("code").asInt() == 200) {
                return String.format("""
                        投诉建议提交成功！

                        【提交信息】
                        业主：%s
                        联系方式：%s
                        类型：%s
                        内容：%s

                        我们将认真处理您的投诉建议，一般处理时限为3个工作日。
                        感谢您的宝贵意见！
                        """, ownerName, phone, formatComplaintType(type), content);
            } else {
                return "投诉提交失败：" + root.path("msg").asText();
            }
        } catch (Exception e) {
            log.error("提交投诉建议失败: {}", e.getMessage());
            return "提交投诉建议失败，请稍后重试。";
        }
    }

    private String getComplaintDetail(Map<String, Object> args) {
        try {
            String complaintId = getStringArg(args, "complaint_id");
            if (complaintId == null) {
                return "请提供投诉建议编号。";
            }

            String result = communityClient.get("/system/suggest/" + complaintId);
            JsonNode root = objectMapper.readTree(result);
            JsonNode data = root.path("data");

            if (data.isEmpty() || data.isNull()) {
                return "未找到该投诉建议，请检查单号是否正确。";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("【投诉建议详情】\n");
            sb.append("  单号：").append(data.path("complaintSuggestId").asText()).append("\n");
            sb.append("  姓名：").append(data.path("ownerName").asText()).append("\n");
            sb.append("  联系电话：").append(data.path("ownerPhone").asText()).append("\n");
            sb.append("  类型：").append(formatComplaintType(data.path("suggestType").asText())).append("\n");
            sb.append("  内容：").append(data.path("suggestContent").asText()).append("\n");
            sb.append("  地点：").append(data.path("suggestLocation").asText()).append("\n");
            sb.append("  状态：").append(formatComplaintState(data.path("suggestState").asText())).append("\n");
            sb.append("  提交时间：").append(data.path("createTime").asText()).append("\n");

            return sb.toString();
        } catch (Exception e) {
            log.error("查询投诉详情失败: {}", e.getMessage());
            return "查询投诉详情失败，请稍后重试。";
        }
    }

    // ========== 物业费 ==========

    private String queryPropertyFee(Map<String, Object> args) {
        Object yearObj = args.get("year");
        Object monthObj = args.get("month");
        String ownerName = getStringArg(args, "owner_name");

        int year = yearObj != null ? ((Number) yearObj).intValue() : java.time.LocalDate.now().getYear();
        int month = monthObj != null ? ((Number) monthObj).intValue() : java.time.LocalDate.now().getMonthValue();

        return String.format("""
                【物业费账单查询结果】

                账期：%d年%d月
                您好！物业费查询功能正在建设中，暂时无法直接在线查询。

                您可以通过以下方式查询物业费：
                1. 拨打物业服务热线：400-888-8888
                2. 前往物业服务中心咨询

                【物业费参考标准】
                - 住宅：2.5元/平方米/月
                - 商铺：5.0元/平方米/月
                - 车位：100元/个/月
                """, year, month);
    }

    private String getPaymentGuide(Map<String, Object> args) {
        return """
                【物业费缴纳指南】

                【缴费方式】
                1. 线上缴费：
                   - 微信关注"合家云物业"公众号
                   - 进入"物业缴费"栏目
                2. 线下缴费：
                   - 物业服务中心缴费
                   - 地址：小区1号楼B1层
                   - 时间：工作日 8:30-17:30

                【温馨提示】
                - 物业费按月缴纳，每月15日前完成
                - 逾期未缴将产生滞纳金（每日0.05%）

                【收费标准】
                - 住宅：2.5元/平方米/月
                - 商铺：5.0元/平方米/月
                - 车位：100元/个/月

                如需帮助，请拨打服务热线：400-888-8888
                """;
    }

    private String getPaymentHistory(Map<String, Object> args) {
        String startDate = getStringArg(args, "start_date");
        String endDate = getStringArg(args, "end_date");

        return """
                【物业费缴费历史】

                您好！缴费历史查询功能正在建设中，暂时无法在线查询。

                如需查询历史缴费记录，您可以：
                1. 拨打物业服务热线：400-888-8888
                2. 前往物业服务中心查询
                """;
    }

    private String getArrearsInfo(Map<String, Object> args) {
        String ownerName = getStringArg(args, "owner_name");

        return """
                【物业费欠费查询】

                您好！欠费查询功能正在建设中，暂时无法在线查询。

                如需查询是否有欠费，您可以：
                1. 拨打物业服务热线：400-888-8888
                2. 前往物业服务中心咨询
                """;
    }

    // ========== 业主信息 ==========

    private String queryOwnerInfo(Map<String, Object> args) {
        try {
            String ownerName = getStringArg(args, "owner_name");
            String phone = getStringArg(args, "phone");

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

                String itemName = item.path("ownerName").asText();
                String itemPhone = item.path("ownerPhone").asText("");

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
                sb.append("  性别：").append(item.path("ownerSex").asText("未知")).append("\n");
                sb.append("  联系电话：").append(itemPhone).append("\n");
                sb.append("  房屋信息：").append(item.path("roomName").asText("未绑定")).append("\n");
                sb.append("\n");
                count++;
            }

            if (count == 0) {
                return "未找到符合条件的业主信息。";
            }

            return sb.toString();
        } catch (Exception e) {
            log.error("查询业主信息失败: {}", e.getMessage());
            return "查询业主信息失败，请稍后重试。";
        }
    }

    private String getOwnerVehicles(Map<String, Object> args) {
        try {
            String ownerName = getStringArg(args, "owner_name");
            if (ownerName == null) {
                return "请提供业主姓名。";
            }

            String result = communityClient.get("/system/owner/list");
            JsonNode root = objectMapper.readTree(result);
            JsonNode data = root.path("rows");

            if (!data.isArray() || data.isEmpty()) {
                return "当前暂无业主记录。";
            }

            for (JsonNode item : data) {
                String itemName = item.path("ownerName").asText();
                if (ownerName.isEmpty() || itemName.contains(ownerName)) {
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

    private String getFamilyMembers(Map<String, Object> args) {
        try {
            String ownerName = getStringArg(args, "owner_name");
            if (ownerName == null) {
                return "请提供业主姓名。";
            }

            String result = communityClient.get("/system/owner/list");
            JsonNode root = objectMapper.readTree(result);
            JsonNode data = root.path("rows");

            if (!data.isArray() || data.isEmpty()) {
                return "当前暂无业主记录。";
            }

            for (JsonNode item : data) {
                String itemName = item.path("ownerName").asText();
                if (ownerName.isEmpty() || itemName.contains(ownerName)) {
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
        } catch (Exception e) {
            log.error("查询家庭成员失败: {}", e.getMessage());
            return "查询家庭成员信息失败，请稍后重试。";
        }
    }

    // ========== 社区公告 ==========

    private String queryAnnouncements(Map<String, Object> args) {
        try {
            String category = getStringArg(args, "category");

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
                sb.append("  类型：").append(formatNoticeType(noticeType)).append("\n");
                sb.append("  内容：").append(item.path("noticeContent").asText()).append("\n");
                sb.append("  发布时间：").append(item.path("createTime").asText()).append("\n");
                sb.append("\n");
                count++;
            }

            if (count == 0) {
                return "未找到符合条件的公告。";
            }

            sb.append("共查询到 ").append(count).append(" 条公告。");
            sb.append("\n如需查看公告详情，请告诉我公告标题。");
            return sb.toString();
        } catch (Exception e) {
            log.error("查询公告失败: {}", e.getMessage());
            return "查询公告失败，请稍后重试。";
        }
    }

    private String getAnnouncementDetail(Map<String, Object> args) {
        try {
            String title = getStringArg(args, "title");
            if (title == null) {
                return "请提供公告标题。";
            }

            String result = communityClient.get("/system/notice/list");
            JsonNode root = objectMapper.readTree(result);
            JsonNode data = root.path("rows");

            if (!data.isArray() || data.isEmpty()) {
                return "当前暂无公告通知。";
            }

            for (JsonNode item : data) {
                String noticeTitle = item.path("noticeTitle").asText();
                if (title.isEmpty() || noticeTitle.contains(title)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("【公告详情】\n\n");
                    sb.append("  标题：").append(noticeTitle).append("\n");
                    sb.append("  类型：").append(formatNoticeType(item.path("noticeType").asText())).append("\n");
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

    private String getActivities(Map<String, Object> args) {
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

    // ========== 社区信息 ==========

    private String queryCommunityInfo(Map<String, Object> args) {
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

    private String getFacilities(Map<String, Object> args) {
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

                【生活设施】
                - 快递驿站：9:00-21:00
                - 便利超市：7:00-23:00
                - 社区餐厅：早餐 7:00-9:00，午餐 11:30-13:30，晚餐 17:30-20:00

                【公共设施】
                - 地下车库：24小时开放
                - 电梯：24小时运行

                如需预约场地或了解更多设施使用规则，请告诉我。
                """;
    }

    private String getNearbyInfo(Map<String, Object> args) {
        String category = getStringArg(args, "category");

        return """
                【周边配套信息】

                【交通配套】
                - 地铁站：2号线XX路站，约800米
                - 公交站：XX路XX路站，多条线路经过

                【教育资源】
                - 幼儿园：XX幼儿园（省级示范），约500米
                - 小学：XX小学（市重点），约1公里

                【医疗资源】
                - 社区医院：约500米
                - 三甲医院：XX医院，约3公里

                【商业配套】
                - 大型超市：XX超市，约1公里
                - 菜市场：约600米

                如需了解更多信息，请告诉我。
                """;
    }

    private String reserveFacility(Map<String, Object> args) {
        String facility = getStringArg(args, "facility");
        String date = getStringArg(args, "date");
        String timeSlot = getStringArg(args, "time_slot");

        if (facility == null || date == null || timeSlot == null) {
            return "请提供设施名称、预约日期和时间段。";
        }

        return String.format("""
                【设施预约】

                您好！设施预约功能正在建设中，暂时无法在线预约。

                如需预约设施，请拨打服务热线：400-888-8888
                或前往物业服务中心（1号楼B1层）办理。

                预约信息：
                设施：%s
                预约日期：%s
                预约时间：%s

                我们将尽快完善在线预约功能，感谢您的理解！
                """, facility, date, timeSlot);
    }

    // ========== 辅助方法 ==========

    private String getStringArg(Map<String, Object> args, String key) {
        Object value = args.get(key);
        return value != null ? value.toString() : null;
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

    private String formatComplaintType(String type) {
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

    private String formatComplaintState(String state) {
        if (state == null) return "未知";
        return switch (state.toLowerCase()) {
            case "pending" -> "待处理";
            case "processing" -> "处理中";
            case "resolved" -> "已解决";
            case "closed" -> "已关闭";
            default -> state;
        };
    }

    private String formatNoticeType(String type) {
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