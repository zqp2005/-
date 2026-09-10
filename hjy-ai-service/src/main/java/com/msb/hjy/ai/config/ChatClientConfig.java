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
 * 设置系统提示词（从 resources/prompt/system-prompt.txt 加载，强制调用规则），
 * 并配置消息窗口记忆（最近20条，Redis 持久化）。
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
     * <p>
     * 存储为 Redis（{@link RedisChatMemoryRepository}），服务重启后历史不丢、多实例可共享。
     */
    @Bean
    public ChatMemory chatMemory(RedisChatMemoryRepository repository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(repository)
                .maxMessages(20)
                .build();
    }

    /**
     * 创建 ChatClient Bean，注入默认系统提示词和六大工具
     * <p>
     * 系统提示词强制 AI 在收到物业相关问题时必须立即调用工具获取真实数据，
     * 避免大模型凭空编造答案。提示词文本外置于 resources/prompt/system-prompt.txt。
     */
    @Bean
    public ChatClient chatClient(ChatModel chatModel, ChatMemory chatMemory) {
        return ChatClient.builder(chatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultSystem(loadSystemPrompt())
                .defaultTools(repairTool, complaintTool, propertyFeeTool, ownerInfoTool, announcementTool, communityTool)
                .build();
    }

    /**
     * 从 classpath 加载系统提示词文本
     */
    private String loadSystemPrompt() {
        try (java.io.InputStream in = getClass().getResourceAsStream("/prompt/system-prompt.txt")) {
            if (in == null) {
                throw new IllegalStateException("未找到系统提示词文件 /prompt/system-prompt.txt");
            }
            return new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (java.io.IOException e) {
            throw new IllegalStateException("读取系统提示词失败", e);
        }
    }
}