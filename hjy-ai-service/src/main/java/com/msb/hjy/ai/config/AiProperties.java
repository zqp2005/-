package com.msb.hjy.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "hjy.ai")
public class AiProperties {

    private String apiKey;
    
    private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
    
    private String model = "qwen-plus";
    
    private String embeddingModel = "text-embedding-v3";
    
    private Double temperature = 0.7;
    
    private Integer maxTokens = 2000;
    
    private CommunityConfig community = new CommunityConfig();
    
    private HjyCommunityConfig hjyCommunity = new HjyCommunityConfig();
    
    @Data
    public static class CommunityConfig {
        private String name = "合家云社区";
        private String address = "智慧小区";
        private String servicePhone = "400-888-8888";
    }
    
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
