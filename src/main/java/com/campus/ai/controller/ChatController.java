package com.campus.ai.controller;

import com.campus.ai.dto.ChatRequest;
import com.campus.ai.dto.ChatResponse;
import com.campus.ai.dto.Result;
import com.campus.ai.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
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
     * 清理会话历史
     */
    @DeleteMapping("/session/{sessionId}")
    @Operation(summary = "清理会话", description = "清理指定会话的历史记录")
    public Result<Void> clearSession(@PathVariable String sessionId) {
        log.info("清理会话: sessionId={}", sessionId);
        chatService.clearSession(sessionId);
        return Result.success();
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
}
