package com.msb.hjy.ai.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/** 未接入账单前不推算实际欠费，不虚构费率、电话或支付入口。 */
@Component
public class PropertyFeeTool {
    private static final String UNAVAILABLE = "尚未接入真实收费规则、账单和收款记录，无法查询应缴金额、欠费或缴费历史。请联系本小区物业核实；无数据不代表无欠费。";
    @Tool(description = "说明物业费账单查询的实际可用情况")
    public String queryPropertyFee(@ToolParam(description = "姓名") String ownerName,
            @ToolParam(description = "年份") Integer year, @ToolParam(description = "月份") Integer month) { return UNAVAILABLE; }
    @Tool(description = "说明欠费查询的实际可用情况")
    public String getArrearsInfo(@ToolParam(description = "姓名") String ownerName) { return UNAVAILABLE; }
    @Tool(description = "说明缴费历史查询的实际可用情况")
    public String getPaymentHistory(@ToolParam(description = "姓名") String ownerName,
            @ToolParam(description = "起始日期") String startDate, @ToolParam(description = "截止日期") String endDate) { return UNAVAILABLE; }
    @Tool(description = "提供缴费渠道核实提示，不提供未经配置的支付入口")
    public String getPaymentGuide() { return "请向本小区物业核实正式账单和收款渠道；系统尚未接入在线支付，不要向未经核实的个人账户转账。"; }
    @Tool(description = "解释计算公式；未配置费率时不计算实际应缴金额")
    public String calculatePropertyFee(@ToolParam(description = "面积") Double acreage,
            @ToolParam(description = "房屋类型") String propertyType, @ToolParam(description = "月数") Integer months) {
        return "按面积按月计费的示例公式为：计费面积 × 经确认的每平方米月费率 × 月数。当前未配置实际费率或计费规则，不能据此生成应缴金额或欠费。";
    }
}
