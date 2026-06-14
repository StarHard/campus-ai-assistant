package com.campus.ai.rag;

import com.campus.ai.dto.ChatResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * 基于内存向量存储实现校园文档知识库的精准检索
 * （可后续升级为Milvus/Chroma等专业向量数据库）
 *
 * 核心功能：
 * 1. 文档加载与分块
 * 2. 文本向量化（使用简单的TF-IDF或BM25算法）
 * 3. 相似度检索
 * 4. 上下文组装
 *
 * @author A组长
 */
@Service
public class RagServiceImpl implements RagService {

    private static final Logger log = LoggerFactory.getLogger(RagServiceImpl.class);

    /** 知识库存储：文档ID -> 文档内容块列表 */
    private final Map<String, List<DocumentChunk>> knowledgeBase = new ConcurrentHashMap<>();

    /** 向量索引：用于快速检索 */
    private final List<VectorEntry> vectorIndex = Collections.synchronizedList(new ArrayList<>());

    @Value("${rag.knowledge-base.path:./data/knowledge-base}")
    private String knowledgeBasePath;

    @Value("${rag.retrieval.top-k:5}")
    private int topK;

    @Value("${rag.retrieval.similarity-threshold:0.7}")
    private double similarityThreshold;

    @Value("${rag.enabled:true}")
    private boolean ragEnabled;

    /**
     * 初始化：自动加载知识库目录下的文档
     */
    @PostConstruct
    public void init() {
        if (!ragEnabled) {
            log.info("RAG服务已禁用");
            return;
        }

        log.info("初始化RAG知识库...");
        try {
            File dir = new File(knowledgeBasePath);
            if (dir.exists() && dir.isDirectory()) {
                int count = loadDocumentsFromDirectory(knowledgeBasePath);
                log.info("RAG知识库初始化完成，共加载 {} 个文档", count);
            } else {
                // 创建知识库目录
                Files.createDirectories(Paths.get(knowledgeBasePath));
                log.info("创建知识库目录: {}", knowledgeBasePath);
            }
        } catch (Exception e) {
            log.warn("RAG知识库初始化失败: {}", e.getMessage());
        }
    }

    /**
     * 检索相关上下文信息
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

        // 组装上下文文本
        StringBuilder context = new StringBuilder();
        for (int i = 0; i < sources.size(); i++) {
            ChatResponse.KnowledgeSource source = sources.get(i);
            context.append(String.format("[来源%d] %s（相似度: %.2f）\n%s\n\n",
                    i + 1,
                    source.getTitle(),
                    source.getScore(),
                    source.getContent()));
        }

        return context.toString();
    }

    /**
     * 检索相关文档来源列表
     */
    @Override
    public List<ChatResponse.KnowledgeSource> retrieveSources(String query) {
        if (!ragEnabled || vectorIndex.isEmpty()) {
            return Collections.emptyList();
        }

        // 计算查询向量
        double[] queryVector = textToVector(query);

        // 计算相似度并排序
        List<ScoredDocument> scoredDocs = vectorIndex.stream()
                .map(entry -> new ScoredDocument(
                        entry.chunk,
                        cosineSimilarity(queryVector, entry.vector)))
                .filter(doc -> doc.score >= similarityThreshold)
                .sorted(Comparator.comparingDouble((ScoredDocument d) -> d.score).reversed())
                .limit(topK)
                .collect(Collectors.toList());

        // 转换为KnowledgeSource格式
        return scoredDocs.stream()
                .map(doc -> ChatResponse.KnowledgeSource.builder()
                        .title(doc.chunk.documentId)
                        .content(doc.chunk.content)
                        .score(doc.score)
                        .documentPath(doc.chunk.sourcePath)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 加载单个文档到知识库
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

            // 向量化并添加到索引
            for (DocumentChunk chunk : chunks) {
                double[] vector = textToVector(chunk.content);
                vectorIndex.add(new VectorEntry(chunk, vector));
            }

            knowledgeBase.put(fileName, chunks);
            log.info("成功加载文档: {}, 分块数: {}", fileName, chunks.size());
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
        vectorIndex.clear();
        log.info("知识库已清空");
    }

    @Override
    public int getDocumentCount() {
        return knowledgeBase.size();
    }

    /**
     * 文本分块（按段落和句子进行智能分割）
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
     * 文本向量化（简化版TF-IDF向量化）
     * 实际项目中可替换为专业的Embedding模型（如通义千问的text-embedding模型）
     */
    private double[] textToVector(String text) {
        // 使用简单的词频统计作为向量表示
        Map<String, Integer> wordFreq = new HashMap<>();
        String[] words = text.toLowerCase().replaceAll("[^a-zA-Z0-9\u4e00-\u9fa5]", " ").split("\\s+");

        for (String word : words) {
            if (word.length() > 1) { // 过滤单字
                wordFreq.merge(word, 1, Integer::sum);
            }
        }

        // 返回基于词频的稀疏向量（这里简化为固定长度的特征向量）
        double[] vector = new double[256]; // 固定维度
        for (Map.Entry<String, Integer> entry : wordFreq.entrySet()) {
            int index = Math.abs(entry.getKey().hashCode() % 256);
            vector[index] += entry.getValue();
        }

        // 归一化
        double norm = 0;
        for (double v : vector) norm += v * v;
        norm = Math.sqrt(norm);
        if (norm > 0) {
            for (int i = 0; i < vector.length; i++) {
                vector[i] /= norm;
            }
        }

        return vector;
    }

    /**
     * 计算余弦相似度
     */
    private double cosineSimilarity(double[] vec1, double[] vec2) {
        double dotProduct = 0;
        double norm1 = 0;
        double norm2 = 0;

        for (int i = 0; i < Math.min(vec1.length, vec2.length); i++) {
            dotProduct += vec1[i] * vec2[i];
            norm1 += vec1[i] * vec1[i];
            norm2 += vec2[i] * vec2[i];
        }

        if (norm1 == 0 || norm2 == 0) return 0;
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * 文档内容块内部类
     */
    private static class DocumentChunk {
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

    /**
     * 向量索引条目内部类
     */
    private static class VectorEntry {
        DocumentChunk chunk;
        double[] vector;

        VectorEntry(DocumentChunk chunk, double[] vector) {
            this.chunk = chunk;
            this.vector = vector;
        }
    }

    /**
     * 评分文档内部类
     */
    private static class ScoredDocument {
        DocumentChunk chunk;
        double score;

        ScoredDocument(DocumentChunk chunk, double score) {
            this.chunk = chunk;
            this.score = score;
        }
    }
}
