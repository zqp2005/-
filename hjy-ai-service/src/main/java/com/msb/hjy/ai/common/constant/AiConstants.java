package com.msb.hjy.ai.common.constant;

/**
 * AI 服务全局常量定义
 * <p>
 * 集中管理会话、聊天、代理、知识库、工具等模块的常量值，
 * 避免魔法数字和字符串散落在代码各处。
 */
public class AiConstants {

    /** 会话相关常量 */
    public static final class Session {
        /** Redis 会话 Key 前缀 */
        public static final String SESSION_PREFIX = "ai:session:";
        /** 会话过期时间（秒）：24 小时 */
        public static final int SESSION_EXPIRE_SECONDS = 3600 * 24;
    }

    /** 聊天相关常量 */
    public static final class Chat {
        /** 用户角色标识 */
        public static final String DEFAULT_USER = "user";
        /** AI 助手角色标识 */
        public static final String DEFAULT_ASSISTANT = "assistant";
        /** 最大对话历史条数 */
        public static final int MAX_HISTORY_SIZE = 50;
    }

    /** 代理类型常量 */
    public static final class Agent {
        /** 物业助手代理 */
        public static final String PROPERTY_AGENT = "property";
        /** 客服助手代理 */
        public static final String CUSTOMER_SERVICE_AGENT = "customer_service";
        /** 数据分析代理 */
        public static final String DATA_ANALYSIS_AGENT = "data_analysis";
    }

    /** 知识库分类常量 */
    public static final class Knowledge {
        /** 物业管理手册 */
        public static final String PROPERTY_MANUAL = "property_manual";
        /** 常见问题 */
        public static final String FAQ = "faq";
        /** 社区公告 */
        public static final String ANNOUNCEMENT = "announcement";
    }

    /** 工具名称常量 */
    public static final class Tool {
        /** 报修工具 */
        public static final String REPAIR = "repair";
        /** 投诉工具 */
        public static final String COMPLAINT = "complaint";
        /** 物业费工具 */
        public static final String PROPERTY_FEE = "property_fee";
        /** 业主信息工具 */
        public static final String OWNER_INFO = "owner_info";
        /** 公告工具 */
        public static final String ANNOUNCEMENT = "announcement";
    }
}
