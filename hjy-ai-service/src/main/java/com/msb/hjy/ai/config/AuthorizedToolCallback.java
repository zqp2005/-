package com.msb.hjy.ai.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.msb.hjy.ai.client.CallerContext;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;
import java.util.Set;

/** 服务端可信上下文不进入模型参数；写操作与位置查询在此执行额外授权。 */
public final class AuthorizedToolCallback implements ToolCallback {
    private static final Set<String> BLOCKED_WRITES = Set.of("cancelRepairOrder", "submitComplaint", "registerVisitor");
    private static final Set<String> CONFIRMATIONS = Set.of("确认", "确认提交", "确认报修", "确认创建");
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final ToolCallback delegate;
    private final PendingRepairConfirmationStore pendingRepairs;
    public AuthorizedToolCallback(ToolCallback delegate, PendingRepairConfirmationStore pendingRepairs) {
        this.delegate = delegate;
        this.pendingRepairs = pendingRepairs;
    }
    public ToolDefinition getToolDefinition() { return delegate.getToolDefinition(); }
    public ToolMetadata getToolMetadata() { return delegate.getToolMetadata(); }
    public String call(String input) { return "缺少可信调用者身份，请重新登录。"; }
    public String call(String input, ToolContext context) {
        Object value = context == null ? null : context.getContext().get(CallerContext.AUTHORIZATION);
        if (!(value instanceof String token) || !token.startsWith("Bearer ")) return call(input);
        String name = getToolDefinition().name();
        if (BLOCKED_WRITES.contains(name)) return "该写操作尚未开放，请到对应业务页面核对内容并提交。";
        if ("createRepairOrder".equals(name)) {
            String conversationId = contextValue(context, CallerContext.CONVERSATION_ID);
            if (!confirmed(context)) {
                pendingRepairs.save(conversationId, input);
                return repairConfirmation(input);
            }
            String pendingInput = pendingRepairs.consume(conversationId);
            if (pendingInput == null) {
                return "当前没有待确认的报修，或确认已超过10分钟，请重新描述报修信息。";
            }
            input = pendingInput;
        }

        String effectiveInput = input;
        if (name.contains("login_location")) {
            Object userName = context.getContext().get(CallerContext.USER_NAME);
            if (!(userName instanceof String) || ((String) userName).isBlank()) {
                return "无法识别当前登录用户，请重新登录。";
            }
            effectiveInput = forceUserName(input, (String) userName);
        }
        String previous = CallerContext.install(token);
        try { return delegate.call(effectiveInput); }
        finally { CallerContext.restore(previous); }
    }

    private boolean confirmed(ToolContext context) {
        Object value = context.getContext().get(CallerContext.USER_MESSAGE);
        if (!(value instanceof String)) return false;
        String normalized = ((String) value).replaceAll("[\\s，。！？!?,.]", "");
        return CONFIRMATIONS.contains(normalized);
    }

    private String contextValue(ToolContext context, String key) {
        Object value = context.getContext().get(key);
        return value instanceof String ? (String) value : null;
    }

    private String repairConfirmation(String input) {
        try {
            JsonNode node = MAPPER.readTree(input);
            return "请确认提交以下报修信息：\n"
                    + "业主：" + text(node, "ownerName", "未填写") + "\n"
                    + "电话：" + text(node, "phone", "未填写") + "\n"
                    + "位置：" + text(node, "location", "未填写") + "\n"
                    + "问题：" + text(node, "problem", "未填写") + "\n"
                    + "类别：" + text(node, "category", "其他") + "\n"
                    + "信息无误请回复“确认提交”，需要修改请直接说明。";
        } catch (Exception e) {
            return "报修信息已整理，请核对后回复“确认提交”。";
        }
    }

    private String forceUserName(String input, String userName) {
        try {
            JsonNode node = MAPPER.readTree(input);
            ObjectNode object = node != null && node.isObject() ? (ObjectNode) node : MAPPER.createObjectNode();
            object.put("userName", userName);
            return MAPPER.writeValueAsString(object);
        } catch (Exception e) {
            ObjectNode object = MAPPER.createObjectNode();
            object.put("userName", userName);
            return object.toString();
        }
    }

    private String text(JsonNode node, String field, String fallback) {
        String value = node.path(field).asText("").trim();
        return value.isEmpty() ? fallback : value;
    }
}
