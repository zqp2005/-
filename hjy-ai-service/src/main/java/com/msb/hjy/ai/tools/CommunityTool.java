package com.msb.hjy.ai.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CommunityTool {

    public String queryCommunityInfo(String infoType) {
        log.info("查询社区信息 - infoType: {}", infoType);

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

    public String getFacilities() {
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

    public String getNearbyInfo(String category) {
        log.info("查询周边配套 - category: {}", category);

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

    public String getFacilitiesReservation(String facility, String date, String timeSlot) {
        log.info("预约设施 - facility: {}, date: {}, timeSlot: {}", facility, date, timeSlot);

        return String.format("""
                【设施预约】

                您好！设施预约功能正在建设中，暂时无法在线预约。

                如需预约设施，请拨打服务热线：400-888-8888
                或前往物业服务中心（1号楼B1层）办理。

                我们将尽快完善在线预约功能，感谢您的理解！

                预约信息：
                设施：%s
                预约日期：%s
                预约时间：%s
                """, facility != null ? facility : "未指定",
                date != null ? date : "未指定",
                timeSlot != null ? timeSlot : "未指定");
    }
}
