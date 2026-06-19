package com.autoparts.warehouse.controller;

import com.autoparts.warehouse.ai.audit.AIAuditService;
import com.autoparts.warehouse.ai.audit.RateLimitService;
import com.autoparts.warehouse.ai.memory.ChatSessionManager;
import com.autoparts.warehouse.common.Result;
import com.autoparts.warehouse.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

/**
 * AI助手控制器 — Prompt-Based Function Calling
 *
 * DashScope 不原生支持 OpenAI Function Calling，改用 Prompt 描述工具 + JSON 返回。
 * 所有 LLM 调用统一通过 AIService.processMessage() 处理。
 *
 * 端点：
 * - POST /api/ai/chat    — 非流式对话
 * - GET  /api/ai/chat/stream — SSE 流式对话（模拟打字效果）
 * - GET  /api/ai/status  — AI 状态
 * - GET  /api/ai/stats   — 用量统计
 */
@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Autowired private AIService aiService;
    @Autowired private ChatSessionManager sessionManager;
    @Autowired private AIAuditService auditService;
    @Autowired private RateLimitService rateLimiter;

    // ==================== 非流式端点 ====================

    @PostMapping("/chat")
    public Result<Map<String, Object>> chat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        if (message == null || message.trim().isEmpty()) {
            return Result.error("请输入您的问题");
        }

        String sessionId = request.getOrDefault("sessionId", "default");
        if (!rateLimiter.tryAcquire(sessionId)) {
            return Result.error(429, "请求过于频繁，请稍后再试。（限制: "
                    + rateLimiter.getStatus(sessionId).get("maxPerMinute") + " 次/分钟）");
        }

        long start = System.currentTimeMillis();
        Map<String, Object> response = aiService.processMessage(message);
        long duration = System.currentTimeMillis() - start;

        String content = (String) response.get("content");
        auditService.log(sessionId, message,
                content != null ? content : response.toString(),
                estimateTokens(message + (content != null ? content : "")), duration);

        return Result.success(response);
    }

    // ==================== SSE 流式端点 ====================

    @GetMapping("/chat/stream")
    public SseEmitter chatStream(@RequestParam String message,
                                  @RequestParam(defaultValue = "default") String sessionId) {
        SseEmitter emitter = new SseEmitter(120000L);

        if (message == null || message.trim().isEmpty()) {
            completeWithError(emitter, "请输入您的问题");
            return emitter;
        }

        if (!rateLimiter.tryAcquire(sessionId)) {
            completeWithError(emitter, "请求过于频繁，请稍后再试。（限制: "
                    + rateLimiter.getStatus(sessionId).get("maxPerMinute") + " 次/分钟）");
            return emitter;
        }

        final long startTime = System.currentTimeMillis();

        // 1. 极简问候快速路径
        Map<String, Object> localResult = aiService.tryLocalMatch(message);
        if (localResult != null) {
            long duration = System.currentTimeMillis() - startTime;
            String content = (String) localResult.get("content");
            auditService.log(sessionId, message, content, estimateTokens(message + content), duration);
            sendAndComplete(emitter, "result", Result.success(localResult));
            return emitter;
        }

        // 2. 构建带历史的上下文消息
        String contextualMessage = sessionManager.buildContextualMessage(sessionId, message);

        // 3. 异步处理（Prompt-Based Function Calling + 模拟流式输出）
        final String userMsg = message;
        new Thread(() -> {
            try {
                long llmStart = System.currentTimeMillis();
                Map<String, Object> result = aiService.processMessage(contextualMessage);
                long llmDuration = System.currentTimeMillis() - llmStart;

                String fullResponse = (String) result.get("content");
                System.out.printf("[AI-Stream] Prompt-Based FC 完成, 耗时=%dms, 输出长度=%d%n",
                        llmDuration, fullResponse != null ? fullResponse.length() : 0);

                // 保存对话历史
                if (fullResponse != null && !fullResponse.isEmpty()) {
                    sessionManager.addExchange(sessionId, userMsg, fullResponse);
                }

                // 审计日志
                long duration = System.currentTimeMillis() - startTime;
                auditService.log(sessionId, userMsg, fullResponse != null ? fullResponse : "",
                        estimateTokens(userMsg + (fullResponse != null ? fullResponse : "")), duration);

                // 导航响应
                if ("navigate".equals(result.get("type"))) {
                    emitter.send(SseEmitter.event().name("result").data(Result.success(result)));
                    emitter.send(SseEmitter.event().name("done")
                            .data(Result.success(Map.of("sessionId", sessionId,
                                    "historyCount", sessionManager.getMessageCount(sessionId)))));
                    emitter.complete();
                    return;
                }

                // 模拟流式输出
                if (fullResponse != null) {
                    streamTextContent(emitter, fullResponse, sessionId);
                } else {
                    completeWithError(emitter, "AI 服务返回为空");
                }

            } catch (Exception e) {
                System.err.println("SSE 流式处理失败: " + e.getMessage());
                e.printStackTrace();
                completeWithError(emitter, "处理失败: " + e.getMessage());
            }
        }, "sse-" + sessionId.substring(0, Math.min(8, sessionId.length()))).start();

        return emitter;
    }

    // ==================== 辅助端点 ====================

    @GetMapping("/status")
    public Result<Map<String, Object>> getStatus() {
        Map<String, Object> status = aiService.getStatus();
        status.putAll(auditService.getStats());
        return Result.success(status);
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats(@RequestParam(defaultValue = "default") String sessionId) {
        Map<String, Object> stats = new java.util.LinkedHashMap<>();
        stats.put("audit", auditService.getStats());
        stats.put("rateLimit", rateLimiter.getStatus(sessionId));
        stats.put("recentRecords", auditService.getRecentRecords(10));
        return Result.success(stats);
    }

    // ==================== 内部方法 ====================

    private void streamTextContent(SseEmitter emitter, String text, String sessionId) {
        try {
            for (int i = 0; i < text.length(); ) {
                int chunkSize = chunkSize(text, i);
                int end = Math.min(i + chunkSize, text.length());
                String chunk = text.substring(i, end);
                emitter.send(SseEmitter.event().name("token").data(chunk));
                i = end;
                try { Thread.sleep(20 + (int)(Math.random() * 30)); } catch (InterruptedException ignored) {}
            }
            emitter.send(SseEmitter.event().name("done")
                    .data(Result.success(Map.of(
                            "sessionId", sessionId,
                            "historyCount", sessionManager.getMessageCount(sessionId)))));
            emitter.complete();
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }

    private int chunkSize(String text, int pos) {
        char c = text.charAt(pos);
        if (c == '，' || c == '。' || c == '、' || c == '；' || c == '：' || c == '\n') return 1;
        if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) {
            return 2 + (int)(Math.random() * 3);
        }
        return 3 + (int)(Math.random() * 4);
    }

    private void sendAndComplete(SseEmitter emitter, String event, Object data) {
        try {
            emitter.send(SseEmitter.event().name(event).data(data));
            emitter.complete();
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
    }

    private void completeWithError(SseEmitter emitter, String errorMsg) {
        try {
            emitter.send(SseEmitter.event().name("error").data(Result.error(errorMsg)));
            emitter.complete();
        } catch (IOException ignored) {}
    }

    private int estimateTokens(String text) {
        if (text == null || text.isEmpty()) return 0;
        int chinese = 0, other = 0;
        for (char c : text.toCharArray()) {
            if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                    || Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS) {
                chinese++;
            } else {
                other++;
            }
        }
        return (int) (chinese / 1.5 + other / 4.0) + 1;
    }
}
