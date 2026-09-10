package com.msb.hjy.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 合家云 MCP Server 启动类
 * <p>
 * 独立的 MCP（Model Context Protocol）工具服务，运行在 8091 端口，
 * 通过 SSE 端点（/sse）向 hjy-ai-service 暴露环境上下文工具：
 * <ul>
 *     <li>query_login_location —— 查询用户最近登录 IP 归属地（内网回退社区注册地址）</li>
 *     <li>query_weather —— 查询城市实时天气（高德）</li>
 * </ul>
 */
@SpringBootApplication
public class HjyMcpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(HjyMcpServerApplication.class, args);
    }
}
