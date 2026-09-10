package com.msb.hjy.mcp.config;

import com.msb.hjy.mcp.service.ContextToolService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MCP 工具注册配置
 * <p>
 * 将 {@link ContextToolService} 中的 @Tool 注解方法包装为 {@link ToolCallbackProvider}，
 * Spring AI 的 MCP Server 自动配置会扫描该 Bean 并把工具经 /sse 端点暴露给 MCP 客户端
 * （即 hjy-ai-service），工具名为 @Tool(name=...) 指定的 query_login_location / query_weather。
 */
@Configuration
public class McpToolsConfig {

    @Bean
    public ToolCallbackProvider hjyContextTools(ContextToolService contextToolService) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(contextToolService)
                .build();
    }
}
