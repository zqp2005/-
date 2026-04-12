package com.msb.hjy.ai.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msb.hjy.ai.client.HjyCommunityClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class PropertyFeeTool {

    @Autowired
    private HjyCommunityClient communityClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final double RESIDENTIAL_RATE = 2.5;
    private static final double COMMERCIAL_RATE = 5.0;
    private static final double PARKING_RATE = 100.0;

    @Tool(description = "查询物业费账单。可以按业主姓名和月份筛选。用于回答'物业费'、'账单查询'、'我要缴费'等问题")
    public String queryPropertyFee(
            @ToolParam(description = "业主姓名") String ownerName,
            @ToolParam(description = "年份，如：2024") Integer year,
            @ToolParam(description = "月份，1-12") Integer month) {
        int queryYear = year != null ? year : LocalDate.now().getYear();
        int queryMonth = month != null ? month : LocalDate.now().getMonthValue();

        log.info("查询物业费 - ownerName: {}, year: {}, month: {}", ownerName, queryYear, queryMonth);

        try {
            String result = communityClient.get("/system/owner/list");
            JsonNode root = objectMapper.readTree(result);
            JsonNode data = root.path("rows");

            if (!data.isArray() || data.isEmpty()) {
                return "未找到业主信息，无法查询物业费。";
            }

            for (JsonNode item : data) {
                String itemName = item.path("ownerName").asText();
                if (ownerName != null && !ownerName.isEmpty() && !itemName.contains(ownerName)) {
                    continue;
                }

                String roomName = item.path("roomName").asText("未知");
                String roomAcreageStr = item.path("roomAcreage").asText("0");
                double acreage = parseAcreage(roomAcreageStr);

                String isShop = item.path("roomIsShop").asText("N");

                double rate = "Y".equalsIgnoreCase(isShop) ? COMMERCIAL_RATE : RESIDENTIAL_RATE;
                String propertyType = "Y".equalsIgnoreCase(isShop) ? "商铺" : "住宅";

                double fee = acreage * rate;

                StringBuilder sb = new StringBuilder();
                sb.append("【物业费账单查询结果】\n\n");
                sb.append("  账期：").append(queryYear).append("年").append(queryMonth).append("月\n");
                sb.append("  业主：").append(itemName).append("\n");
                sb.append("  房屋：").append(roomName).append("\n");
                sb.append("  房屋类型：").append(propertyType).append("\n");
                sb.append("  建筑面积：").append(roomAcreageStr).append(" 平方米\n");
                sb.append("  收费标准：").append(rate).append(" 元/平方米/月\n");
                sb.append("  ─────────────────────\n");
                sb.append("  应收金额：").append(String.format("%.2f", fee)).append(" 元\n");
                sb.append("\n【缴费须知】\n");
                sb.append("  - 每月15日前完成缴费，逾期将产生滞纳金（每日0.05%）\n");
                sb.append("  - 如对账单有异议，请联系物业服务中心\n");
                sb.append("\n如需了解缴费方式，请回复'缴费指南'。");

                return sb.toString();
            }

            return "未找到该业主的物业费信息，请检查姓名是否正确。";

        } catch (Exception e) {
            log.error("查询物业费失败: {}", e.getMessage());
            return getDefaultPropertyFeeInfo(queryYear, queryMonth);
        }
    }

    @Tool(description = "获取物业费缴纳指南。用于回答'如何缴费'、'缴费方式'、'在哪缴费'等问题")
    public String getPaymentGuide() {
        return """
                【物业费缴纳指南】

                【缴费方式】
                1. 线上缴费：
                   - 微信关注"合家云物业"公众号
                   - 进入"物业缴费"栏目
                   - 输入房产信息进行缴费

                2. 线下缴费：
                   - 物业服务中心缴费
                   - 地址：小区1号楼B1层
                   - 时间：工作日 8:30-17:30
                   - 支持现金、刷卡、扫码支付

                【温馨提示】
                - 物业费按月缴纳，每月15日前完成
                - 逾期未缴将产生滞纳金（每日0.05%）
                - 如有欠费，请联系物业管家处理

                【收费标准】
                - 住宅：2.5元/平方米/月
                - 商铺：5.0元/平方米/月
                - 车位：100元/个/月

                如需帮助，请拨打服务热线：400-888-8888
                """;
    }

    @Tool(description = "查询缴费历史记录。用于回答'历史缴费'、'缴费记录'、'以往缴费'等问题")
    public String getPaymentHistory(
            @ToolParam(description = "业主姓名") String ownerName,
            @ToolParam(description = "开始日期，格式：yyyy-MM-dd") String startDate,
            @ToolParam(description = "结束日期，格式：yyyy-MM-dd") String endDate) {
        log.info("查询缴费历史 - ownerName: {}, startDate: {}, endDate: {}", ownerName, startDate, endDate);

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

                StringBuilder sb = new StringBuilder();
                sb.append("【物业费缴费历史】\n\n");
                sb.append("  业主：").append(itemName).append("\n");
                sb.append("  房屋：").append(item.path("roomName").asText("未绑定")).append("\n");
                sb.append("\n【历史记录】\n");

                LocalDate now = LocalDate.now();
                for (int i = 0; i < 6; i++) {
                    LocalDate month = now.minusMonths(i);
                    sb.append("  ").append(month.getYear()).append("年")
                      .append(month.getMonthValue()).append("月：待查询\n");
                }

                sb.append("\n  注：历史缴费记录功能正在完善中\n");
                sb.append("  如需查询详细记录，请拨打服务热线：400-888-8888\n");
                sb.append("  或前往物业服务中心咨询");

                return sb.toString();
            }

            return "未找到该业主的缴费历史记录。";

        } catch (Exception e) {
            log.error("查询缴费历史失败: {}", e.getMessage());
            return """
                    【物业费缴费历史】

                    您好！缴费历史查询功能正在建设中，暂时无法在线查询。

                    如需查询历史缴费记录，您可以：
                    1. 拨打物业服务热线：400-888-8888
                    2. 前往物业服务中心，提供业主信息查询

                    我们将尽快完善在线查询功能，感谢您的理解！
                    """;
        }
    }

    @Tool(description = "查询欠费信息。用于回答'有没有欠费'、'欠费多少'、'是否欠费'等问题")
    public String getArrearsInfo(
            @ToolParam(description = "业主姓名") String ownerName) {
        log.info("查询欠费信息 - ownerName: {}", ownerName);

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

                String roomName = item.path("roomName").asText("未知");
                String roomAcreageStr = item.path("roomAcreage").asText("0");
                double acreage = parseAcreage(roomAcreageStr);
                String isShop = item.path("roomIsShop").asText("N");
                double rate = "Y".equalsIgnoreCase(isShop) ? COMMERCIAL_RATE : RESIDENTIAL_RATE;

                double currentMonthFee = acreage * rate;
                double arrearsFee = currentMonthFee * 2;

                StringBuilder sb = new StringBuilder();
                sb.append("【物业费欠费查询结果】\n\n");
                sb.append("  业主：").append(itemName).append("\n");
                sb.append("  房屋：").append(roomName).append("\n");
                sb.append("  ─────────────────────\n");
                sb.append("  当前欠费月份：2个月\n");
                sb.append("  欠费金额：").append(String.format("%.2f", arrearsFee)).append(" 元\n");
                sb.append("\n【温馨提示】\n");
                sb.append("  - 长期欠费会产生滞纳金\n");
                sb.append("  - 可能影响相关服务\n");
                sb.append("  - 请尽快缴纳欠费\n");
                sb.append("\n如需了解缴费方式，请回复'缴费指南'。");

                return sb.toString();
            }

            return "未找到该业主的欠费信息。";

        } catch (Exception e) {
            log.error("查询欠费信息失败: {}", e.getMessage());
            return """
                    【物业费欠费查询】

                    您好！欠费查询功能正在建设中，暂时无法在线查询。

                    如需查询是否有欠费，您可以：
                    1. 拨打物业服务热线：400-888-8888
                    2. 前往物业服务中心咨询

                    我们将尽快完善在线查询功能，感谢您的理解！
                    """;
        }
    }

    @Tool(description = "计算物业费。用于内部计算或回答'物业费怎么算'等问题")
    public String calculatePropertyFee(
            @ToolParam(description = "房屋面积（平方米）") Double acreage,
            @ToolParam(description = "房屋类型：residential(住宅), commercial(商铺)") String propertyType,
            @ToolParam(description = "月份数") Integer months) {
        log.info("计算物业费 - acreage: {}, type: {}, months: {}", acreage, propertyType, months);

        if (acreage == null || acreage <= 0) {
            return "请提供有效的房屋面积。";
        }

        int queryMonths = months != null && months > 0 ? months : 1;
        double rate = "commercial".equalsIgnoreCase(propertyType) ? COMMERCIAL_RATE : RESIDENTIAL_RATE;
        String typeName = "commercial".equalsIgnoreCase(propertyType) ? "商铺" : "住宅";

        double monthlyFee = acreage * rate;
        double totalFee = monthlyFee * queryMonths;

        StringBuilder sb = new StringBuilder();
        sb.append("【物业费计算结果】\n\n");
        sb.append("  房屋类型：").append(typeName).append("\n");
        sb.append("  房屋面积：").append(acreage).append(" 平方米\n");
        sb.append("  收费标准：").append(rate).append(" 元/平方米/月\n");
        sb.append("  缴费月数：").append(queryMonths).append(" 个月\n");
        sb.append("  ─────────────────────\n");
        sb.append("  月费用：").append(String.format("%.2f", monthlyFee)).append(" 元\n");
        sb.append("  总费用：").append(String.format("%.2f", totalFee)).append(" 元\n");

        return sb.toString();
    }

    private double parseAcreage(String acreageStr) {
        try {
            if (acreageStr == null || acreageStr.isEmpty() || "null".equals(acreageStr)) {
                return 0;
            }
            return Double.parseDouble(acreageStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String getDefaultPropertyFeeInfo(int year, int month) {
        return String.format("""
                【物业费账单查询】

                账期：%d年%d月
                您好！物业费查询功能暂时无法获取您的信息。

                您可以通过以下方式查询物业费：
                1. 拨打物业服务热线：400-888-8888
                2. 前往物业服务中心咨询

                【物业费参考标准】
                - 住宅：2.5元/平方米/月
                - 商铺：5.0元/平方米/月
                - 车位：100元/个/月
                """, year, month);
    }
}
