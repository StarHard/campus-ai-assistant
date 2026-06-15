package com.campus.ai.service;

import com.campus.ai.dto.ChatRequest;
import com.campus.ai.dto.ChatResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI智能问答服务接口
 * 集成SpringAI Alibaba，对接大模型
 *
 * @author A组长
 */
public interface ChatService {

    /**
     * 智能问答（同步模式）
     *
     * @param request 聊天请求
     * @return 聊天响应
     */
    ChatResponse chat(ChatRequest request);

    /**
     * 智能问答（流式模式）
     * 使用SSE（Server-Sent Events）实时推送AI回复内容
     *
     * @param request 聊天请求
     * @return SSE流式响应
     */
    SseEmitter chatStream(ChatRequest request);

    /**
     * 简单问答（无需会话管理）
     * 适用于单次快速提问场景
     *
     * @param question 用户问题
     * @return AI回答
     */
    String simpleChat(String question);

    /**
     * 清理会话历史
     *
     * @param sessionId 会话ID
     */
    void clearSession(String sessionId);

    /**
     * 获取会话历史记录
     *
     * @param sessionId 会话ID
     * @return 历史消息列表
     */
    Object getSessionHistory(String sessionId);
}
