package com.campus.ai.dto;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * AI聊天请求数据传输对象
 *
 * @author A组长
 */
public class ChatRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户问题（必填） */
    @NotBlank(message = "问题内容不能为空")
    private String question;

    /** 会话ID（用于多轮对话） */
    private String sessionId;

    /** 历史消息列表 */
    private List<Message> history;

    /** 是否启用RAG检索增强 */
    private Boolean enableRag = true;

    /** 自定义系统提示词 */
    private String systemPrompt;

    /** 模型参数配置 */
    private ModelConfig modelConfig;

    /** 附加上下文信息 */
    private Map<String, Object> context;

    public ChatRequest() {
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public List<Message> getHistory() {
        return history;
    }

    public void setHistory(List<Message> history) {
        this.history = history;
    }

    public Boolean getEnableRag() {
        return enableRag;
    }

    public void setEnableRag(Boolean enableRag) {
        this.enableRag = enableRag;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public ModelConfig getModelConfig() {
        return modelConfig;
    }

    public void setModelConfig(ModelConfig modelConfig) {
        this.modelConfig = modelConfig;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public void setContext(Map<String, Object> context) {
        this.context = context;
    }

    /**
     * 消息内部类
     */
    public static class Message implements Serializable {
        private static final long serialVersionUID = 1L;

        /** 角色：user/assistant/system */
        private String role;

        /** 消息内容 */
        private String content;

        public Message() {
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    /**
     * 模型配置内部类
     */
    public static class ModelConfig implements Serializable {
        private static final long serialVersionUID = 1L;

        /** 模型名称：qwen-plus/qwen-max/qwen-turbo等 */
        private String model;

        /** 温度参数（0-2，越高越随机） */
        private Double temperature;

        /** 最大生成token数 */
        private Integer maxTokens;

        /** TopP采样参数 */
        private Double topP;

        public ModelConfig() {
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public Double getTemperature() {
            return temperature;
        }

        public void setTemperature(Double temperature) {
            this.temperature = temperature;
        }

        public Integer getMaxTokens() {
            return maxTokens;
        }

        public void setMaxTokens(Integer maxTokens) {
            this.maxTokens = maxTokens;
        }

        public Double getTopP() {
            return topP;
        }

        public void setTopP(Double topP) {
            this.topP = topP;
        }
    }
}
