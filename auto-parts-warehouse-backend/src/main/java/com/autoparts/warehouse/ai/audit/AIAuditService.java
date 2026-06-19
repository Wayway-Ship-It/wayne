package com.autoparts.warehouse.ai.audit;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * AI 助手审计服务
 * 记录每次 LLM 调用的详细信息，支持查询和统计
 */
@Service
public class AIAuditService {

    /** 最近 N 条审计记录（内存中保存） */
    private static final int MAX_RECORDS = 500;

    private final List<AuditRecord> records = Collections.synchronizedList(new ArrayList<>());

    private final AtomicLong totalCalls = new AtomicLong(0);
    private final AtomicLong totalTokens = new AtomicLong(0);
    private final AtomicLong totalDurationMs = new AtomicLong(0);

    /**
     * 记录一次 LLM 调用
     */
    public AuditRecord log(String sessionId, String query, String response, int tokensUsed, long durationMs) {
        AuditRecord record = new AuditRecord();
        record.id = totalCalls.incrementAndGet();
        record.timestamp = Instant.now();
        record.sessionId = sessionId != null ? sessionId : "unknown";
        record.query = truncate(query, 200);
        record.response = truncate(response, 500);
        record.tokensUsed = tokensUsed;
        record.durationMs = durationMs;
        record.success = response != null && !response.isEmpty();

        totalTokens.addAndGet(tokensUsed);
        totalDurationMs.addAndGet(durationMs);

        synchronized (records) {
            records.add(record);
            while (records.size() > MAX_RECORDS) {
                records.remove(0);
            }
        }

        // 控制台审计日志
        System.out.printf("[AI-Audit] id=%d session=%s tokens=%d duration=%dms success=%s query=%s%n",
                record.id, record.sessionId, tokensUsed, durationMs, record.success,
                truncate(query, 80));

        return record;
    }

    /**
     * 记录失败调用
     */
    public AuditRecord logError(String sessionId, String query, String error, long durationMs) {
        AuditRecord record = new AuditRecord();
        record.id = totalCalls.incrementAndGet();
        record.timestamp = Instant.now();
        record.sessionId = sessionId != null ? sessionId : "unknown";
        record.query = truncate(query, 200);
        record.response = "[ERROR] " + truncate(error, 200);
        record.tokensUsed = 0;
        record.durationMs = durationMs;
        record.success = false;

        totalDurationMs.addAndGet(durationMs);

        synchronized (records) {
            records.add(record);
            while (records.size() > MAX_RECORDS) {
                records.remove(0);
            }
        }

        System.err.printf("[AI-Audit-ERROR] session=%s error=%s query=%s%n",
                record.sessionId, truncate(error, 80), truncate(query, 80));

        return record;
    }

    /**
     * 获取最近 N 条审计记录
     */
    public List<AuditRecord> getRecentRecords(int count) {
        synchronized (records) {
            int size = records.size();
            int from = Math.max(0, size - count);
            return new ArrayList<>(records.subList(from, size));
        }
    }

    /**
     * 获取统计摘要
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("totalCalls", totalCalls.get());
        stats.put("totalTokens", totalTokens.get());
        stats.put("avgTokensPerCall", totalCalls.get() > 0 ? totalTokens.get() / totalCalls.get() : 0);
        stats.put("avgDurationMs", totalCalls.get() > 0 ? totalDurationMs.get() / totalCalls.get() : 0);
        stats.put("recentRecords", records.size());

        // 成功率
        long successCount;
        synchronized (records) {
            successCount = records.stream().filter(r -> r.success).count();
        }
        int total = records.size();
        stats.put("successRate", total > 0 ? String.format("%.1f%%", 100.0 * successCount / total) : "N/A");

        return stats;
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        if (text.length() <= maxLen) return text;
        return text.substring(0, maxLen) + "...";
    }

    /**
     * 审计记录
     */
    public static class AuditRecord {
        public long id;
        public Instant timestamp;
        public String sessionId;
        public String query;
        public String response;
        public int tokensUsed;
        public long durationMs;
        public boolean success;
    }
}
