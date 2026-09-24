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

class ToolAuthorizationTest {
    private ToolCallback delegate(String name) {
        return new ToolCallback() {
            public ToolDefinition getToolDefinition() {
                return ToolDefinition.builder().name(name).description("test").inputSchema("{}").build();
            }
            public String call(String input) { return CallerContext.current(); }
        };
    }
    @Test void contextIsRequiredAndClearedAfterCallback() {
        AuthorizedToolCallback callback = new AuthorizedToolCallback(delegate("query"));
        assertTrue(callback.call("{}").contains("身份"));
        assertEquals("Bearer alice", callback.call("{}", new ToolContext(Map.of(CallerContext.AUTHORIZATION, "Bearer alice"))));
        assertNull(CallerContext.current());
        assertEquals("Bearer bob", callback.call("{}", new ToolContext(Map.of(CallerContext.AUTHORIZATION, "Bearer bob"))));
        assertNull(CallerContext.current());
    }
    @Test void writeAndLocationCallbacksAreNotExecuted() {
        ToolContext context = new ToolContext(Map.of(CallerContext.AUTHORIZATION, "Bearer alice"));
        for (String name : new String[]{"createRepairOrder", "cancelRepairOrder", "submitComplaint", "registerVisitor", "query_login_location"}) {
            assertNotEquals("Bearer alice", new AuthorizedToolCallback(delegate(name)).call("{}", context));
        }
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
        String previous = CallerContext.install("Bearer alice");
        try {
            assertThrows(IllegalStateException.class, () -> client.get("/system/owner/list"));
            assertTrue(client.put("/system/repair/cancel/1", Map.of()).contains("403"));
        } finally { CallerContext.restore(previous); }
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
