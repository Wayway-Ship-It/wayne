package com.autoparts.warehouse.ai.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * DashScope（阿里云百炼）LangChain4j 配置
 * 通过 OpenAI 兼容模式对接阿里云百炼 Qwen 系列模型
 *
 * 提供的 Bean:
 * - ChatModel: 对话模型（qwen3-max）
 * - StreamingChatModel: 流式对话模型（qwen3-max）
 * - EmbeddingModel: 文本嵌入模型（text-embedding-v3）
 * - EmbeddingStore<TextSegment>: 内存向量存储
 * - ChatMemoryStore: 对话记忆存储
 *
 * Function Calling 改为 Prompt-Based 模式（DashScope 不原生支持 OpenAI tool_calls）：
 * - 工具定义通过 System Prompt 传递给 LLM
 * - LLM 返回 JSON 格式的工具调用指令
 * - AIService 解析并执行对应的 WarehouseTools 方法
 */
@Configuration
public class DashScopeConfig {

    @Autowired
    private AIConfigProperties aiConfig;

    @Bean
    public ChatModel chatModel() {
        String baseUrl = getBaseUrl();
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(aiConfig.getApiKey())
                .modelName(aiConfig.getModelName())
                .timeout(Duration.ofMillis(aiConfig.getTimeout().getRead()))
                .maxRetries(1)
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        String baseUrl = getBaseUrl();
        return OpenAiEmbeddingModel.builder()
                .baseUrl(baseUrl)
                .apiKey(aiConfig.getApiKey())
                .modelName(aiConfig.getEmbeddingModel())
                .timeout(Duration.ofMillis(aiConfig.getTimeout().getRead()))
                .maxRetries(1)
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    @Bean
    public StreamingChatModel streamingChatModel() {
        String baseUrl = getBaseUrl();
        return OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(aiConfig.getApiKey())
                .modelName(aiConfig.getModelName())
                .timeout(Duration.ofMillis(aiConfig.getTimeout().getRead()))
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    @Bean
    public ChatMemoryStore chatMemoryStore() {
        return new InMemoryChatMemoryStore();
    }

    private String getBaseUrl() {
        String apiUrl = aiConfig.getApiUrl();
        if (apiUrl.endsWith("/chat/completions")) {
            return apiUrl.substring(0, apiUrl.length() - "/chat/completions".length());
        }
        return apiUrl;
    }
}
