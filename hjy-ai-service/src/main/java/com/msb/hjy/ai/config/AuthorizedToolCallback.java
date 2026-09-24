package com.msb.hjy.ai.config;

import com.msb.hjy.ai.client.CallerContext;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;
import java.util.Set;

/** 服务端上下文不进入模型参数；写操作在确认流程上线前拒绝。 */
public final class AuthorizedToolCallback implements ToolCallback {
    private static final Set<String> WRITES = Set.of("createRepairOrder", "cancelRepairOrder", "submitComplaint", "registerVisitor");
    private final ToolCallback delegate;
    public AuthorizedToolCallback(ToolCallback delegate) { this.delegate = delegate; }
    public ToolDefinition getToolDefinition() { return delegate.getToolDefinition(); }
    public ToolMetadata getToolMetadata() { return delegate.getToolMetadata(); }
    public String call(String input) { return "缺少可信调用者身份，请重新登录。"; }
    public String call(String input, ToolContext context) {
        Object value = context == null ? null : context.getContext().get(CallerContext.AUTHORIZATION);
        if (!(value instanceof String token) || !token.startsWith("Bearer ")) return call(input);
        String name = getToolDefinition().name();
        if (WRITES.contains(name)) return "AI 写操作尚未开放，请到对应业务页面核对内容并提交。";
        if (name.contains("login_location")) return "登录位置查询暂未开放，请使用个人登录记录页面。";
        String previous = CallerContext.install(token);
        // 令牌只留在本地执行边界，不传给模型参数或远端 MCP 的上下文。
        try { return delegate.call(input); }
        finally { CallerContext.restore(previous); }
    }
}
