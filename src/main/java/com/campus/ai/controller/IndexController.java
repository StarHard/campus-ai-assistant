package com.campus.ai.controller;

import com.campus.ai.dto.Result;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 首页/健康检查控制器
 *
 * @author A组长
 */
@RestController
public class IndexController {

    @GetMapping("/")
    @Operation(summary = "服务首页", description = "返回服务状态和可用接口列表")
    public Result<Map<String, Object>> index() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("service", "校园智能服务小助手");
        info.put("status", "running");
        info.put("version", "1.0.0");

        Map<String, String> endpoints = new LinkedHashMap<>();
        endpoints.put("GET /chat/simple?question=xxx", "简单问答");
        endpoints.put("POST /chat/ask", "同步智能问答");
        endpoints.put("POST /chat/stream", "流式智能问答(SSE)");
        endpoints.put("POST /rag/document/upload", "上传知识库文档");
        endpoints.put("GET /rag/stats", "知识库统计");
        endpoints.put("DELETE /rag/clear", "清空知识库");
        info.put("endpoints", endpoints);

        return Result.success(info);
    }
}
