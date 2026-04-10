package com.msb.hjy.ai.agent;

import com.alibaba.dashscope.tools.FunctionDefinition;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Arrays;
import java.util.List;

public class ToolsSpec {

    private static final Gson gson = new Gson();

    public static final List<FunctionDefinition> TOOLS = Arrays.asList(
            FunctionDefinition.builder()
                    .name("query_repair_orders")
                    .description("查询业主的报修工单列表，可以按状态或业主名筛选")
                    .parameters(createRepairParams())
                    .build(),

            FunctionDefinition.builder()
                    .name("create_complaint")
                    .description("提交业主投诉或建议")
                    .parameters(createComplaintParams())
                    .build(),

            FunctionDefinition.builder()
                    .name("query_property_fee")
                    .description("查询物业费账单")
                    .parameters(createPropertyFeeParams())
                    .build(),

            FunctionDefinition.builder()
                    .name("query_announcements")
                    .description("查询社区公告列表")
                    .parameters(createAnnouncementParams())
                    .build(),

            FunctionDefinition.builder()
                    .name("query_owner_info")
                    .description("查询业主信息")
                    .parameters(createOwnerInfoParams())
                    .build()
    );

    private static JsonObject createRepairParams() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "object");

        JsonObject properties = new JsonObject();
        JsonObject status = new JsonObject();
        status.addProperty("type", "string");
        status.addProperty("description", "报修状态：pending(待处理), allocated(已派单), processing(处理中), completed(已完成), rated(已评价)");
        properties.add("status", status);

        JsonObject ownerName = new JsonObject();
        ownerName.addProperty("type", "string");
        ownerName.addProperty("description", "业主姓名（可选，用于筛选特定业主的报修单）");
        properties.add("owner_name", ownerName);

        obj.add("properties", properties);
        return obj;
    }

    private static JsonObject createComplaintParams() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "object");

        JsonObject properties = new JsonObject();

        JsonObject ownerName = new JsonObject();
        ownerName.addProperty("type", "string");
        ownerName.addProperty("description", "业主姓名");
        properties.add("owner_name", ownerName);

        JsonObject phone = new JsonObject();
        phone.addProperty("type", "string");
        phone.addProperty("description", "联系电话");
        properties.add("phone", phone);

        JsonObject content = new JsonObject();
        content.addProperty("type", "string");
        content.addProperty("description", "投诉或建议内容");
        properties.add("content", content);

        JsonObject category = new JsonObject();
        category.addProperty("type", "string");
        category.addProperty("description", "投诉类型：service(服务态度), sanitation(环境卫生), facility(设施设备), noise(噪音扰民), other(其他)");
        properties.add("category", category);

        obj.add("properties", properties);

        return obj;
    }

    private static JsonObject createPropertyFeeParams() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "object");

        JsonObject properties = new JsonObject();

        JsonObject ownerName = new JsonObject();
        ownerName.addProperty("type", "string");
        ownerName.addProperty("description", "业主姓名");
        properties.add("owner_name", ownerName);

        JsonObject houseId = new JsonObject();
        houseId.addProperty("type", "string");
        houseId.addProperty("description", "房屋ID");
        properties.add("house_id", houseId);

        obj.add("properties", properties);
        return obj;
    }

    private static JsonObject createAnnouncementParams() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "object");

        JsonObject properties = new JsonObject();

        JsonObject title = new JsonObject();
        title.addProperty("type", "string");
        title.addProperty("description", "公告标题关键词（可选）");
        properties.add("title", title);

        obj.add("properties", properties);
        return obj;
    }

    private static JsonObject createOwnerInfoParams() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "object");

        JsonObject properties = new JsonObject();

        JsonObject ownerName = new JsonObject();
        ownerName.addProperty("type", "string");
        ownerName.addProperty("description", "业主姓名");
        properties.add("owner_name", ownerName);

        JsonObject phone = new JsonObject();
        phone.addProperty("type", "string");
        phone.addProperty("description", "联系电话");
        properties.add("phone", phone);

        obj.add("properties", properties);
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
        - 当用户询问"物业费"、"账单"、"缴费"时，必须立即调用 query_property_fee 工具
        - 当用户询问"公告"、"通知"、"社区活动"时，必须立即调用 query_announcements 工具
        - 当用户询问"业主信息"、"我的信息"时，必须立即调用 query_owner_info 工具
        - 当用户提交"投诉"、"建议"时，必须立即调用 create_complaint 工具
        - **绝对不要**在没有调用工具的情况下编造任何具体数据（如工单号、金额、状态等）
        - 如果用户没有提供足够的信息来调用工具，可以在提示用户提供信息之前先尝试调用工具查询

        【可用工具 - 必须使用】
        - query_repair_orders: 查询报修工单（参数：status, owner_name）
        - query_property_fee: 查询物业费账单（参数：owner_name, house_id）
        - query_announcements: 查询社区公告（参数：title）
        - query_owner_info: 查询业主信息（参数：owner_name, phone）
        - create_complaint: 提交投诉建议（参数：owner_name, phone, content, category）

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
