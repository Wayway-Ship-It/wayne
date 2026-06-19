package com.autoparts.warehouse.ai.memory;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 对话会话管理器
 * 管理多轮对话的上下文记忆，支持按 sessionId 隔离会话
 *
 * 功能：
 * - 创建/获取会话
 * - 添加用户消息/AI 回复
 * - 获取完整对话历史（用于 LLM 请求）
 * - 清理过期会话
 */
@Service
public class ChatSessionManager {

    @Autowired
    private ChatMemoryStore memoryStore;

    /** 每个会话最多保留的对话轮数 */
    private static final int MAX_MESSAGES = 20;

    /** 会话默认 System Prompt */
    private static final String DEFAULT_SYSTEM_PROMPT =
            "你是汽车配件仓库管理系统的智能助手，可以帮用户查询库存、处理出入库、管理供应商和客户等。请用专业、友好的中文回答。";

    /**
     * 获取指定会话的对话历史
     *
     * @param sessionId 会话 ID
     * @return 对话消息列表（不含 SystemMessage）
     */
    public List<ChatMessage> getHistory(String sessionId) {
        List<ChatMessage> messages = memoryStore.getMessages(sessionId);
        if (messages == null) {
            return new ArrayList<>();
        }
        return messages;
    }

    /**
     * 获取完整对话列表（System Prompt + 历史消息）
     * 用于传递给 LLM
     *
     * @param sessionId     会话 ID
     * @param systemPrompt  本次请求的 System Prompt
     * @param userMessage   本次用户消息
     * @return 完整的消息列表
     */
    public List<ChatMessage> buildMessages(String sessionId, String systemPrompt, String userMessage) {
        List<ChatMessage> messages = new ArrayList<>();

        // 1. System Prompt
        String effectiveSystemPrompt = (systemPrompt != null && !systemPrompt.isEmpty())
                ? systemPrompt : DEFAULT_SYSTEM_PROMPT;
        messages.add(SystemMessage.from(effectiveSystemPrompt));

        // 2. 历史消息
        List<ChatMessage> history = getHistory(sessionId);
        messages.addAll(history);

        // 3. 当前用户消息
        messages.add(UserMessage.from(userMessage));

        return messages;
    }

    /**
     * 添加一轮对话到会话历史
     *
     * @param sessionId   会话 ID
     * @param userMessage 用户消息
     * @param aiResponse  AI 回复
     */
    public void addExchange(String sessionId, String userMessage, String aiResponse) {
        List<ChatMessage> history = getHistory(sessionId);
        history.add(UserMessage.from(userMessage));
        history.add(AiMessage.from(aiResponse));

        // 限制历史长度（保留最近 N 条）
        while (history.size() > MAX_MESSAGES) {
            history.remove(0);
            if (!history.isEmpty()) {
                history.remove(0); // 成对删除
            }
        }

        memoryStore.updateMessages(sessionId, history);
    }

    /**
     * 清空指定会话
     *
     * @param sessionId 会话 ID
     */
    public void clear(String sessionId) {
        memoryStore.deleteMessages(sessionId);
    }

    /**
     * 构建带历史上下文的用户消息
     * <p>
     * 将对话历史拼接为一个字符串，供 Function Calling 模式使用
     * （AiServices @UserMessage 只接受单个 String，无法直接传 List<ChatMessage>）
     *
     * @param sessionId   会话 ID
     * @param userMessage 当前用户消息
     * @return 包含上下文和当前问题的消息字符串
     */
    public String buildContextualMessage(String sessionId, String userMessage) {
        List<ChatMessage> history = getHistory(sessionId);
        if (history.isEmpty()) {
            return userMessage;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("【对话历史】\n");
        int count = 0;
        for (ChatMessage msg : history) {
            if (count >= 10) break; // 最多包含最近 5 轮对话
            if (msg instanceof UserMessage) {
                sb.append("用户：").append(((UserMessage) msg).singleText()).append("\n");
                count++;
            } else if (msg instanceof dev.langchain4j.data.message.AiMessage) {
                // 截断过长回复
                String text = ((dev.langchain4j.data.message.AiMessage) msg).text();
                if (text.length() > 200) {
                    text = text.substring(0, 200) + "...";
                }
                sb.append("助手：").append(text).append("\n");
            }
        }
        sb.append("\n【当前问题】\n");
        sb.append(userMessage);
        return sb.toString();
    }

    /**
     * 获取会话的消息数量
     *
     * @param sessionId 会话 ID
     * @return 消息数量
     */
    public int getMessageCount(String sessionId) {
        List<ChatMessage> messages = memoryStore.getMessages(sessionId);
        return messages != null ? messages.size() : 0;
    }
}
