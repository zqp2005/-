package com.msb.hjycommunity.property.constant;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 报修工单状态常量与合法转移表
 * <p>
 * Pending(待处理) -> Allocated(已分派)/Cancelled(已取消)/No_Processed(不处理)
 * Allocated(已分派) -> Processing(处理中)/Cancelled(已取消)
 * Processing(处理中) -> Processed(已处理)
 * Processed/No_Processed/Cancelled 为终态
 */
public class RepairState {

    public static final String PENDING = "Pending";
    public static final String ALLOCATED = "Allocated";
    public static final String PROCESSING = "Processing";
    public static final String PROCESSED = "Processed";
    public static final String NO_PROCESSED = "No_Processed";
    public static final String CANCELLED = "Cancelled";

    private static final Map<String, Set<String>> ALLOWED_TRANSITIONS = new HashMap<>();

    static {
        ALLOWED_TRANSITIONS.put(PENDING, new HashSet<>(Arrays.asList(ALLOCATED, CANCELLED, NO_PROCESSED)));
        ALLOWED_TRANSITIONS.put(ALLOCATED, new HashSet<>(Arrays.asList(PROCESSING, CANCELLED)));
        ALLOWED_TRANSITIONS.put(PROCESSING, new HashSet<>(Arrays.asList(PROCESSED)));
    }

    /** 判断从 from 状态能否流转到 to 状态（终态无出边，返回 false） */
    public static boolean canTransit(String from, String to) {
        Set<String> targets = ALLOWED_TRANSITIONS.get(from);
        return targets != null && targets.contains(to);
    }

    private RepairState() {
    }
}
