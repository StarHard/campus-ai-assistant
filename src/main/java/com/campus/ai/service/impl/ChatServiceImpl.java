package com.campus.ai.service.impl;

import com.campus.ai.config.AiServiceException;
import com.campus.ai.dto.ChatRequest;
import com.campus.ai.dto.ChatResponse;
import com.campus.ai.rag.RagService;
import com.campus.ai.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.model.ChatResponse as AiChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * AI智能问答服务实现类
 * 核心功能：集成SpringAI Alibaba，对接大模型，支持RAG检索增强
 *
 * @author A组长
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatClient.Builder chatClientBuilder;

    @Autowired(required = false)
    private RagService ragService;

    @Value("${app.system-prompt:}")
    private String defaultSystemPrompt;

    @Autowired
    private Executor aiTaskExecutor;

    /**
     * 智能问答（同步模式）
     */
    @Override
    public ChatResponse chat(ChatRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("收到聊天请求: question={}, sessionId={}", request.getQuestion(), request.getSessionId());

        try {
            // 生成会话ID（如果未提供）
            String sessionId = request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString();

            // 构建消息列表
            List<Message> messages = buildMessages(request);

            // 创建ChatClient并调用AI模型
            ChatClient chatClient = chatClientBuilder.build();

            // 执行AI调用（异步执行以利用线程池）
            CompletableFuture<AiChatResponse> future = CompletableFuture.supplyAsync(() -> {
                Prompt prompt = new Prompt(messages);
                return chatClient.prompt(prompt).call().chatResponse();
            }, aiTaskExecutor);

            // 等待结果
            AiChatResponse aiResponse = future.get();

            // 构建响应对象
            ChatResponse response = ChatResponse.builder()
                    .id(UUID.randomUUID().toString())
                    .sessionId(sessionId)
                    .answer(aiResponse.getResult().getOutput().getText())
                    .model(getModelName(request))
                    .latency(System.currentTimeMillis() - startTime)
                    .ragEnabled(request.getEnableRag())
                    .build();

            // 设置Token使用统计
            if (aiResponse.getMetadata() != null && aiResponse.getMetadata().getUsage() != null) {
                var usage = aiResponse.getMetadata().getUsage();
                response.setTokenUsage(ChatResponse.TokenUsage.builder()
                        .promptTokens(usage.getPromptTokens())
                        .completionTokens(usage.getCompletionTokens())
                        .totalTokens(usage.getTotalTokens())
                        .build());
            }

            log.info("AI响应完成: latency={}ms, tokens={}",
                    response.getLatency(),
                    response.getTokenUsage() != null ? response.getTokenUsage().getTotalTokens() : "N/A");

            return response;

        } catch (AiServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI调用失败", e);
            throw new AiServiceException("AI服务调用失败: " + e.getMessage());
        }
    }

    /**
     * 智能问答（流式模式）
     */
    @Override
    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter chatStream(ChatRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("收到流式聊天请求: question={}, sessionId={}", request.getQuestion(), request.getSessionId());

        // 创建SSE连接（超时时间30分钟）
        org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter =
                new org.springframework.web.servlet.mvc.method.annotation.SseEmitter(30 * 60 * 1000L);

        // 异步处理流式请求
        CompletableFuture.runAsync(() -> {
            try {
                String sessionId = request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString();

                // 构建消息列表
                List<Message> messages = buildMessages(request);

                // 创建ChatClient
                ChatClient chatClient = chatClientBuilder.build();
                Prompt prompt = new Prompt(messages);

                // 流式调用AI模型
                var stream = chatClient.prompt(prompt).stream().chatResponse();

                // 发送流式数据
                stream.content().forEach(content -> {
                    try {
                        emitter.send(SseEmitter.event()
                                .name("message")
                                .data(content));
                    } catch (Exception e) {
                        log.warn("发送SSE数据失败", e);
                    }
                });

                // 发送完成事件
                emitter.send(SseEmitter.event()
                        .name("done")
                        .data("{\"sessionId\":\"" + sessionId + "\",\"latency\":" + (System.currentTimeMillis() - startTime) + "}"));

                emitter.complete();

                log.info("流式响应完成: sessionId={}, latency={}ms", sessionId, System.currentTimeMillis() - startTime);

            } catch (Exception e) {
                log.error("流式AI调用失败", e);
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("{\"error\":\"" + e.getMessage().replace("\"", "\\\"") + "\"}"));
                    emitter.completeWithError(e);
                } catch (Exception ex) {
                    emitter.completeWithError(ex);
                }
            }
        }, aiTaskExecutor);

        return emitter;
    }

    /**
     * 简单问答
     */
    @Override
    public String simpleChat(String question) {
        ChatRequest request = new ChatRequest();
        request.setQuestion(question);
        request.setEnableRag(true);

        ChatResponse response = chat(request);
        return response.getAnswer();
    }

    @Override
    public void clearSession(String sessionId) {
        // TODO: 实现会话清理逻辑（可结合Redis或内存存储）
        log.info("清理会话: sessionId={}", sessionId);
    }

    @Override
    public Object getSessionHistory(String sessionId) {
        // TODO: 实现会话历史查询逻辑
        log.info("获取会话历史: sessionId={}", sessionId);
        return null;
    }

    /**
     * 构建消息列表（包含系统提示词、历史消息、当前问题）
     */
    private List<Message> buildMessages(ChatRequest request) {
        List<Message> messages = new ArrayList<>();

        // 1. 添加系统提示词
        String systemPrompt = request.getSystemPrompt() != null ?
                request.getSystemPrompt() : defaultSystemPrompt;
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            messages.add(new SystemMessage(systemPrompt));
        }

        // 2. 如果启用RAG，添加检索到的上下文信息
        if (Boolean.TRUE.equals(request.getEnableRag()) && ragService != null) {
            String context = ragService.retrieveContext(request.getQuestion());
            if (context != null && !context.isEmpty()) {
                messages.add(new SystemMessage("【参考文档内容】\n" + context + "\n请基于以上参考文档内容回答用户问题。"));
            }
        }

        // 3. 添加历史消息
        if (request.getHistory() != null && !request.getHistory().isEmpty()) {
            for (ChatRequest.Message msg : request.getHistory()) {
                switch (msg.getRole().toLowerCase()) {
                    case "user":
                        messages.add(new UserMessage(msg.getContent()));
                        break;
                    case "assistant":
                        messages.add(new AssistantMessage(msg.getContent()));
                        break;
                    case "system":
                        messages.add(new SystemMessage(msg.getContent()));
                        break;
                }
            }
        }

        // 4. 添加当前用户问题
        messages.add(new UserMessage(request.getQuestion()));

        return messages;
    }

    /**
     * 获取模型名称
     */
    private String getModelName(ChatRequest request) {
        if (request.getModelConfig() != null && request.getModelConfig().getModel() != null) {
            return request.getModelConfig().getModel();
        }
        return "qwen-max"; // 默认模型
    }
}
