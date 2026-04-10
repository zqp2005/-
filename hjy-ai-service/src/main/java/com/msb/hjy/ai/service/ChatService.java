package com.msb.hjy.ai.service;

import com.msb.hjy.ai.dto.ChatRequest;
import com.msb.hjy.ai.dto.ChatResponse;

public interface ChatService {

    ChatResponse chat(ChatRequest request);

    ChatResponse chatWithAgent(ChatRequest request, String agentType);

    void clearSession(String sessionId);
}
