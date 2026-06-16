package com.campus.ai.rag;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Lucene 全文索引服务
 * 使用 SmartCN 中文分词器实现专业级文档检索，替代简易 TF-IDF
 *
 * 核心能力：
 * 1. 中文智能分词（SmartCN）
 * 2. 倒排索引（毫秒级检索）
 * 3. BM25 相关性评分
 * 4. 关键词高亮
 *
 * @author A组长
 */
@Service
public class LuceneIndexService {

    private static final Logger log = LoggerFactory.getLogger(LuceneIndexService.class);

    /** Lucene 字段名常量 */
    public static final String FIELD_CONTENT = "content";
    public static final String FIELD_DOC_ID = "docId";
    public static final String FIELD_SOURCE_PATH = "sourcePath";

    @Value("${rag.lucene.index-path:./data/lucene-index}")
    private String indexPath;

    /** 中文分词器 */
    private final Analyzer analyzer = new SmartChineseAnalyzer();

    /** 索引写入器 */
    private IndexWriter indexWriter;

    /** 索引目录 */
    private FSDirectory directory;

    /**
     * 初始化：创建或打开 Lucene 索引
     */
    @PostConstruct
    public void init() {
        try {
            Path path = Paths.get(indexPath);
            path.getParent().toFile().mkdirs();
            directory = FSDirectory.open(path);

            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            indexWriter = new IndexWriter(directory, config);

            log.info("Lucene索引初始化完成，路径: {}", indexPath);
        } catch (IOException e) {
            log.error("Lucene索引初始化失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 应用关闭时释放资源
     */
    @PreDestroy
    public void destroy() {
        try {
            if (indexWriter != null) {
                indexWriter.close();
            }
            if (directory != null) {
                directory.close();
            }
            log.info("Lucene索引已关闭");
        } catch (IOException e) {
            log.error("关闭Lucene索引失败: {}", e.getMessage());
        }
    }

    /**
     * 添加文档到索引（支持增量更新）
     *
     * @param docId      文档标识（如文件名）
     * @param content    文档内容（分块后的文本）
     * @param sourcePath 原始文件路径
     */
    public void addDocument(String docId, String content, String sourcePath) {
        if (indexWriter == null || content == null || content.isBlank()) {
            return;
        }
        try {
            // 先删除同 docId 的旧文档（支持更新）
            indexWriter.deleteDocuments(new Term(FIELD_DOC_ID, docId));

            Document doc = new Document();
            // TextField：分词+索引+存储（用于搜索和高亮）
            doc.add(new TextField(FIELD_CONTENT, content, Field.Store.YES));
            // StoredField：仅存储不索引（用于展示来源信息）
            doc.add(new StoredField(FIELD_DOC_ID, docId));
            doc.add(new StoredField(FIELD_SOURCE_PATH, sourcePath));

            indexWriter.addDocument(doc);
            indexWriter.commit();
        } catch (IOException e) {
            log.error("添加文档到Lucene索引失败: docId={}", docId, e);
        }
    }

    /**
     * 批量添加文档
     */
    public void addDocuments(List<IndexDocument> documents) {
        if (indexWriter == null || documents == null || documents.isEmpty()) {
            return;
        }
        try {
            for (IndexDocument doc : documents) {
                Document luceneDoc = new Document();
                luceneDoc.add(new TextField(FIELD_CONTENT, doc.content, Field.Store.YES));
                luceneDoc.add(new StoredField(FIELD_DOC_ID, doc.docId));
                luceneDoc.add(new StoredField(FIELD_SOURCE_PATH, doc.sourcePath));
                indexWriter.addDocument(luceneDoc);
            }
            indexWriter.commit();
            log.info("批量添加 {} 个文档到Lucene索引", documents.size());
        } catch (IOException e) {
            log.error("批量添加文档失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 搜索文档（BM25 评分 + 高亮）
     *
     * @param queryText   用户查询文本
     * @param topK        返回前 K 个结果
     * @return 搜索结果列表
     */
    public List<SearchResult> search(String queryText, int topK) {
        List<SearchResult> results = new ArrayList<>();
        if (indexWriter == null || queryText == null || queryText.isBlank()) {
            return results;
        }

        try (DirectoryReader reader = DirectoryReader.open(indexWriter)) {
            IndexSearcher searcher = new IndexSearcher(reader);

            // 用 QueryParser 解析查询（自动中文分词，先转义特殊字符防止语法错误）
            QueryParser parser = new QueryParser(FIELD_CONTENT, analyzer);
            String escapedQuery = escapeQueryChars(queryText);
            Query query = parser.parse(escapedQuery);

            // 执行搜索
            TopDocs topDocs = searcher.search(query, topK);

            // 高亮器配置
            QueryScorer queryScorer = new QueryScorer(query);
            Highlighter highlighter = new Highlighter(
                    new SimpleHTMLFormatter("<mark>", "</mark>"),
                    queryScorer
            );
            Fragmenter fragmenter = new SimpleSpanFragmenter(queryScorer, 200);
            highlighter.setTextFragmenter(fragmenter);

            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document hitDoc = searcher.storedFields().document(scoreDoc.doc);
                String content = hitDoc.get(FIELD_CONTENT);
                String docId = hitDoc.get(FIELD_DOC_ID);
                String sourcePath = hitDoc.get(FIELD_SOURCE_PATH);

                // 提取高亮片段
                String highlightedContent = highlighter.getBestFragment(analyzer, FIELD_CONTENT, content);
                if (highlightedContent == null) {
                    // 内容太短或无匹配词时返回原文截断
                    highlightedContent = content.length() > 300 ? content.substring(0, 300) + "..." : content;
                }

                results.add(new SearchResult(
                        docId,
                        content,
                        highlightedContent,
                        sourcePath,
                        scoreDoc.score
                ));
            }

            log.info("Lucene搜索完成: query='{}', 命中{}条", queryText, results.size());

        } catch (Exception e) {
            log.error("Lucene搜索失败: query='{}'", queryText, e);
        }

        return results;
    }

    /**
     * 删除指定文档的所有索引
     */
    public void deleteDocument(String docId) {
        if (indexWriter == null) return;
        try {
            indexWriter.deleteDocuments(new Term(FIELD_DOC_ID, docId));
            indexWriter.commit();
            log.info("删除Lucene索引: docId={}", docId);
        } catch (IOException e) {
            log.error("删除索引失败: docId={}", docId, e);
        }
    }

    /**
     * 清空所有索引
     */
    public void clearAll() {
        if (indexWriter == null) return;
        try {
            indexWriter.deleteAll();
            indexWriter.commit();
            log.info("Lucene索引已全部清空");
        } catch (IOException e) {
            log.error("清空索引失败", e);
        }
    }

    /**
     * 获取索引文档总数
     */
    public int getIndexedDocCount() {
        if (indexWriter == null) return 0;
        try {
            return indexWriter.getDocStats().numDocs;
        } catch (Exception e) {
            return 0;
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 转义 Lucene 查询语法特殊字符
     * 防止用户输入 C++、A+B、转专业A→B 等导致 QueryParser 解析失败
     */
    private String escapeQueryChars(String query) {
        if (query == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < query.length(); i++) {
            char c = query.charAt(i);
            // Lucene 特殊字符：+ - && || ! ( ) { } [ ] ^ " ~ * ? : \ /
            if ("+-&|!(){}[]^\"~*?:\\/".indexOf(c) >= 0) {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.toString();
    }

    // ==================== 数据类 ====================

    /**
     * 待索引的文档
     */
    public static class IndexDocument {
        public final String docId;
        public final String content;
        public final String sourcePath;

        public IndexDocument(String docId, String content, String sourcePath) {
            this.docId = docId;
            this.content = content;
            this.sourcePath = sourcePath;
        }
    }

    /**
     * 搜索结果
     */
    public static class SearchResult {
        public final String docId;
        public final String originalContent;
        public final String highlightedContent;
        public final String sourcePath;
        public final float score;

        public SearchResult(String docId, String originalContent, String highlightedContent,
                           String sourcePath, float score) {
            this.docId = docId;
            this.originalContent = originalContent;
            this.highlightedContent = highlightedContent;
            this.sourcePath = sourcePath;
            this.score = score;
        }
    }
}
