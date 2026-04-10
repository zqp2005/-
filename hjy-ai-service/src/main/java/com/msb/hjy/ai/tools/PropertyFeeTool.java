package com.msb.hjy.ai.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
public class PropertyFeeTool {

    public String queryPropertyFee(String ownerName, String building, String unit, String room, Integer year, Integer month) {
        int currentYear = year != null ? year : LocalDate.now().getYear();
        int currentMonth = month != null ? month : LocalDate.now().getMonthValue();

        log.info("查询物业费 - ownerName: {}, year: {}, month: {}", ownerName, currentYear, currentMonth);

        return """
                【物业费账单查询】

                您好！物业费查询功能正在建设中，暂时无法直接在线查询。

                您可以通过以下方式查询物业费：
                1. 拨打物业服务热线：400-888-8888
                2. 前往物业服务中心咨询（工作日 8:30-17:30）
                3. 关注"合家云物业"微信公众号，在线查询

                【物业费参考标准】
                - 住宅：2.5元/平方米/月
                - 商铺：5.0元/平方米/月
                - 车位：100元/个/月

                如需了解更多，请告诉我！
                """;
    }

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

    public String getPaymentHistory(String ownerName, String startDate, String endDate) {
        log.info("查询缴费历史 - ownerName: {}, startDate: {}, endDate: {}", ownerName, startDate, endDate);

        return """
                【物业费缴费历史】

                您好！缴费历史查询功能正在建设中，暂时无法在线查询。

                如需查询历史缴费记录，您可以：
                1. 拨打物业服务热线：400-888-8888
                2. 前往物业服务中心，提供业主信息查询

                我们将尽快完善在线查询功能，感谢您的理解！
                """;
    }

    public String getArrearsInfo(String ownerName) {
        log.info("查询欠费信息 - ownerName: {}", ownerName);

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
