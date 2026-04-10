package com.msb.hjy.ai.prompt;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
public class PromptTemplate {

    public static final String GREETING = "您好！我是合家云社区的AI助手小合，很高兴为您服务。请问有什么可以帮助您的？";

    public static final String HELP_PROMPT = """
            我可以帮您：
            1. 报修服务 - 提交报修、查询进度
            2. 投诉建议 - 提交投诉、查询处理进度
            3. 物业费用 - 查询账单、缴费指南
            4. 社区信息 - 公告、活动、设施
            5. 业主服务 - 个人信息、车辆管理等
            
            请告诉我您的需求！
            """;

    public static String buildRepairPrompt(String problem) {
        return String.format("""
                业主咨询报修问题：%s
                
                请根据问题类型提供以下信息：
                1. 确认问题类别（水暖、电工、门锁、家电、其他）
                2. 了解详细情况
                3. 提供报修指引
                4. 如可查询，展示相关报修单状态
                """, problem);
    }

    public static String buildFeePrompt(String period) {
        return String.format("""
                业主查询物业费信息：%s
                
                请提供：
                1. 当期账单详情
                2. 缴费状态
                3. 缴费方式和指引
                4. 如有欠费，说明情况并提供建议
                """, period);
    }

    public static String buildActivityPrompt(String type) {
        return String.format("""
                业主查询社区活动：%s
                
                请提供：
                1. 相关活动列表
                2. 活动详情和时间
                3. 报名方式（如需要）
                4. 注意事项
                """, type);
    }

    public static String buildUnknownPrompt() {
        return """
                抱歉，我暂时无法理解您的问题。
                
                您可以：
                1. 换个方式描述您的问题
                2. 告诉我您需要的具体服务类型
                3. 拨打服务热线：400-888-8888 转人工服务
                
                我很乐意为您服务！
                """;
    }

    public static String buildDataAnalysisPrompt(List<String> dataTypes) {
        StringBuilder sb = new StringBuilder("请分析以下数据：\n");
        for (int i = 0; i < dataTypes.size(); i++) {
            sb.append(i + 1).append(". ").append(dataTypes.get(i)).append("\n");
        }
        sb.append("\n请提供：\n");
        sb.append("1. 数据概览和关键指标\n");
        sb.append("2. 趋势分析\n");
        sb.append("3. 潜在问题和改进建议\n");
        return sb.toString();
    }
}
