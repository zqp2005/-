package com.msb.hjy.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * AI 服务配置属性类
 * <p>
 * 映射 application.yml 中 hjy.ai.* 配置项，集中管理 AI 模型参数、
 * 社区信息、以及与 hjy-community 主后端对接的接口地址。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "hjy.ai")
public class AiProperties {

    /** DeepSeek API 密钥 */
    private String apiKey;

    /** AI 模型名称，默认 deepseek-chat */
    private String model = "deepseek-chat";

    /** 嵌入模型名称 */
    private String embeddingModel = "deepseek-embedding";

    /** 模型温度参数（0-1），控制回复随机性 */
    private Double temperature = 0.7;

    /** 最大生成 Token 数 */
    private Integer maxTokens = 2000;

    /** 社区基础信息配置 */
    private CommunityConfig community = new CommunityConfig();

    /** 主后端服务对接配置 */
    private HjyCommunityConfig hjyCommunity = new HjyCommunityConfig();

    /** 社区基础信息 */
    @Data
    public static class CommunityConfig {
        private String name = "合家云社区";
        private String address = "智慧小区";
        private String servicePhone = "400-888-8888";
    }

    /** hjy-community 主后端的端点配置 */
    @Data
    public static class HjyCommunityConfig {
        private String baseUrl = "http://127.0.0.1:8080";
        private String repairEndpoint = "/api/property/repair";
        private String ownerEndpoint = "/api/property/owner";
        private String complaintEndpoint = "/api/property/complaint";
        private String feeEndpoint = "/api/property/fee";
        private String announcementEndpoint = "/api/community/announcement";
    }
}
