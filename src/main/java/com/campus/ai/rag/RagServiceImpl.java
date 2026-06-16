package com.campus.ai.rag;

import com.campus.ai.dto.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * RAG（检索增强生成）服务实现类
 * 基于 Apache Lucene 全文搜索引擎实现校园文档知识库的精准检索
 * 使用 SmartCN 中文分词器 + BM25 评分 + 关键词高亮
 *
 * 核心功能：
 * 1. 文档加载与分块
 * 2. Lucene 倒排索引检索
 * 3. BM25 相关性评分
 * 4. 上下文组装与高亮
 *
 * @author A组长
 */
@Service
public class RagServiceImpl implements RagService {

    private static final Logger log = LoggerFactory.getLogger(RagServiceImpl.class);

    /** 知识库存储：文档ID -> 文档内容块列表 */
    private final Map<String, List<DocumentChunk>> knowledgeBase = new ConcurrentHashMap<>();

    /** Lucene 全文索引服务 */
    @Autowired
    private LuceneIndexService luceneIndexService;

    @Value("${rag.knowledge-base.path:./data/knowledge-base}")
    private String knowledgeBasePath;

    @Value("${rag.retrieval.top-k:5}")
    private int topK;

    @Value("${rag.enabled:true}")
    private boolean ragEnabled;

    /**
     * 初始化：自动加载知识库目录下的文档到 Lucene 索引
     */
    @PostConstruct
    public void init() {
        if (!ragEnabled) {
            log.info("RAG服务已禁用");
            return;
        }

        log.info("初始化RAG知识库（Lucene全文检索引擎）...");
        try {
            File dir = new File(knowledgeBasePath);
            if (dir.exists() && dir.isDirectory()) {
                int count = loadDocumentsFromDirectory(knowledgeBasePath);
                log.info("RAG知识库初始化完成，共加载 {} 个文档，Lucene索引 {} 条",
                        count, luceneIndexService.getIndexedDocCount());
            } else {
                Files.createDirectories(Paths.get(knowledgeBasePath));
                log.info("创建知识库目录: {}", knowledgeBasePath);
            }
        } catch (Exception e) {
            log.warn("RAG知识库初始化失败: {}", e.getMessage());
        }
    }

    /**
     * 检索相关上下文信息（用于注入 AI Prompt）
     */
    @Override
    public String retrieveContext(String query) {
        if (!ragEnabled || query == null || query.trim().isEmpty()) {
            return null;
        }

        List<ChatResponse.KnowledgeSource> sources = retrieveSources(query);
        if (sources.isEmpty()) {
            return null;
        }

        // 组装上下文文本，包含高亮信息
        StringBuilder context = new StringBuilder();
        for (int i = 0; i < sources.size(); i++) {
            ChatResponse.KnowledgeSource source = sources.get(i);
            context.append(String.format("[来源%d] %s（相关度: %.2f）\n%s\n\n",
                    i + 1,
                    source.getTitle(),
                    source.getScore(),
                    source.getContent()));
        }

        return context.toString();
    }

    /**
     * 检索相关文档来源列表（使用 Lucene BM25 搜索）
     */
    @Override
    public List<ChatResponse.KnowledgeSource> retrieveSources(String query) {
        if (!ragEnabled) {
            return Collections.emptyList();
        }

        // 使用 Lucene 全文搜索
        List<LuceneIndexService.SearchResult> luceneResults =
                luceneIndexService.search(query, topK);

        if (luceneResults.isEmpty()) {
            return Collections.emptyList();
        }

        // 转换为 KnowledgeSource 格式（保留高亮内容）
        return luceneResults.stream()
                .map(result -> ChatResponse.KnowledgeSource.builder()
                        .title(result.docId)
                        .content(result.highlightedContent)
                        .score((double) result.score)
                        .documentPath(result.sourcePath)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 加载单个文档到知识库并建立 Lucene 索引
     */
    @Override
    public boolean loadDocument(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                log.warn("文件不存在: {}", filePath);
                return false;
            }

            String fileName = path.getFileName().toString();
            String content = Files.readString(path);

            // 文本分块
            List<DocumentChunk> chunks = splitText(content, fileName, filePath);

            // 将每个分块添加到 Lucene 索引
            for (DocumentChunk chunk : chunks) {
                luceneIndexService.addDocument(
                        fileName + "#" + chunk.chunkIndex,
                        chunk.content,
                        filePath
                );
            }

            knowledgeBase.put(fileName, chunks);
            log.info("成功加载文档: {}, 分块数: {}, 已建立Lucene索引",
                    fileName, chunks.size());
            return true;

        } catch (IOException e) {
            log.error("加载文档失败: {}", filePath, e);
            return false;
        }
    }

    /**
     * 批量加载目录下的所有文档
     */
    @Override
    public int loadDocumentsFromDirectory(String directoryPath) {
        File dir = new File(directoryPath);
        if (!dir.exists() || !dir.isDirectory()) {
            log.warn("目录不存在: {}", directoryPath);
            return 0;
        }

        File[] files = dir.listFiles(file -> {
            String name = file.getName().toLowerCase();
            return name.endsWith(".txt") || name.endsWith(".md") ||
                   name.endsWith(".pdf") || name.endsWith(".doc") ||
                   name.endsWith(".docx");
        });

        if (files == null || files.length == 0) {
            log.info("目录下没有可加载的文档: {}", directoryPath);
            return 0;
        }

        int successCount = 0;
        for (File file : files) {
            if (loadDocument(file.getAbsolutePath())) {
                successCount++;
            }
        }

        return successCount;
    }

    @Override
    public void clearKnowledgeBase() {
        knowledgeBase.clear();
        luceneIndexService.clearAll();
        log.info("知识库已清空（含Lucene索引）");
    }

    @Override
    public int getDocumentCount() {
        return knowledgeBase.size();
    }

    /**
     * 文本分块（按段落和句子进行智能分割）
     * 每个块最大约500字符，保持语义完整性
     */
    private List<DocumentChunk> splitText(String text, String documentId, String sourcePath) {
        List<DocumentChunk> chunks = new ArrayList<>();

        // 按双换行符分割段落
        String[] paragraphs = text.split("\n\n+");

        int chunkIndex = 0;
        StringBuilder currentChunk = new StringBuilder();

        for (String paragraph : paragraphs) {
            paragraph = paragraph.trim();
            if (paragraph.isEmpty()) continue;

            // 如果当前块加上新段落后超过500字符，则保存当前块
            if (currentChunk.length() + paragraph.length() > 500 && currentChunk.length() > 0) {
                chunks.add(new DocumentChunk(
                        documentId,
                        chunkIndex++,
                        currentChunk.toString().trim(),
                        sourcePath
                ));
                currentChunk = new StringBuilder();
            }
            currentChunk.append(paragraph).append("\n\n");
        }

        // 保存最后一个块
        if (currentChunk.length() > 0) {
            chunks.add(new DocumentChunk(
                    documentId,
                    chunkIndex++,
                    currentChunk.toString().trim(),
                    sourcePath
            ));
        }

        return chunks;
    }

    /**
     * 文档内容块内部类
     */
    static class DocumentChunk {
        String documentId;
        int chunkIndex;
        String content;
        String sourcePath;

        DocumentChunk(String documentId, int chunkIndex, String content, String sourcePath) {
            this.documentId = documentId;
            this.chunkIndex = chunkIndex;
            this.content = content;
            this.sourcePath = sourcePath;
        }
    }
}
