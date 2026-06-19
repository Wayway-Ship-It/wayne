package com.autoparts.warehouse.ai.audit;

import com.autoparts.warehouse.ai.config.AIConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AI API 速率限制服务
 * 使用滑动窗口算法，按 sessionId 限制请求频率
 */
@Service
public class RateLimitService {

    @Autowired
    private AIConfigProperties aiConfig;

    /** 每个 sessionId 的请求计数（时间戳 → 计数） */
    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();

    /** 每分钟自动清理过期条目 */
    private long lastCleanup = System.currentTimeMillis();

    /**
     * 检查指定 session 是否超过速率限制
     *
     * @param sessionId 会话 ID
     * @return true = 允许请求, false = 触发限流
     */
    public boolean tryAcquire(String sessionId) {
        int maxPerMinute = aiConfig.getRateLimit().getMaxPerMinute();
        if (maxPerMinute <= 0) return true; // 未启用限流

        cleanExpired();

        WindowCounter counter = counters.computeIfAbsent(sessionId, k -> new WindowCounter());

        synchronized (counter) {
            long now = System.currentTimeMillis();
            // 重置过期窗口
            if (now - counter.windowStart > 60_000) {
                counter.windowStart = now;
                counter.count.set(0);
            }
            int current = counter.count.incrementAndGet();
            return current <= maxPerMinute;
        }
    }

    /**
     * 获取当前限流状态
     */
    public Map<String, Object> getStatus(String sessionId) {
        int maxPerMinute = aiConfig.getRateLimit().getMaxPerMinute();

        Map<String, Object> status = new ConcurrentHashMap<>();
        status.put("maxPerMinute", maxPerMinute);
        status.put("enabled", maxPerMinute > 0);

        WindowCounter counter = counters.get(sessionId);
        if (counter != null) {
            synchronized (counter) {
                long now = System.currentTimeMillis();
                if (now - counter.windowStart > 60_000) {
                    status.put("currentCount", 0);
                    status.put("remaining", maxPerMinute);
                } else {
                    int current = counter.count.get();
                    status.put("currentCount", current);
                    status.put("remaining", Math.max(0, maxPerMinute - current));
                }
            }
        } else {
            status.put("currentCount", 0);
            status.put("remaining", maxPerMinute);
        }

        return status;
    }

    /**
     * 清理过期的窗口计数器
     */
    private void cleanExpired() {
        long now = System.currentTimeMillis();
        if (now - lastCleanup < 120_000) return; // 每 2 分钟清理一次
        lastCleanup = now;

        counters.entrySet().removeIf(entry -> {
            synchronized (entry.getValue()) {
                return now - entry.getValue().windowStart > 120_000;
            }
        });
    }

    /**
     * 滑动窗口计数器
     */
    private static class WindowCounter {
        long windowStart = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger(0);
    }
}
