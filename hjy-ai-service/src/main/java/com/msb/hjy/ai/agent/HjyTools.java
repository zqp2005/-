package com.msb.hjy.ai.agent;

import com.msb.hjy.ai.tools.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class HjyTools {

    private final RepairTool repairTool;
    private final ComplaintTool complaintTool;
    private final PropertyFeeTool propertyFeeTool;
    private final OwnerInfoTool ownerInfoTool;
    private final AnnouncementTool announcementTool;
    private final CommunityTool communityTool;

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

    public String executeTool(String toolName, String... params) {
        log.info("执行工具: {}, 参数: {}", toolName, params);

        try {
            return switch (toolName.toLowerCase()) {
                case "queryrepairorders" -> repairTool.queryRepairOrders(
                        getParam(params, 0), getParam(params, 1));
                case "createrepairorder" -> repairTool.createRepairOrder(
                        getParam(params, 0), getParam(params, 1),
                        getParam(params, 2), getParam(params, 3), getParam(params, 4));
                case "getrepairdetail" -> repairTool.getRepairDetail(getParam(params, 0));
                case "querycomplaints" -> complaintTool.queryComplaints(
                        getParam(params, 0), getParam(params, 1));
                case "submitcomplaint" -> complaintTool.submitComplaint(
                        getParam(params, 0), getParam(params, 1),
                        getParam(params, 2), getParam(params, 3), getParam(params, 4));
                case "getcomplaintdetail" -> complaintTool.getComplaintDetail(getParam(params, 0));
                case "querypropertyfee" -> propertyFeeTool.queryPropertyFee(
                        getParam(params, 0), getParam(params, 1),
                        getParam(params, 2), getParam(params, 3),
                        parseInteger(getParam(params, 4)),
                        parseInteger(getParam(params, 5)));
                case "getpaymentguide" -> propertyFeeTool.getPaymentGuide();
                case "getpaymenthistory" -> propertyFeeTool.getPaymentHistory(
                        getParam(params, 0), getParam(params, 1), getParam(params, 2));
                case "getarrearsinfo" -> propertyFeeTool.getArrearsInfo(getParam(params, 0));
                case "queryownerinfo" -> ownerInfoTool.queryOwnerInfo(
                        getParam(params, 0), getParam(params, 1));
                case "getownervehicles" -> ownerInfoTool.getOwnerVehicles(getParam(params, 0));
                case "getfamilymembers" -> ownerInfoTool.getFamilyMembers(getParam(params, 0));
                case "queryannouncements" -> announcementTool.queryAnnouncements(getParam(params, 0));
                case "getannouncementdetail" -> announcementTool.getAnnouncementDetail(getParam(params, 0));
                case "getactivities" -> announcementTool.getActivities(getParam(params, 0));
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

    private String getParam(String[] params, int index) {
        return params != null && index < params.length && params[index] != null ? params[index] : "";
    }

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
