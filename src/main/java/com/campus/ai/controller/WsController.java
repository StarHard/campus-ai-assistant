package com.campus.ai.controller;

import com.campus.ai.dto.Result;
import com.campus.ai.websocket.ChatWebSocketHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * WebSocket管理接口
 * 提供在线人数查询等HTTP接口
 *
 * @author A组长
 */
@RestController
@RequestMapping("/ws")
@Tag(name = "WebSocket管理", description = "WebSocket双向通信相关接口")
public class WsController {

    private final ChatWebSocketHandler chatWebSocketHandler;

    public WsController(ChatWebSocketHandler chatWebSocketHandler) {
        this.chatWebSocketHandler = chatWebSocketHandler;
    }

    /**
     * 获取当前在线人数
     */
    @GetMapping("/online-count")
    @Operation(summary = "在线人数", description = "获取当前WebSocket在线用户数")
    public Result<Integer> getOnlineCount() {
        return Result.success(chatWebSocketHandler.getOnlineCount());
    }
}
