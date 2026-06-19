package com.autoparts.warehouse.service;

import com.autoparts.warehouse.ai.client.LLMClient;
import com.autoparts.warehouse.ai.rag.VectorStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * RAG（检索增强生成）服务
 * 将用户问题通过向量检索匹配知识库片段，再调用 LLM 生成回答
 *
 * Pipeline: query → 向量检索(Top-K) → 构建增强Prompt → LLM生成 → 回答
 */
@Service
public class RAGService {

    @Autowired
    private VectorStoreService vectorStore;

    @Autowired
    private LLMClient llmClient;

    /**
     * 使用 RAG 检索知识库并生成回答
     *
     * @param query 用户查询（如"怎么新增供应商"）
     * @return AI 生成的回答
     */
    public String query(String query) {
        // 1. 向量检索：搜索最相关的知识库片段
        List<String> relevantSegments = vectorStore.search(query, 3);

        // 2. 如果没有检索到相关内容，直接调用 LLM
        if (relevantSegments.isEmpty()) {
            System.out.println("[RAG] 未检索到相关知识，使用 LLM 直接回答");
            String fallback = llmClient.chat("你是汽车配件仓库管理系统的智能助手，请回答用户的问题。\n\n问题：" + query);
            return fallback != null ? fallback : "抱歉，无法获取回答。";
        }

        // 3. 构建增强 Prompt
        String prompt = buildPrompt(query, relevantSegments);
        System.out.println("[RAG] 检索到 " + relevantSegments.size() + " 个相关片段，构建增强Prompt");

        // 4. 调用 LLM 生成回答
        String answer = llmClient.chat(prompt);
        return answer != null ? answer : "抱歉，无法获取回答。";
    }

    /**
     * 构建 RAG 增强 Prompt
     */
    private String buildPrompt(String query, List<String> segments) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一个汽车配件仓库管理系统的智能助手。\n\n");
        sb.append("请参考以下知识库内容回答用户的问题。如果知识库中没有相关信息，请根据你的常识回答并说明。\n\n");
        sb.append("=== 知识库参考内容 ===\n");

        for (int i = 0; i < segments.size(); i++) {
            sb.append("\n【参考片段 ").append(i + 1).append("】\n");
            sb.append(segments.get(i)).append("\n");
        }

        sb.append("\n=== 用户问题 ===\n");
        sb.append(query).append("\n\n");
        sb.append("请用清晰、专业的中文回答。如果是操作步骤，请列出分步说明。");

        return sb.toString();
    }
}
