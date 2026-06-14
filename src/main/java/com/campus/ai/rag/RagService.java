package com.campus.ai.rag;

import com.campus.ai.dto.ChatResponse;

import java.util.List;

/**
 * RAG（检索增强生成）服务接口
 * 结合Lucene或Vector DB实现本地校园文档知识库的精准检索
 *
 * @author A组长
 */
public interface RagService {

    /**
     * 检索相关上下文信息
     *
     * @param query 用户查询问题
     * @return 检索到的相关文档内容
     */
    String retrieveContext(String query);

    /**
     * 检索相关文档来源列表
     *
     * @param query 用户查询问题
     * @return 相关文档来源列表（包含相似度分数）
     */
    List<ChatResponse.KnowledgeSource> retrieveSources(String query);

    /**
     * 加载文档到知识库
     *
     * @param filePath 文件路径
     * @return 是否加载成功
     */
    boolean loadDocument(String filePath);

    /**
     * 批量加载目录下的所有文档到知识库
     *
     * @param directoryPath 目录路径
     * @return 成功加载的文档数量
     */
    int loadDocumentsFromDirectory(String directoryPath);

    /**
     * 清空知识库
     */
    void clearKnowledgeBase();

    /**
     * 获取知识库中已加载的文档数量
     *
     * @return 文档数量
     */
    int getDocumentCount();
}
