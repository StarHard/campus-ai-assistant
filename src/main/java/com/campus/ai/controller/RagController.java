package com.campus.ai.controller;

import com.campus.ai.dto.Result;
import com.campus.ai.rag.RagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RAG知识库管理控制器
 * 提供知识库文档的增删查接口
 *
 * @author A组长
 */
@RestController
@RequestMapping("/rag")
@Tag(name = "RAG知识库管理", description = "校园文档知识库管理接口")
public class RagController {

    private static final Logger log = LoggerFactory.getLogger(RagController.class);

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    /**
     * 上传文档到知识库
     */
    @PostMapping("/document/upload")
    @Operation(summary = "上传文档", description = "上传文档文件到知识库")
    public Result<Map<String, Object>> uploadDocument(@RequestParam("file") MultipartFile file) {
        log.info("上传文档: {}", file.getOriginalFilename());
        try {
            // 保存临时文件并加载到知识库
            String tempPath = System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename();
            file.transferTo(new java.io.File(tempPath));
            boolean success = ragService.loadDocument(tempPath);

            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            result.put("fileName", file.getOriginalFilename());
            result.put("documentCount", ragService.getDocumentCount());

            return Result.success("文档上传" + (success ? "成功" : "失败"), result);
        } catch (Exception e) {
            log.error("文档上传失败", e);
            return Result.error("文档上传失败: " + e.getMessage());
        }
    }

    /**
     * 从目录批量加载文档
     */
    @PostMapping("/documents/batch-load")
    @Operation(summary = "批量加载文档", description="从指定目录批量加载文档到知识库")
    public Result<Map<String, Object>> batchLoadDocuments(@RequestBody Map<String, String> request) {
        String directoryPath = request.getOrDefault("directoryPath", "./data/knowledge-base");
        log.info("批量加载文档，目录: {}", directoryPath);

        int count = ragService.loadDocumentsFromDirectory(directoryPath);

        Map<String, Object> result = new HashMap<>();
        result.put("loadedCount", count);
        result.put("totalDocuments", ragService.getDocumentCount());

        return Result.success(result);
    }

    /**
     * 获取知识库统计信息
     */
    @GetMapping("/stats")
    @Operation(summary = "知识库统计", description="获取当前知识库的文档数量和状态信息")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("documentCount", ragService.getDocumentCount());
        stats.put("status", "active");

        return Result.success(stats);
    }

    /**
     * 清空知识库
     */
    @DeleteMapping("/clear")
    @Operation(summary = "清空知识库", description="清空所有已加载的知识库文档")
    public Result<Void> clearKnowledgeBase() {
        log.warn("清空知识库");
        ragService.clearKnowledgeBase();
        return Result.success();
    }

    /**
     * 测试检索功能
     */
    @GetMapping("/test-retrieval")
    @Operation(summary = "测试检索", description="测试RAG检索功能是否正常")
    public Result<List<com.campus.ai.dto.ChatResponse.KnowledgeSource>> testRetrieval(
            @RequestParam String query) {
        log.info("测试检索: query={}", query);
        List<com.campus.ai.dto.ChatResponse.KnowledgeSource> sources =
                ragService.retrieveSources(query);
        return Result.success(sources);
    }
}
