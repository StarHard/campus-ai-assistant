package com.campus.ai.websocket;

import com.campus.ai.dto.ChatRequest;
import com.campus.ai.service.ChatService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * WebSocket聊天处理器
 * 功能：
 * 1. 连接管理（上线/下线/在线人数统计）
 * 2. AI问答（通过WebSocket收发，支持流式输出）
 * 3. 取消生成（前端发送cancel指令中断AI流）
 *
 * 消息协议：
 * 客户端→服务端：
 *   {"type":"chat", "question":"xxx", "sessionId":"xxx"}     发起提问
 *   {"type":"cancel", "sessionId":"xxx"}                      取消生成
 *
 * 服务端→客户端：
 *   {"type":"message", "content":"片段", "sessionId":"xxx"}   流式内容
 *   {"type":"done", "sessionId":"xxx", "latency":123}        流结束
 *   {"type":"error", "error":"错误信息"}                       错误
 *   {"type":"online_count", "count":5}                        在线人数
 *
 * @author A组长
 */
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    /** 所有在线的WebSocket连接 */
    private final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    /** 在线人数计数器 */
    private final AtomicInteger onlineCount = new AtomicInteger(0);

    /** 正在生成的会话 → 取消标志 (sessionId -> 是否取消) */
    private final ConcurrentHashMap<String, Boolean> cancelFlags = new ConcurrentHashMap<>();

    /** 正在生成的会话 → 对应的WebSocketSession (用于推送) */
    private final ConcurrentHashMap<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();

    @Autowired
    private ChatService chatService;

    @Autowired
    private Executor aiTaskExecutor;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ========== 连接生命周期 ==========

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        int count = onlineCount.incrementAndGet();
        log.info("WebSocket连接建立: sessionId={}, 当前在线: {}", session.getId(), count);
        broadcastOnlineCount();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        int count = onlineCount.decrementAndGet();
        log.info("WebSocket连接关闭: sessionId={}, 原因={}, 当前在线: {}", session.getId(), status, count);
        broadcastOnlineCount();
    }

    // ========== 消息处理 ==========

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            JsonNode json = objectMapper.readTree(message.getPayload());
            String type = json.has("type") ? json.get("type").asText() : "";

            switch (type) {
                case "chat" -> handleChat(session, json);
                case "cancel" -> handleCancel(json);
                default -> sendError(session, "未知消息类型: " + type);
            }
        } catch (Exception e) {
            log.error("处理WebSocket消息失败", e);
            sendError(session, "消息解析失败: " + e.getMessage());
        }
    }

    /**
     * 处理聊天请求：通过WebSocket发起AI问答，流式返回
     */
    private void handleChat(WebSocketSession session, JsonNode json) {
        String question = json.has("question") ? json.get("question").asText() : "";
        String sessionId = json.has("sessionId") ? json.get("sessionId").asText() : null;

        if (question.isEmpty()) {
            sendError(session, "问题不能为空");
            return;
        }
        if (sessionId == null) {
            sessionId = java.util.UUID.randomUUID().toString().replace("-", "");
        }

        log.info("[WS] 收到聊天请求: question={}, sessionId={}", question, sessionId);

        // 注册当前会话为活跃状态（可被取消）
        cancelFlags.put(sessionId, false);
        activeSessions.put(sessionId, session);

        ChatRequest request = new ChatRequest();
        request.setQuestion(question);
        request.setSessionId(sessionId);
        request.setEnableRag(true);

        long startTime = System.currentTimeMillis();

        // 将sessionId和startTime转为final局部变量供lambda使用
        final String sid = sessionId;
        final long start = startTime;

        aiTaskExecutor.execute(() -> {
            try {
                var response = chatService.chat(request);

                // 检查是否已被取消
                if (isCancelled(sid)) {
                    WebSocketSession ws = activeSessions.get(sid);
                    sendJson(ws, Map.of(
                        "type", "done",
                        "sessionId", sid,
                        "cancelled", true,
                        "latency", System.currentTimeMillis() - start
                    ));
                    cleanupSession(sid);
                    return;
                }

                // 模拟流式输出：将完整回答分段发送
                String answer = response.getAnswer();
                if (answer != null && !answer.isEmpty()) {
                    int chunkSize = 8; // 每次发送8个字符
                    for (int i = 0; i < answer.length(); i += chunkSize) {
                        if (isCancelled(sid)) {
                            WebSocketSession ws = activeSessions.get(sid);
                            sendJson(ws, Map.of(
                                "type", "done",
                                "sessionId", sid,
                                "cancelled", true,
                                "latency", System.currentTimeMillis() - start
                            ));
                            cleanupSession(sid);
                            return;
                        }
                        int end = Math.min(i + chunkSize, answer.length());
                        String chunk = answer.substring(i, end);
                        WebSocketSession ws = activeSessions.get(sid);
                        sendJson(ws, Map.of(
                            "type", "message",
                            "content", chunk,
                            "sessionId", sid
                        ));
                        Thread.sleep(30); // 模拟打字延迟
                    }
                }

                WebSocketSession ws = activeSessions.get(sid);
                sendJson(ws, Map.of(
                    "type", "done",
                    "sessionId", sid,
                    "latency", System.currentTimeMillis() - start
                ));

            } catch (Exception e) {
                log.error("[WS] AI调用失败: sessionId={}", sid, e);
                WebSocketSession ws = activeSessions.get(sid);
                sendError(ws, "AI服务调用失败: " + e.getMessage());
            } finally {
                cleanupSession(sid);
            }
        });
    }

    /**
     * 处理取消生成请求
     */
    private void handleCancel(JsonNode json) {
        String sessionId = json.has("sessionId") ? json.get("sessionId").asText() : null;
        if (sessionId != null) {
            log.info("[WS] 收到取消请求: sessionId={}", sessionId);
            cancelFlags.put(sessionId, true);
        }
    }

    // ========== 在线人数推送 ==========

    /**
     * 向所有在线用户推送当前在线人数
     */
    public void broadcastOnlineCount() {
        String msg = toJson(Map.of(
            "type", "online_count",
            "count", onlineCount.get()
        ));
        broadcast(msg);
    }

    /**
     * 向所有连接广播消息
     */
    private void broadcast(String message) {
        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                try {
                    s.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    log.warn("广播消息失败: sessionId={}", s.getId(), e);
                }
            }
        }
    }

    // ========== 工具方法 ==========

    private boolean isCancelled(String sessionId) {
        return Boolean.TRUE.equals(cancelFlags.get(sessionId));
    }

    private void cleanupSession(String sessionId) {
        cancelFlags.remove(sessionId);
        activeSessions.remove(sessionId);
    }

    private void sendJson(WebSocketSession session, Map<String, Object> data) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(toJson(data)));
            }
        } catch (IOException e) {
            log.warn("发送WS消息失败", e);
        }
    }

    private void sendError(WebSocketSession session, String error) {
        sendJson(session, Map.of("type", "error", "error", error));
    }

    private String toJson(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            return "{}";
        }
    }

    // ========== 查询接口 ==========

    public int getOnlineCount() {
        return onlineCount.get();
    }
}
