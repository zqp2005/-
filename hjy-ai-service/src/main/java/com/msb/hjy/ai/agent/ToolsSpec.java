package com.msb.hjy.ai.agent;

import com.alibaba.dashscope.tools.FunctionDefinition;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Arrays;
import java.util.List;

public class ToolsSpec {

    private static final Gson gson = new Gson();

    public static final List<FunctionDefinition> TOOLS = Arrays.asList(
            // ========== 报修服务 ==========
            FunctionDefinition.builder()
                    .name("query_repair_orders")
                    .description("查询业主的报修工单列表，可以按状态或业主名筛选。用于回答'报修进度'、'我的报修'等问题")
                    .parameters(createRepairQueryParams())
                    .build(),

            FunctionDefinition.builder()
                    .name("create_repair_order")
                    .description("创建新的报修工单。用于回答'我要报修'、'提交报修'等问题，需要业主姓名、联系电话、报修位置和问题描述")
                    .parameters(createRepairCreateParams())
                    .build(),

            FunctionDefinition.builder()
                    .name("get_repair_detail")
                    .description("获取报修工单详情。用于回答'报修详情'、'工单号xxx'等问题")
                    .parameters(createRepairDetailParams())
                    .build(),

            // ========== 投诉建议 ==========
            FunctionDefinition.builder()
                    .name("query_complaints")
                    .description("查询投诉建议列表，可以按状态或类型筛选。用于回答'投诉进度'、'我的建议'等问题")
                    .parameters(createComplaintQueryParams())
                    .build(),

            FunctionDefinition.builder()
                    .name("submit_complaint")
                    .description("提交投诉建议。用于回答'我要投诉'、'提建议'等问题，需要投诉人信息、类型和内容")
                    .parameters(createComplaintSubmitParams())
                    .build(),

            FunctionDefinition.builder()
                    .name("get_complaint_detail")
                    .description("获取投诉建议详情。用于回答'投诉详情'、'建议详情'等问题")
                    .parameters(createComplaintDetailParams())
                    .build(),

            // ========== 物业费 ==========
            FunctionDefinition.builder()
                    .name("query_property_fee")
                    .description("查询物业费账单。可以按年份、月份筛选。用于回答'物业费'、'账单查询'、'缴费'等问题")
                    .parameters(createPropertyFeeParams())
                    .build(),

            FunctionDefinition.builder()
                    .name("get_payment_guide")
                    .description("获取物业费缴纳指南。用于回答'如何缴费'、'缴费方式'等问题")
                    .parameters(createEmptyParams())
                    .build(),

            FunctionDefinition.builder()
                    .name("get_payment_history")
                    .description("查询缴费历史记录。用于回答'历史缴费'、'缴费记录'等问题")
                    .parameters(createPaymentHistoryParams())
                    .build(),

            FunctionDefinition.builder()
                    .name("get_arrears_info")
                    .description("查询欠费信息。用于回���'有没有欠费'、'欠费多少'等问题")
                    .parameters(createArrearsParams())
                    .build(),

            // ========== 业主信息 ==========
            FunctionDefinition.builder()
                    .name("query_owner_info")
                    .description("查询业主信息。用于回答'我的信息'、'业主信息'等问题")
                    .parameters(createOwnerInfoParams())
                    .build(),

            FunctionDefinition.builder()
                    .name("get_owner_vehicles")
                    .description("查询业主车辆信息。用于回答'车辆信息'、'车牌号'等问题")
                    .parameters(createOwnerVehicleParams())
                    .build(),

            FunctionDefinition.builder()
                    .name("get_family_members")
                    .description("查询家庭成员信息。用于回答'家庭成员'、'家人信息'等问题")
                    .parameters(createFamilyMembersParams())
                    .build(),

            // ========== 社区公告 ==========
            FunctionDefinition.builder()
                    .name("query_announcements")
                    .description("查询社区公告列表，可以按类型筛选。用于回答'公告'、'通知'、'社区最新消息'等问题")
                    .parameters(createAnnouncementParams())
                    .build(),

            FunctionDefinition.builder()
                    .name("get_announcement_detail")
                    .description("获取公告详情。用于回答查看具体公告内容")
                    .parameters(createAnnouncementDetailParams())
                    .build(),

            FunctionDefinition.builder()
                    .name("get_activities")
                    .description("查询社区活动。用于回答'有什么活动'、'社区活动'等问题")
                    .parameters(createActivityParams())
                    .build(),

            // ========== 社区信息 ==========
            FunctionDefinition.builder()
                    .name("query_community_info")
                    .description("查询社区基本信息。用于回答'小区介绍'、'社区信息'等问题")
                    .parameters(createCommunityInfoParams())
                    .build(),

            FunctionDefinition.builder()
                    .name("get_facilities")
                    .description("查询社区设施。用于回答'有什么设施'、'健身房'、'游泳池'等问题")
                    .parameters(createEmptyParams())
                    .build(),

            FunctionDefinition.builder()
                    .name("get_nearby_info")
                    .description("查询周边配套信息。用于回答'周边有什么'、'附近配套'、'地铁站'等问题")
                    .parameters(createNearbyParams())
                    .build(),

            FunctionDefinition.builder()
                    .name("reserve_facility")
                    .description("预约社区设施。用于回答'预约场地'、'预约设施'等问题")
                    .parameters(createReserveParams())
                    .build()
    );

    // ========== 参数定义方法 ==========

    private static JsonObject createEmptyParams() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "object");
        obj.add("properties", new com.google.gson.JsonObject());
        return obj;
    }

    private static JsonObject createRepairQueryParams() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "object");

        JsonObject properties = new JsonObject();
        properties.add("status", createStringProperty("报修状态筛选：pending(待处理), allocated(已派单), processing(处理中), completed(已完成), rated(已评价)"));
        properties.add("owner_name", createStringProperty("业主姓名，用于筛选特定业主的报修单"));

        obj.add("properties", properties);
        return obj;
    }

    private static JsonObject createRepairCreateParams() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "object");

        JsonObject properties = new JsonObject();
        properties.add("owner_name", createStringProperty("业主姓名，必填"));
        properties.add("phone", createStringProperty("联系电话，必填"));
        properties.add("location", createStringProperty("报修位置，如：A栋101"));
        properties.add("problem", createStringProperty("问题描述，必填"));
        properties.add("category", createStringProperty("报修类别：water(水电), facility(设施), door(门锁), other(其他)"));

        obj.add("properties", properties);
        obj.add("required", gson.toJsonTree(Arrays.asList("owner_name", "phone", "problem")));
        return obj;
    }

    private static JsonObject createRepairDetailParams() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "object");

        JsonObject properties = new JsonObject();
        properties.add("repair_id", createStringProperty("报修工单号"));

        obj.add("properties", properties);
        obj.add("required", gson.toJsonTree(Arrays.asList("repair_id")));
        return obj;
    }

    private static JsonObject createComplaintQueryParams() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "object");

        JsonObject properties = new JsonObject();
        properties.add("status", createStringProperty("状态筛选：pending(待处理), processing(处理中), resolved(已解决), closed(已关闭)"));
        properties.add("type", createStringProperty("类型筛选：service(服务态度), sanitation(环境卫生), facility(设施设备), noise(噪音), other(其他)"));

        obj.add("properties", properties);
        return obj;
    }

    private static JsonObject createComplaintSubmitParams() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "object");

        JsonObject properties = new JsonObject();
        properties.add("owner_name", createStringProperty("投诉人姓名，必填"));
        properties.add("phone", createStringProperty("联系电话"));
        properties.add("type", createStringProperty("投诉类型：service, sanitation, facility, noise, other，必填"));
        properties.add("content", createStringProperty("投诉内容，必填"));
        properties.add("location", createStringProperty("投诉地点"));

        obj.add("properties", properties);
        obj.add("required", gson.toJsonTree(Arrays.asList("owner_name", "type", "content")));
        return obj;
    }

    private static JsonObject createComplaintDetailParams() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "object");

        JsonObject properties = new JsonObject();
        properties.add("complaint_id", createStringProperty("投诉建议编号"));

        obj.add("properties", properties);
        obj.add("required", gson.toJsonTree(Arrays.asList("complaint_id")));
        return obj;
    }

    private static JsonObject createPropertyFeeParams() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "object");

        JsonObject properties = new JsonObject();
        properties.add("owner_name", createStringProperty("业主姓名"));
        properties.add("year", createIntegerProperty("年份，如：2024"));
        properties.add("month", createIntegerProperty("月份，1-12"));

        obj.add("properties", properties);
        return obj;
    }

    private static JsonObject createPaymentHistoryParams() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "object");

        JsonObject properties = new JsonObject();
        properties.add("owner_name", createStringProperty("业主姓名"));
        properties.add("start_date", createStringProperty("开始日期，格式：yyyy-MM-dd"));
        properties.add("end_date", createStringProperty("结束日期，格式：yyyy-MM-dd"));

        obj.add("properties", properties);
        return obj;
    }

    private static JsonObject createArrearsParams() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "object");

        JsonObject properties = new JsonObject();
        properties.add("owner_name", createStringProperty("业主姓名"));

        obj.add("properties", properties);
        return obj;
    }

    private static JsonObject createOwnerInfoParams() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "object");

        JsonObject properties = new JsonObject();
        properties.add("owner_name", createStringProperty("业主姓名"));
        properties.add("phone", createStringProperty("联系电话"));

        obj.add("properties", properties);
        return obj;
    }

    private static JsonObject createOwnerVehicleParams() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "object");

        JsonObject properties = new JsonObject();
        properties.add("owner_name", createStringProperty("业主姓名"));

        obj.add("properties", properties);
        return obj;
    }

    private static JsonObject createFamilyMembersParams() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "object");

        JsonObject properties = new JsonObject();
        properties.add("owner_name", createStringProperty("业主姓名"));

        obj.add("properties", properties);
        return obj;
    }

    private static JsonObject createAnnouncementParams() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "object");

        JsonObject properties = new JsonObject();
        properties.add("category", createStringProperty("公告类型：notice(通知), announcement(公告), activity(活动), news(新闻)"));

        obj.add("properties", properties);
        return obj;
    }

    private static JsonObject createAnnouncementDetailParams() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "object");

        JsonObject properties = new JsonObject();
        properties.add("title", createStringProperty("公告标题"));

        obj.add("properties", properties);
        obj.add("required", gson.toJsonTree(Arrays.asList("title")));
        return obj;
    }

    private static JsonObject createActivityParams() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "object");

        JsonObject properties = new JsonObject();
        properties.add("status", createStringProperty("活动状态：upcoming(即将开始), ongoing(进行中), ended(已结束)"));

        obj.add("properties", properties);
        return obj;
    }

    private static JsonObject createCommunityInfoParams() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "object");

        JsonObject properties = new JsonObject();
        properties.add("info_type", createStringProperty("信息类型：basic(基本信息), property(物业信息), contact(联系方式)"));

        obj.add("properties", properties);
        return obj;
    }

    private static JsonObject createNearbyParams() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "object");

        JsonObject properties = new JsonObject();
        properties.add("category", createStringProperty("配套类别：traffic(交通), education(教育), medical(医疗), shopping(商业), park(公园)"));

        obj.add("properties", properties);
        return obj;
    }

    private static JsonObject createReserveParams() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "object");

        JsonObject properties = new JsonObject();
        properties.add("facility", createStringProperty("设施名称，如：篮球场、健身房"));
        properties.add("date", createStringProperty("预约日期，格式：yyyy-MM-dd"));
        properties.add("time_slot", createStringProperty("时间段，如：14:00-16:00"));
        properties.add("owner_name", createStringProperty("预约人姓名"));
        properties.add("phone", createStringProperty("联系电话"));

        obj.add("properties", properties);
        obj.add("required", gson.toJsonTree(Arrays.asList("facility", "date", "time_slot")));
        return obj;
    }

    // ========== 辅助方法 ==========

    private static JsonObject createStringProperty(String description) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "string");
        obj.addProperty("description", description);
        return obj;
    }

    private static JsonObject createIntegerProperty(String description) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "integer");
        obj.addProperty("description", description);
        return obj;
    }

    public static final String SYSTEM_PROMPT = """
            你是一位专业、热情的合家云社区物业客服助手，名为"小合"。

            【身份定位】
            - 你是合家云社区的AI客服代表
            - 你的职责是帮助业主解决日常问题，提供优质的服务体验
            - 你需要以友善、耐心的态度回应每一位业主

            【核心原则 - 非常重要】
            - 当用户询问"报修进度"、"报修情况"、"我的报修"时，必须立即调用 query_repair_orders 工具
            - 当用户要"提交报修"时，必须立即调用 create_repair_order 工具
            - 当用户询问"物业费"、"账单"、"缴费"时，必须立即调用 query_property_fee 工具
            - 当用户询问"如何缴费"、"缴费方式"时，必须立即调用 get_payment_guide 工具
            - 当用户询问"历史缴费"时，必须立即调用 get_payment_history 工具
            - 当用户询问"是否欠费"时，必须立即调用 get_arrears_info 工具
            - 当用户询问"公告"、"通知"时，必须立即调用 query_announcements 工具
            - 当用户询问"社区活动"时，必须立即调用 get_activities 工具
            - 当用户询问"业主信息"、"我的信息"时，必须立即调用 query_owner_info 工具
            - 当用户询问"车辆"信息时，必须立即调用 get_owner_vehicles 工具
            - 当用户询问"家庭成员"时，必须立即调用 get_family_members 工具
            - 当用户需要"预约设施"时，必须立即调用 reserve_facility 工具
            - 当用户询问"社区设施"时，必须立即调用 get_facilities 工具
            - 当用户询问"周边配套"时，必须立即调用 get_nearby_info 工具
            - 当用户提交"投诉"、"建议"时，必须立即调用 submit_complaint 工具
            - **绝对不要**在没有调用工具的情况下编造任何具体数据（如工单号、金额、状态等）
            - 如果用户没有提供足够的信息来调用工具，先尝试调用工具查询

            【可用工具 - 必须使用】
            报修服务：query_repair_orders, create_repair_order, get_repair_detail
            投诉建议：query_complaints, submit_complaint, get_complaint_detail
            物业费：query_property_fee, get_payment_guide, get_payment_history, get_arrears_info
            业主信息：query_owner_info, get_owner_vehicles, get_family_members
            社区公告：query_announcements, get_announcement_detail, get_activities
            社区信息：query_community_info, get_facilities, get_nearby_info, reserve_facility

            【回复格式】
            - 调用工具获取数据后，用友好的方式呈现结果
            - 不要说"我为您查询了一下"，直接展示查询结果
            - 如果工具返回空数据，友好地告知用户

            【社区信息】
            - 社区名称：合家云社区
            - 服务热线：400-888-8888
            - 服务时间：24小时在线
            """;
}