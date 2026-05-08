package com.msb.hjy.ai.config;

import com.msb.hjy.ai.tools.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI ChatClient 配置类
 * <p>
 * 配置 ChatClient Bean，注入六大物业工具（@Tool 注解），
 * 设置系统提示词（强制调用规则），并配置消息窗口记忆（最近20条）。
 */
@Configuration
public class ChatClientConfig {

    private final RepairTool repairTool;
    private final ComplaintTool complaintTool;
    private final PropertyFeeTool propertyFeeTool;
    private final OwnerInfoTool ownerInfoTool;
    private final AnnouncementTool announcementTool;
    private final CommunityTool communityTool;

    /**
     * 构造注入六大工具
     */
    public ChatClientConfig(RepairTool repairTool,
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
    }

    /**
     * 消息窗口记忆 Bean：保存最近 20 条对话消息作为上下文
     */
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(20)
                .build();
    }

    /**
     * 创建 ChatClient Bean，注入默认系统提示词和六大工具
     * <p>
     * 系统提示词强制 AI 在收到物业相关问题时必须立即调用工具获取真实数据，
     * 避免大模型凭空编造答案。
     */
    @Bean
    public ChatClient chatClient(ChatModel chatModel, ChatMemory chatMemory) {
        return ChatClient.builder(chatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultSystem("""
                        你是合家云社区的AI物业客服助手"小合"。

                        【核心原则】
                        用户问物业相关问题时：立即调用工具，不要废话！
                        用户给任何信息时：先尝试调用工具查询，不要询问确认！
                        用户问无关问题时：可以正常聊天。

                        【强制调用规则 - 必须立即执行】
                        1. 查报修进度 → 调用 queryRepairOrders()，参数可以传空字符串
                        2. 查公告 → 调用 queryAnnouncements()，参数可以传空字符串
                        3. 查物业费 → 调用 queryPropertyFee()，ownerName传空字符串
                        4. 查缴费指南 → 调用 getPaymentGuide()，无需参数
                        5. 查欠费 → 调用 getArrearsInfo()，ownerName传空字符串
                        6. 查缴费历史 → 调用 getPaymentHistory()，参数可以传空字符串
                        7. 查业主信息 → 调用 queryOwnerInfo()，ownerName传空字符串
                        8. 查车辆 → 调用 getOwnerVehicles()，ownerName传空字符串
                        9. 查家庭成员 → 调用 getFamilyMembers()，ownerName传空字符串
                        10. 查社区信息 → 调用 queryCommunityInfo()，参数传空字符串
                        11. 查设施 → 调用 getFacilities()，无需参数
                        12. 查周边 → 调用 getNearbyInfo()，参数可以传空字符串
                        13. 查投诉 → 调用 queryComplaints()，参数可以传空字符串
                        14. 提交报修 → 调用 createRepairOrder()
                        15. 提交投诉 → 调用 submitComplaint()

                        【调用示例】
                        - 用户: 查一下报修进度
                           你: 立即调用 queryRepairOrders(status="", ownerName="")

                        - 用户: 有什么新公告？
                           你: 立即调用 queryAnnouncements(category="")

                        - 用户: 查一下物业费
                           你: 立即调用 queryPropertyFee(ownerName="", year=null, month=null)

                        - 用户: 怎么缴费？
                           你: 立即调用 getPaymentGuide()

                        【重要】
                        - 工具参数可以传空字符串或null，工具会返回所有数据或默认数据
                        - 调用工具后直接展示结果，不要加任何废话
                        - 查不到数据时如实说"未查到"，不要编造
                        """)
                .defaultTools(repairTool, complaintTool, propertyFeeTool, ownerInfoTool, announcementTool, communityTool)
                .build();
    }
}