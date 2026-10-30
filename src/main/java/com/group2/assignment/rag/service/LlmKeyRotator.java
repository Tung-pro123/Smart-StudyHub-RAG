package com.group2.assignment.rag.service;

import com.group2.assignment.rag.config.LlmProperties;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service quản lý xoay vòng API Keys theo cơ chế Round-Robin kết hợp Cooldown & Failover tự động
 */
@Slf4j
@Service
public class LlmKeyRotator {

    private final LlmProperties llmProperties;
    private final List<String> configuredKeys = new ArrayList<>();
    private final AtomicInteger currentIndex = new AtomicInteger(0);
    private final Map<String, Long> cooldownUntilMap = new ConcurrentHashMap<>();
    private static final long COOLDOWN_DURATION_MS = 60_000L; // 60 giây nghỉ sau khi bị 429

    public LlmKeyRotator(LlmProperties llmProperties) {
        this.llmProperties = llmProperties;
        initKeys();
    }

    @PostConstruct
    public void initKeys() {
        this.configuredKeys.clear();
        if (llmProperties != null) {
            List<String> extracted = llmProperties.extractAllConfiguredKeys();
            if (extracted != null) {
                this.configuredKeys.addAll(extracted);
            }
        }
        log.info("LlmKeyRotator khởi tạo thành công với {} System API Keys", configuredKeys.size());
    }

    /**
     * Lấy API Key tiếp theo theo Round-Robin trong số các key đang hoạt động tốt (Healthy)
     */
    public synchronized String getNextKey() {
        if (configuredKeys.isEmpty()) {
            return null;
        }

        long now = System.currentTimeMillis();
        List<String> healthyKeys = new ArrayList<>();
        for (String k : configuredKeys) {
            long cooldownUntil = cooldownUntilMap.getOrDefault(k, 0L);
            if (now >= cooldownUntil) {
                healthyKeys.add(k);
            }
        }

        // Nếu có ít nhất 1 key khỏe mạnh -> Round-Robin
        if (!healthyKeys.isEmpty()) {
            int idx = Math.abs(currentIndex.getAndIncrement() % healthyKeys.size());
            return healthyKeys.get(idx);
        }

        // Nếu tất cả các key đều đang trong Cooldown -> Lấy key có thời gian cooldown sớm nhất
        log.warn("Tất cả {} key đều đang trong thời gian Cooldown. Đang fallback sang key phục hồi sớm nhất.", configuredKeys.size());
        return configuredKeys.stream()
                .min(Comparator.comparingLong(k -> cooldownUntilMap.getOrDefault(k, 0L)))
                .orElse(configuredKeys.get(0));
    }

    /**
     * Đánh dấu 1 key bị lỗi (đặc biệt là 429 Quota Exceeded / Resource Exhausted)
     */
    public void markKeyFailed(String key, int statusCode, String message) {
        if (key == null) return;

        boolean isQuotaError = statusCode == 429 ||
                (message != null && (message.contains("429") ||
                        message.contains("RESOURCE_EXHAUSTED") ||
                        message.toLowerCase().contains("quota exceeded") ||
                        message.toLowerCase().contains("rate limit")));

        long cooldown = isQuotaError ? COOLDOWN_DURATION_MS : 10_000L;
        long until = System.currentTimeMillis() + cooldown;
        cooldownUntilMap.put(key, until);

        log.warn("Key [{}] được đưa vào trạng thái Cooldown {} giây do lỗi [Status: {}, Lỗi: {}]",
                maskKey(key), cooldown / 1000, statusCode, message);
    }

    public boolean hasConfiguredKeys() {
        return !configuredKeys.isEmpty();
    }

    public int getTotalKeysCount() {
        return configuredKeys.size();
    }

    public int getHealthyKeysCount() {
        long now = System.currentTimeMillis();
        int count = 0;
        for (String k : configuredKeys) {
            if (now >= cooldownUntilMap.getOrDefault(k, 0L)) {
                count++;
            }
        }
        return count;
    }

    private String maskKey(String key) {
        if (key == null || key.length() <= 8) return "***";
        return key.substring(0, 4) + "..." + key.substring(key.length() - 4);
    }
}

