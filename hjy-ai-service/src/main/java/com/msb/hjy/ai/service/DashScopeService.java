package com.msb.hjy.ai.service;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.embeddings.TextEmbedding;
import com.alibaba.dashscope.embeddings.TextEmbeddingParam;
import com.alibaba.dashscope.embeddings.TextEmbeddingResult;
import com.alibaba.dashscope.tools.FunctionDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msb.hjy.ai.agent.ToolExecutor;
import com.msb.hjy.ai.agent.ToolsSpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.*;

@Slf4j
@Service
public class DashScopeService {

    @Autowired
    private Generation generation;

    @Autowired
    private TextEmbedding textEmbedding;

    @Autowired
    private String dashscopeApiKey;

    @Autowired
    private String chatModel;

    @Autowired
    private ToolExecutor toolExecutor;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, List<Message>> sessionHistory = new HashMap<>();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public String chat(String sessionId, String userMessage, String systemPrompt) {
        try {
            List<Message> history = sessionHistory.computeIfAbsent(sessionId, k -> new ArrayList<>());
            List<Message> messages = buildMessages(systemPrompt, history, userMessage);

            List tools = new ArrayList<>(ToolsSpec.TOOLS);
            GenerationParam param = GenerationParam.builder()
                    .model(chatModel)
                    .apiKey(dashscopeApiKey)
                    .messages(messages)
                    .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                    .tools(tools)
                    .build();

            GenerationResult result = generation.call(param);
            String response = processResponse(result, messages, history, userMessage, param);

            return response != null ? response : "抱歉，暂时无法回复您的问题。";

        } catch (Exception e) {
            log.error("调用DashScope API失败: {}", e.getMessage(), e);
            e.printStackTrace();
            return "AI服务暂时不可用: " + e.getMessage();
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private String processResponse(GenerationResult result, List<Message> messages,
                                   List<Message> history, String userMessage,
                                   GenerationParam originalParam) throws Exception {
        Object output = result.getOutput();
        if (output == null) {
            log.warn("Output is null");
            return null;
        }

        var choices = (List<?>) output.getClass().getMethod("getChoices").invoke(output);
        if (choices == null || choices.isEmpty()) {
            log.warn("Choices is empty or null");
            return null;
        }

        var choice = choices.get(0);
        var message = choice.getClass().getMethod("getMessage").invoke(choice);
        if (message == null) {
            log.warn("Message is null");
            return null;
        }

        String content = (String) message.getClass().getMethod("getContent").invoke(message);

        log.info("Message content: {}", content);

        Object toolCalls = message.getClass().getMethod("getToolCalls").invoke(message);
        log.info("ToolCalls object: {}", toolCalls);
        log.info("ToolCalls class: {}", toolCalls != null ? toolCalls.getClass().getName() : "null");

        if (toolCalls != null) {
            if (toolCalls instanceof List) {
                List<?> toolCallsList = (List<?>) toolCalls;
                log.info("ToolCalls list size: {}", toolCallsList.size());

                if (!toolCallsList.isEmpty()) {
                    log.info("检测到工具调用: {} 个工具", toolCallsList.size());

                    messages.add(Message.builder()
                            .role(Role.USER.getValue())
                            .content(userMessage)
                            .build());

                    for (Object tc : toolCallsList) {
                        log.info("Tool call object: {}", tc);
                        var function = tc.getClass().getMethod("getFunction").invoke(tc);
                        String toolName = (String) function.getClass().getMethod("getName").invoke(function);
                        String arguments = (String) function.getClass().getMethod("getArguments").invoke(function);

                        log.info("执行工具: {}, 参数: {}", toolName, arguments);

                        Map<String, Object> args = objectMapper.readValue(arguments, Map.class);
                        String toolResult = toolExecutor.execute(toolName, args);

                        String toolCallId = (String) tc.getClass().getMethod("getId").invoke(tc);

                        messages.add(Message.builder()
                                .role(Role.TOOL.getValue())
                                .content(toolResult)
                                .name(toolName)
                                .toolCallId(toolCallId)
                                .build());
                    }

                    List tools = new ArrayList<>(ToolsSpec.TOOLS);
                    GenerationParam followUpParam = GenerationParam.builder()
                            .model(chatModel)
                            .apiKey(dashscopeApiKey)
                            .messages(messages)
                            .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                            .tools(tools)
                            .build();

                    GenerationResult followUpResult = generation.call(followUpParam);
                    String followUpContent = extractContentSimple(followUpResult);

                    history.add(Message.builder()
                            .role(Role.USER.getValue())
                            .content(userMessage)
                            .build());
                    history.add(Message.builder()
                            .role(Role.ASSISTANT.getValue())
                            .content(followUpContent != null ? followUpContent : "")
                            .build());

                    trimHistory(history);

                    return followUpContent;
                }
            }
        }

        log.info("没有检测到工具调用，返回直接回复");

        history.add(Message.builder()
                .role(Role.USER.getValue())
                .content(userMessage)
                .build());
        history.add(Message.builder()
                .role(Role.ASSISTANT.getValue())
                .content(content != null ? content : "")
                .build());

        trimHistory(history);

        return content;
    }

    private List<Message> buildMessages(String systemPrompt, List<Message> history, String userMessage) {
        List<Message> messages = new ArrayList<>();

        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            messages.add(Message.builder()
                    .role(Role.SYSTEM.getValue())
                    .content(systemPrompt)
                    .build());
        }

        messages.addAll(history);

        messages.add(Message.builder()
                .role(Role.USER.getValue())
                .content(userMessage)
                .build());

        return messages;
    }

    private void trimHistory(List<Message> history) {
        if (history.size() > 50) {
            history.subList(0, history.size() - 50).clear();
        }
    }

    private String extractContentSimple(GenerationResult result) {
        if (result == null) return null;

        try {
            Object output = result.getOutput();
            if (output == null) return null;

            var choices = (List<?>) output.getClass().getMethod("getChoices").invoke(output);
            if (choices == null || choices.isEmpty()) return null;

            var choice = choices.get(0);
            var message = choice.getClass().getMethod("getMessage").invoke(choice);
            if (message == null) return null;

            return (String) message.getClass().getMethod("getContent").invoke(message);

        } catch (Exception e) {
            log.error("解析结果失败: {}", e.getMessage());
            return null;
        }
    }

    public Flux<String> chatStream(String sessionId, String userMessage, String systemPrompt) {
        String content = chat(sessionId, userMessage, systemPrompt);
        return Flux.just(content);
    }

    public void clearSession(String sessionId) {
        sessionHistory.remove(sessionId);
        log.info("清除会话历史 - sessionId: {}", sessionId);
    }

    public List<List<Double>> embedText(List<String> texts) {
        try {
            TextEmbeddingParam param = TextEmbeddingParam.builder()
                    .apiKey(dashscopeApiKey)
                    .model("text-embedding-v3")
                    .build();

            TextEmbeddingResult result = textEmbedding.call(param);

            List<List<Double>> embeddings = new ArrayList<>();
            if (result != null && result.getOutput() != null && result.getOutput().getEmbeddings() != null) {
                for (var embedding : result.getOutput().getEmbeddings()) {
                    embeddings.add(embedding.getEmbedding());
                }
            }
            return embeddings;

        } catch (Exception e) {
            log.error("文本向量化失败: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
