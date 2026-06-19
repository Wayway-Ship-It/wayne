package com.autoparts.warehouse.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * AI 助手配置属性类
 * 映射 application.yml 中的 ai.* 配置
 */
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AIConfigProperties {

    /** 是否启用 AI 助手功能 */
    private boolean enabled = true;

    /** 阿里云百炼 API 密钥 */
    private String apiKey;

    /** 阿里云百炼 API 地址 */
    private String apiUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    /** 模型名称 */
    private String modelName = "qwen3-max";

    /** 嵌入模型名称 */
    private String embeddingModel = "text-embedding-v3";

    /** 超时配置 */
    private Timeout timeout = new Timeout();

    /** 速率限制配置 */
    private RateLimit rateLimit = new RateLimit();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getEmbeddingModel() {
        return embeddingModel;
    }

    public void setEmbeddingModel(String embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public Timeout getTimeout() {
        return timeout;
    }

    public void setTimeout(Timeout timeout) {
        this.timeout = timeout;
    }

    public RateLimit getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(RateLimit rateLimit) {
        this.rateLimit = rateLimit;
    }

    /**
     * 超时配置内部类
     */
    public static class Timeout {
        /** 连接超时（毫秒） */
        private int connect = 5000;

        /** 读取超时（毫秒） */
        private int read = 30000;

        public int getConnect() {
            return connect;
        }

        public void setConnect(int connect) {
            this.connect = connect;
        }

        public int getRead() {
            return read;
        }

        public void setRead(int read) {
            this.read = read;
        }
    }

    /**
     * 速率限制配置内部类
     */
    public static class RateLimit {
        /** 每分钟最大请求数 */
        private int maxPerMinute = 20;

        public int getMaxPerMinute() {
            return maxPerMinute;
        }

        public void setMaxPerMinute(int maxPerMinute) {
            this.maxPerMinute = maxPerMinute;
        }
    }
}
