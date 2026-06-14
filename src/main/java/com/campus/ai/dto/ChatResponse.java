package com.campus.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * AI聊天响应数据传输对象
 *
 * @author A组长
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 响应ID */
    private String id;

    /** 会话ID */
    private String sessionId;

    /** AI回答内容 */
    private String answer;

    /** 引用的知识库来源（RAG检索结果） */
    private List<KnowledgeSource> sources;

    /** 使用的模型名称 */
    private String model;

    /** Token使用统计 */
    private TokenUsage tokenUsage;

    /** 响应耗时（毫秒） */
    private Long latency;

    /** 是否启用RAG */
    private Boolean ragEnabled;

    /** 额外元数据 */
    private Map<String, Object> metadata;

    /**
     * 知识库来源内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KnowledgeSource implements Serializable {
        private static final long serialVersionUID = 1L;

        /** 文档标题 */
        private String title;

        /** 文档内容片段 */
        private String content;

        /** 相似度分数（0-1） */
        private Double score;

        /** 来源文档路径 */
        private String documentPath;
    }

    /**
     * Token使用统计内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenUsage implements Serializable {
        private static final long serialVersionUID = 1L;

        /** 提示词token数 */
        private Integer promptTokens;

        /** 完成token数 */
        private Integer completionTokens;

        /** 总token数 */
        private Integer totalTokens;
    }
}
