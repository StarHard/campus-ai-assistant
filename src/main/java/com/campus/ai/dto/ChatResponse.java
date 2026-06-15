package com.campus.ai.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * AI聊天响应数据传输对象
 *
 * @author A组长
 */
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

    public ChatResponse() {
    }

    // ========== Getter & Setter ==========

    public String getId() {
        return id;
    }

    public ChatResponse setId(String id) {
        this.id = id;
        return this;
    }

    public String getSessionId() {
        return sessionId;
    }

    public ChatResponse setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public String getAnswer() {
        return answer;
    }

    public ChatResponse setAnswer(String answer) {
        this.answer = answer;
        return this;
    }

    public List<KnowledgeSource> getSources() {
        return sources;
    }

    public ChatResponse setSources(List<KnowledgeSource> sources) {
        this.sources = sources;
        return this;
    }

    public String getModel() {
        return model;
    }

    public ChatResponse setModel(String model) {
        this.model = model;
        return this;
    }

    public TokenUsage getTokenUsage() {
        return tokenUsage;
    }

    public ChatResponse setTokenUsage(TokenUsage tokenUsage) {
        this.tokenUsage = tokenUsage;
        return this;
    }

    public Long getLatency() {
        return latency;
    }

    public ChatResponse setLatency(Long latency) {
        this.latency = latency;
        return this;
    }

    public Boolean getRagEnabled() {
        return ragEnabled;
    }

    public ChatResponse setRagEnabled(Boolean ragEnabled) {
        this.ragEnabled = ragEnabled;
        return this;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public ChatResponse setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * 创建Builder实例
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder模式实现
     */
    public static class Builder {
        private final ChatResponse response = new ChatResponse();

        public Builder id(String id) {
            response.id = id;
            return this;
        }

        public Builder sessionId(String sessionId) {
            response.sessionId = sessionId;
            return this;
        }

        public Builder answer(String answer) {
            response.answer = answer;
            return this;
        }

        public Builder sources(List<KnowledgeSource> sources) {
            response.sources = sources;
            return this;
        }

        public Builder model(String model) {
            response.model = model;
            return this;
        }

        public Builder tokenUsage(TokenUsage tokenUsage) {
            response.tokenUsage = tokenUsage;
            return this;
        }

        public Builder latency(Long latency) {
            response.latency = latency;
            return this;
        }

        public Builder ragEnabled(Boolean ragEnabled) {
            response.ragEnabled = ragEnabled;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            response.metadata = metadata;
            return this;
        }

        public ChatResponse build() {
            return response;
        }
    }

    /**
     * 知识库来源内部类
     */
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

        public KnowledgeSource() {
        }

        public String getTitle() {
            return title;
        }

        public KnowledgeSource setTitle(String title) {
            this.title = title;
            return this;
        }

        public String getContent() {
            return content;
        }

        public KnowledgeSource setContent(String content) {
            this.content = content;
            return this;
        }

        public Double getScore() {
            return score;
        }

        public KnowledgeSource setScore(Double score) {
            this.score = score;
            return this;
        }

        public String getDocumentPath() {
            return documentPath;
        }

        public KnowledgeSource setDocumentPath(String documentPath) {
            this.documentPath = documentPath;
            return this;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private final KnowledgeSource source = new KnowledgeSource();

            public Builder title(String title) {
                source.title = title;
                return this;
            }

            public Builder content(String content) {
                source.content = content;
                return this;
            }

            public Builder score(Double score) {
                source.score = score;
                return this;
            }

            public Builder documentPath(String documentPath) {
                source.documentPath = documentPath;
                return this;
            }

            public KnowledgeSource build() {
                return source;
            }
        }
    }

    /**
     * Token使用统计内部类
     */
    public static class TokenUsage implements Serializable {
        private static final long serialVersionUID = 1L;

        /** 提示词token数 */
        private Integer promptTokens;

        /** 完成token数 */
        private Integer completionTokens;

        /** 总token数 */
        private Integer totalTokens;

        public TokenUsage() {
        }

        public Integer getPromptTokens() {
            return promptTokens;
        }

        public TokenUsage setPromptTokens(Integer promptTokens) {
            this.promptTokens = promptTokens;
            return this;
        }

        public Integer getCompletionTokens() {
            return completionTokens;
        }

        public TokenUsage setCompletionTokens(Integer completionTokens) {
            this.completionTokens = completionTokens;
            return this;
        }

        public Integer getTotalTokens() {
            return totalTokens;
        }

        public TokenUsage setTotalTokens(Integer totalTokens) {
            this.totalTokens = totalTokens;
            return this;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private final TokenUsage usage = new TokenUsage();

            public Builder promptTokens(Integer promptTokens) {
                usage.promptTokens = promptTokens;
                return this;
            }

            public Builder completionTokens(Integer completionTokens) {
                usage.completionTokens = completionTokens;
                return this;
            }

            public Builder totalTokens(Integer totalTokens) {
                usage.totalTokens = totalTokens;
                return this;
            }

            public TokenUsage build() {
                return usage;
            }
        }
    }
}
