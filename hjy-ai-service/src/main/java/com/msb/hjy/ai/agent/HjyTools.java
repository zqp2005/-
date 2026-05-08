package com.msb.hjy.ai.agent;

import com.msb.hjy.ai.tools.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 物业工具集 - 管理所有 AI 可调用的物业工具
 * <p>
 * 作为六大便民工具的聚合器，提供统一的工具注册和调度入口。
 * AI 根据用户意图自动匹配并调用对应的工具方法获取真实数据。
 */
@Slf4j
@Component
public class HjyTools {

    /** 报修服务工具 */
    private final RepairTool repairTool;
    /** 投诉建议工具 */
    private final ComplaintTool complaintTool;
    /** 物业费管理工具 */
    private final PropertyFeeTool propertyFeeTool;
    /** 业主信息工具 */
    private final OwnerInfoTool ownerInfoTool;
    /** 社区公告工具 */
    private final AnnouncementTool announcementTool;
    /** 社区信息工具 */
    private final CommunityTool communityTool;

    /**
     * 构造注入六大工具
     */
    public HjyTools(RepairTool repairTool,
                   ComplaintTool complaintTool,
                   PropertyFeeTool propertyFeeTool,
                   OwnerInfoTool ownerInfoTool,
                   AnnouncementTool announcementTool,
                   CommunityTool communityTool) {
        this.repairTool = repairTool;
        this.complaintTool = complaintTool;
        this.propertyFeeTool = propertyFeeTool;
        this.ownerInfoTool = ownerInfoTool;
        this.announcementTool = announcementTool;
        this.communityTool = communityTool;
        log.info("初始化物业工具集...");
    }

    /**
     * 获取所有可用工具列表（含名称和描述）
     */
    public List<String> getAvailableTools() {
        List<String> tools = new ArrayList<>();
        tools.add("queryRepairOrders - 查询报修工单");
        tools.add("createRepairOrder - 创建报修工单");
        tools.add("getRepairDetail - 获取报修详情");
        tools.add("queryComplaints - 查询投诉列表");
        tools.add("submitComplaint - 提交投诉");
        tools.add("getComplaintDetail - 获取投诉详情");
        tools.add("queryPropertyFee - 查询物业费");
        tools.add("getPaymentGuide - 获取缴费指南");
        tools.add("getPaymentHistory - 获取缴费历史");
        tools.add("getArrearsInfo - 查询欠费信息");
        tools.add("queryOwnerInfo - 查询业主信息");
        tools.add("getOwnerVehicles - 查询车辆信息");
        tools.add("getFamilyMembers - 查询家庭成员");
        tools.add("queryAnnouncements - 查询公告");
        tools.add("getAnnouncementDetail - 获取公告详情");
        tools.add("getActivities - 查询社区活动");
        tools.add("queryCommunityInfo - 查询社区信息");
        tools.add("getFacilities - 查询社区设施");
        tools.add("getNearbyInfo - 查询周边配套");
        tools.add("getFacilitiesReservation - 预约设施");
        return tools;
    }

    /**
     * 执行指定名称的工具
     *
     * @param toolName 工具名称（不区分大小写）
     * @param params   可变参数数组
     * @return 工具执行结果文本
     */
    public String executeTool(String toolName, String... params) {
        log.info("执行工具: {}, 参数: {}", toolName, params);

        try {
            return switch (toolName.toLowerCase()) {
                // 报修相关
                case "queryrepairorders" -> repairTool.queryRepairOrders(
                        getParam(params, 0), getParam(params, 1));
                case "createrepairorder" -> repairTool.createRepairOrder(
                        getParam(params, 0), getParam(params, 1),
                        getParam(params, 2), getParam(params, 3), getParam(params, 4));
                case "getrepairdetail" -> repairTool.getRepairDetail(getParam(params, 0));
                // 投诉相关
                case "querycomplaints" -> complaintTool.queryComplaints(
                        getParam(params, 0), getParam(params, 1));
                case "submitcomplaint" -> complaintTool.submitComplaint(
                        getParam(params, 0), getParam(params, 1),
                        getParam(params, 2), getParam(params, 3), getParam(params, 4));
                case "getcomplaintdetail" -> complaintTool.getComplaintDetail(getParam(params, 0));
                // 物业费相关
                case "querypropertyfee" -> propertyFeeTool.queryPropertyFee(
                        getParam(params, 0),
                        parseInteger(getParam(params, 1)),
                        parseInteger(getParam(params, 2)));
                case "getpaymentguide" -> propertyFeeTool.getPaymentGuide();
                case "getpaymenthistory" -> propertyFeeTool.getPaymentHistory(
                        getParam(params, 0), getParam(params, 1), getParam(params, 2));
                case "getarrearsinfo" -> propertyFeeTool.getArrearsInfo(getParam(params, 0));
                // 业主信息相关
                case "queryownerinfo" -> ownerInfoTool.queryOwnerInfo(
                        getParam(params, 0), getParam(params, 1));
                case "getownervehicles" -> ownerInfoTool.getOwnerVehicles(getParam(params, 0));
                case "getfamilymembers" -> ownerInfoTool.getFamilyMembers(getParam(params, 0));
                // 公告相关
                case "queryannouncements" -> announcementTool.queryAnnouncements(getParam(params, 0));
                case "getannouncementdetail" -> announcementTool.getAnnouncementDetail(getParam(params, 0));
                case "getactivities" -> announcementTool.getActivities(getParam(params, 0));
                // 社区信息相关
                case "querycommunityinfo" -> communityTool.queryCommunityInfo(getParam(params, 0));
                case "getfacilities" -> communityTool.getFacilities();
                case "getnearbyinfo" -> communityTool.getNearbyInfo(getParam(params, 0));
                case "getfacilitiesreservation" -> communityTool.getFacilitiesReservation(
                        getParam(params, 0), getParam(params, 1), getParam(params, 2));
                default -> "未知工具: " + toolName;
            };
        } catch (Exception e) {
            log.error("工具执行失败: {}", e.getMessage(), e);
            return "工具执行失败: " + e.getMessage();
        }
    }

    /**
     * 安全获取可变参数指定位的值
     *
     * @param params 参数数组
     * @param index  索引位置
     * @return 参数值，不存在则返回空字符串
     */
    private String getParam(String[] params, int index) {
        return params != null && index < params.length && params[index] != null ? params[index] : "";
    }

    /**
     * 将字符串解析为整数
     *
     * @param value 字符串值
     * @return 解析后的 Integer，解析失败返回 null
     */
    private Integer parseInteger(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
