package com.msb.hjycommunity.property.constant;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 投诉建议状态常量与合法转移表
 * <p>
 * Pending(待受理) -> Processing(处理中)/Closed(已关闭，不予受理)
 * Processing(处理中) -> Replied(已回复)
 * Replied(已回复) -> Closed(已关闭)
 */
public class SuggestState {

    public static final String PENDING = "Pending";
    public static final String PROCESSING = "Processing";
    public static final String REPLIED = "Replied";
    public static final String CLOSED = "Closed";

    private static final Map<String, Set<String>> ALLOWED_TRANSITIONS = new HashMap<>();

    static {
        ALLOWED_TRANSITIONS.put(PENDING, new HashSet<>(Arrays.asList(PROCESSING, CLOSED)));
        ALLOWED_TRANSITIONS.put(PROCESSING, new HashSet<>(Arrays.asList(REPLIED)));
        ALLOWED_TRANSITIONS.put(REPLIED, new HashSet<>(Arrays.asList(CLOSED)));
    }

    public static boolean canTransit(String from, String to) {
        Set<String> targets = ALLOWED_TRANSITIONS.get(from);
        return targets != null && targets.contains(to);
    }

    private SuggestState() {
    }
}
