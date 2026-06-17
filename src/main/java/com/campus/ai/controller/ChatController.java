package com.campus.ai.controller;

import com.campus.ai.dto.ChatRequest;
import com.campus.ai.dto.ChatResponse;
import com.campus.ai.dto.Result;
import com.campus.ai.entity.ChatSession;
import com.campus.ai.service.ChatService;
import com.campus.ai.service.ChatSessionService;
import com.campus.ai.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * AI智能问答控制器
 * 提供RESTful API接口供前端（JavaFX/Vue）调用
 *
 * @author A组长
 */
@RestController
@RequestMapping("/chat")
@Tag(name = "AI智能问答", description = "校园智能服务助手核心问答接口")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ChatService chatService;
    private final ChatSessionService chatSessionService;
    private final UserService userService;

    public ChatController(ChatService chatService, ChatSessionService chatSessionService, UserService userService) {
        this.chatService = chatService;
        this.chatSessionService = chatSessionService;
        this.userService = userService;
    }

    /**
     * 智能问答（同步接口）
     * 适用于需要完整响应后再处理的场景
     */
    @PostMapping("/ask")
    @Operation(summary = "智能问答", description = "同步调用AI模型进行智能问答")
    public Result<ChatResponse> ask(@Valid @RequestBody ChatRequest request) {
        log.info("收到同步聊天请求: question={}", request.getQuestion());
        ChatResponse response = chatService.chat(request);
        return Result.success(response);
    }

    /**
     * 智能问答（流式接口）
     * 使用SSE实时推送AI回复内容，提升用户体验
     */
    @Hidden
    @PostMapping("/stream")
    @Operation(summary = "流式问答", description = "使用SSE流式返回AI回复内容")
    public SseEmitter stream(@Valid @RequestBody ChatRequest request) {
        log.info("收到流式聊天请求: question={}", request.getQuestion());
        return chatService.chatStream(request);
    }

    /**
     * 简单问答（GET接口）
     * 适用于快速测试和简单查询
     */
    @GetMapping("/simple")
    @Operation(summary = "简单问答", description = "快速单次问答（GET方式）")
    public Result<String> simpleAsk(@RequestParam String question) {
        log.info("收到简单问答请求: question={}", question);
        String answer = chatService.simpleChat(question);
        return Result.success(answer);
    }

    /**
     * 获取会话历史记录
     */
    @GetMapping("/session/{sessionId}/history")
    @Operation(summary = "会话历史", description = "获取指定会话的历史消息列表")
    public Result<Object> getSessionHistory(@PathVariable String sessionId) {

        log.info("获取会话历史: sessionId={}", sessionId);
        Object history = chatService.getSessionHistory(sessionId);
        return Result.success(history);
    }

    /**
     * 获取当前用户的所有会话列表
     */
    @GetMapping("/sessions")
    @Operation(summary = "会话列表", description = "获取当前登录用户的所有会话（按更新时间倒序）")
    public Result<List<ChatSession>> listSessions(
            @RequestHeader(value = "X-Token", required = false) String token) {
        String userId = getUserIdFromToken(token);
        List<ChatSession> sessions = chatSessionService.listSessions(userId);
        return Result.success(sessions);
    }

    /**
     * 创建新会话
     */
    @PostMapping("/session")
    @Operation(summary = "创建会话", description = "创建新的聊天会话")
    public Result<String> createSession(
            @RequestHeader(value = "X-Token", required = false) String token,
            @RequestParam(defaultValue = "新会话") String title) {
        String userId = getUserIdFromToken(token);
        String sessionId = chatSessionService.createSession(userId, title);
        log.info("创建会话: sessionId={}, userId={}", sessionId, userId);
        return Result.success(sessionId);
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/session/{sessionId}")
    @Operation(summary = "删除会话", description = "删除指定会话及其所有消息")
    public Result<Void> deleteSession(@PathVariable String sessionId) {
        log.info("删除会话: sessionId={}", sessionId);
        chatSessionService.deleteSession(sessionId);
        return Result.success();
    }

    private String getUserIdFromToken(String token) {
        if (token == null || token.isEmpty()) {
            return "anonymous";
        }
        String userId = userService.validateToken(token);
        return userId != null ? userId : "anonymous";
    }
}
