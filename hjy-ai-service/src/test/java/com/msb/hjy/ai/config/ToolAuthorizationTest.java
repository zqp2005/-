package com.msb.hjy.ai.config;

import com.msb.hjy.ai.client.*;
import com.msb.hjy.ai.tools.PropertyFeeTool;
import com.msb.hjy.ai.dto.ChatRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static org.mockito.Mockito.*;

class ToolAuthorizationTest {
    private final PendingRepairConfirmationStore pendingRepairs = mock(PendingRepairConfirmationStore.class);

    private AuthorizedToolCallback callback(ToolCallback delegate) {
        return new AuthorizedToolCallback(delegate, pendingRepairs);
    }

    private ToolCallback delegate(String name) {
        return new ToolCallback() {
            public ToolDefinition getToolDefinition() {
                return ToolDefinition.builder().name(name).description("test").inputSchema("{}").build();
            }
            public String call(String input) { return CallerContext.current(); }
        };
    }
    private ToolCallback echoDelegate(String name) {
        return new ToolCallback() {
            public ToolDefinition getToolDefinition() {
                return ToolDefinition.builder().name(name).description("test").inputSchema("{}").build();
            }
            public String call(String input) { return input; }
        };
    }
    @Test void contextIsRequiredAndClearedAfterCallback() {
        AuthorizedToolCallback callback = callback(delegate("query"));
        assertTrue(callback.call("{}").contains("身份"));
        assertEquals("Bearer alice", callback.call("{}", new ToolContext(Map.of(CallerContext.AUTHORIZATION, "Bearer alice"))));
        assertNull(CallerContext.current());
        assertEquals("Bearer bob", callback.call("{}", new ToolContext(Map.of(CallerContext.AUTHORIZATION, "Bearer bob"))));
        assertNull(CallerContext.current());
    }
    @Test void unconfirmedRepairIsPreviewedAndOtherWritesRemainBlocked() {
        ToolContext context = new ToolContext(Map.of(CallerContext.AUTHORIZATION, "Bearer alice"));
        String input = "{\"ownerName\":\"张三\",\"phone\":\"13800000000\",\"location\":\"1栋101\",\"problem\":\"漏水\",\"category\":\"water\"}";
        ToolContext preview = new ToolContext(Map.of(
                CallerContext.AUTHORIZATION, "Bearer alice",
                CallerContext.USER_NAME, "alice",
                CallerContext.USER_MESSAGE, "我要报修漏水",
                CallerContext.CONVERSATION_ID, "2:test"));
        assertTrue(callback(delegate("createRepairOrder")).call(input, preview).contains("确认提交"));
        verify(pendingRepairs).save("2:test", input);
        for (String name : new String[]{"cancelRepairOrder", "submitComplaint", "registerVisitor"}) {
            assertNotEquals("Bearer alice", callback(delegate(name)).call("{}", context));
        }
        assertNull(CallerContext.current());
    }
    @Test void confirmedRepairExecutesWithCallerIdentity() {
        ToolContext context = new ToolContext(Map.of(
                CallerContext.AUTHORIZATION, "Bearer alice",
                CallerContext.USER_NAME, "alice",
                CallerContext.USER_MESSAGE, "确认提交",
                CallerContext.CONVERSATION_ID, "2:test"));
        when(pendingRepairs.consume("2:test")).thenReturn("{\"problem\":\"漏水\"}");
        assertEquals("Bearer alice", callback(delegate("createRepairOrder")).call("{}", context));
        assertNull(CallerContext.current());
    }
    @Test void repairCannotBeCreatedWithoutPendingPreview() {
        ToolContext context = new ToolContext(Map.of(
                CallerContext.AUTHORIZATION, "Bearer alice",
                CallerContext.USER_MESSAGE, "确认提交",
                CallerContext.CONVERSATION_ID, "2:test"));
        assertTrue(callback(delegate("createRepairOrder")).call("{}", context).contains("没有待确认"));
        assertNull(CallerContext.current());
    }
    @Test void loginLocationAlwaysUsesAuthenticatedUserName() throws Exception {
        ToolContext context = new ToolContext(Map.of(
                CallerContext.AUTHORIZATION, "Bearer alice",
                CallerContext.USER_NAME, "alice",
                CallerContext.USER_MESSAGE, "查询我的登录位置"));
        String actual = callback(echoDelegate("query_login_location"))
                .call("{\"userName\":\"admin\"}", context);
        assertEquals("alice", new ObjectMapper().readTree(actual).path("userName").asText());
        assertNull(CallerContext.current());
    }
    @Test void clientUsesCallerHeaderWithoutAdminLoginOrRetry() {
        RestTemplate rest = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(rest).build();
        HjyCommunityClient client = new HjyCommunityClient();
        ReflectionTestUtils.setField(client, "restTemplate", rest);
        ReflectionTestUtils.setField(client, "baseUrl", "http://localhost:8080");
        assertThrows(SecurityException.class, () -> client.get("/system/owner/list"));
        server.expect(requestTo("http://localhost:8080/system/owner/list"))
                .andExpect(header("Authorization", "Bearer alice"))
                .andRespond(withSuccess("{\"code\":403}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://localhost:8080/system/repair/cancel/1"))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(header("Authorization", "Bearer alice"))
                .andRespond(withSuccess("{\"code\":403}", MediaType.APPLICATION_JSON));
        String previous = CallerContext.install("Bearer alice");
        try {
            assertThrows(IllegalStateException.class, () -> client.get("/system/owner/list"));
            assertTrue(client.put("/system/repair/cancel/1", Map.of()).contains("403"));
        } finally { CallerContext.restore(previous); }
        server.verify();
    }
    @Test void clientPostUsesCallerHeader() {
        RestTemplate rest = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(rest).build();
        HjyCommunityClient client = new HjyCommunityClient();
        ReflectionTestUtils.setField(client, "restTemplate", rest);
        ReflectionTestUtils.setField(client, "baseUrl", "http://localhost:8080");
        server.expect(requestTo("http://localhost:8080/system/repair"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer alice"))
                .andRespond(withSuccess("{\"code\":200}", MediaType.APPLICATION_JSON));
        String previous = CallerContext.install("Bearer alice");
        try {
            assertTrue(client.post("/system/repair", Map.of("repairContent", "漏水")).contains("200"));
        } finally {
            CallerContext.restore(previous);
        }
        server.verify();
    }
    @Test void tokenCannotBeInjectedThroughRequestJsonOrLogged() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ChatRequest request = mapper.readValue("{\"callerAuthorization\":\"Bearer forged\"}", ChatRequest.class);
        assertNull(request.getCallerAuthorization());
        request.setCallerAuthorization("Bearer secret");
        assertFalse(mapper.writeValueAsString(request).contains("secret"));
        assertFalse(request.toString().contains("secret"));
    }
    @Test void feeToolsDoNotInventLedgerValues() {
        PropertyFeeTool fees = new PropertyFeeTool();
        assertTrue(fees.getArrearsInfo("").contains("尚未接入"));
        assertFalse(fees.getArrearsInfo("").contains("2个月"));
        assertFalse(fees.getPaymentGuide().contains("400-888"));
        assertTrue(fees.calculatePropertyFee(100.0, "residential", 2).contains("不能"));
    }
}
