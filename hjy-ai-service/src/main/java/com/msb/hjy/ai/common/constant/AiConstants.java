package com.msb.hjy.ai.common.constant;

public class AiConstants {

    public static final class Session {
        public static final String SESSION_PREFIX = "ai:session:";
        public static final int SESSION_EXPIRE_SECONDS = 3600 * 24;
    }

    public static final class Chat {
        public static final String DEFAULT_USER = "user";
        public static final String DEFAULT_ASSISTANT = "assistant";
        public static final int MAX_HISTORY_SIZE = 50;
    }

    public static final class Agent {
        public static final String PROPERTY_AGENT = "property";
        public static final String CUSTOMER_SERVICE_AGENT = "customer_service";
        public static final String DATA_ANALYSIS_AGENT = "data_analysis";
    }

    public static final class Knowledge {
        public static final String PROPERTY_MANUAL = "property_manual";
        public static final String FAQ = "faq";
        public static final String ANNOUNCEMENT = "announcement";
    }

    public static final class Tool {
        public static final String REPAIR = "repair";
        public static final String COMPLAINT = "complaint";
        public static final String PROPERTY_FEE = "property_fee";
        public static final String OWNER_INFO = "owner_info";
        public static final String ANNOUNCEMENT = "announcement";
    }
}
