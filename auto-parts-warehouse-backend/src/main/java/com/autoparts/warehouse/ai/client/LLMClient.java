package com.autoparts.warehouse.ai.client;

import com.autoparts.warehouse.ai.config.AIConfigProperties;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 统一 LLM 调用客户端
 * 封装 LangChain4j ChatModel，提供带降级的调用方法
 *
 * 使用方式：
 * - AIService 意图识别：chatWithSystem(systemPrompt, userMessage)
 * - RAGService 知识问答：chatWithSystem(systemPrompt, userMessage)
 * - 简单对话：chat(userMessage)
 */
@Service
public class LLMClient {

    @Autowired
    private ChatModel chatModel;

    @Autowired
    private AIConfigProperties aiConfig;

    /**
     * 带系统提示的对话（意图识别、RAG 场景）
     *
     * @param systemPrompt 系统提示词
     * @param userMessage  用户消息
     * @return LLM 返回的文本内容，失败时返回 null
     */
    public String chatWithSystem(String systemPrompt, String userMessage) {
        if (!isAvailable()) {
            System.err.println("LLM不可用: enabled=" + aiConfig.isEnabled() + ", hasKey=" + hasApiKey());
            return null;
        }

        try {
            ChatResponse response = chatModel.chat(
                    SystemMessage.from(systemPrompt),
                    UserMessage.from(userMessage)
            );

            if (response != null && response.aiMessage() != null) {
                return response.aiMessage().text();
            }
            System.err.println("LLM返回空响应");
            return null;
        } catch (Exception e) {
            System.err.println("LLM调用失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 简单对话（无系统提示，聊天场景）
     *
     * @param userMessage 用户消息
     * @return LLM 返回的文本内容，失败时返回 null
     */
    public String chat(String userMessage) {
        if (!isAvailable()) {
            System.err.println("LLM不可用");
            return null;
        }

        try {
            return chatModel.chat(userMessage);
        } catch (Exception e) {
            System.err.println("LLM简单调用失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * LLM 服务是否可用
     */
    public boolean isAvailable() {
        return aiConfig.isEnabled() && hasApiKey();
    }

    /**
     * 是否配置了 API Key
     */
    public boolean hasApiKey() {
        String key = aiConfig.getApiKey();
        return key != null && !key.isEmpty();
    }
}
