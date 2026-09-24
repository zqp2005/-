package com.msb.hjy.ai.config;

import com.msb.hjy.ai.controller.ChatController;
import com.msb.hjy.ai.dto.ChatRequest;
import com.msb.hjy.ai.service.ChatService;
import com.msb.hjy.ai.service.impl.ChatServiceImpl;
import com.msb.hjy.ai.prompt.PromptTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import reactor.core.publisher.Flux;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ChatStreamTest {
    @Test void greetingReturnsRawTextAndDoneNotNestedSseFrames() {
        ChatServiceImpl service = new ChatServiceImpl(null, null, new PromptTemplate());
        ChatRequest request = new ChatRequest(); request.setMessage("你好");
        List<String> result = service.chatStream(request).collectList().block();
        assertEquals(2, result.size()); assertEquals("[DONE]", result.get(1));
        assertFalse(result.get(0).startsWith("data:")); assertTrue(result.get(0).contains("\n\n"));
    }

    @Test void mvcSseEncoderPreservesMultilineDataAndControllerUsesTrustedIdentity() throws Exception {
        ChatService service = mock(ChatService.class);
        when(service.chatStream(any())).thenReturn(Flux.just("第一行\n\n  缩进\ndata:正文", "[DONE]"));
        java.util.List<org.springframework.http.converter.HttpMessageConverter<?>> converters = new java.util.ArrayList<>();
        converters.add(new org.springframework.http.converter.StringHttpMessageConverter());
        converters.add(new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter());
        new WebConfig().extendMessageConverters(converters);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new ChatController(service))
                .setMessageConverters(converters.toArray(new org.springframework.http.converter.HttpMessageConverter<?>[0])).build();
        MvcResult pending = mvc.perform(post("/ai/chat/stream").contentType("application/json")
                .header("Authorization", "Bearer trusted")
                .requestAttr(JwtAuthFilter.ATTR_USER_ID, 2L).requestAttr(JwtAuthFilter.ATTR_USER_NAME, "worker")
                .content("{\"sessionId\":\"test\",\"message\":\"test\",\"userId\":1}"))
                .andExpect(request().asyncStarted()).andReturn();
        MvcResult result = mvc.perform(asyncDispatch(pending)).andExpect(status().isOk()).andReturn();
        String body = result.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(body.contains("data:第一行\ndata:\ndata:  缩进\ndata:data:正文\n\n"), body);
        assertTrue(body.endsWith("data:[DONE]\n\n"), body);
        verify(service).chatStream(argThat(r -> Long.valueOf(2).equals(r.getUserId()) && "Bearer trusted".equals(r.getCallerAuthorization())));
    }
}
