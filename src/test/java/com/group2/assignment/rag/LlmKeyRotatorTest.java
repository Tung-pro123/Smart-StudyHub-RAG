package com.group2.assignment.rag;

import com.group2.assignment.rag.config.LlmProperties;
import com.group2.assignment.rag.service.LlmKeyRotator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LlmKeyRotatorTest {

    @Test
    @DisplayName("Round-Robin xoay vòng đều qua các API Key")
    void testRoundRobinRotation() {
        LlmProperties props = new LlmProperties();
        props.setGeminiApiKeys(List.of("key-1", "key-2", "key-3"));

        LlmKeyRotator rotator = new LlmKeyRotator(props);

        assertTrue(rotator.hasConfiguredKeys());
        assertEquals(3, rotator.getTotalKeysCount());
        assertEquals(3, rotator.getHealthyKeysCount());

        assertEquals("key-1", rotator.getNextKey());
        assertEquals("key-2", rotator.getNextKey());
        assertEquals("key-3", rotator.getNextKey());
        assertEquals("key-1", rotator.getNextKey());
    }

    @Test
    @DisplayName("Khi 1 key dính lỗi 429, hệ thống đưa vào cooldown và bỏ qua key đó")
    void testFailoverAndCooldownOn429() {
        LlmProperties props = new LlmProperties();
        props.setGeminiApiKeys(List.of("key-A", "key-B", "key-C"));

        LlmKeyRotator rotator = new LlmKeyRotator(props);

        // Đánh dấu key-A bị lỗi 429 (ResourceExhausted)
        rotator.markKeyFailed("key-A", 429, "Resource exhausted rate limit");

        assertEquals(2, rotator.getHealthyKeysCount());

        // Lần lấy tiếp theo chỉ xoay giữa key-B và key-C
        String next1 = rotator.getNextKey();
        String next2 = rotator.getNextKey();
        String next3 = rotator.getNextKey();

        assertNotEquals("key-A", next1);
        assertNotEquals("key-A", next2);
        assertNotEquals("key-A", next3);
    }

    @Test
    @DisplayName("Nếu tất cả key đều bị cooldown, hệ thống tự động fallback sang key có sẵn tốt nhất")
    void testFallbackWhenAllKeysInCooldown() {
        LlmProperties props = new LlmProperties();
        props.setGeminiApiKeys(List.of("single-key"));

        LlmKeyRotator rotator = new LlmKeyRotator(props);
        rotator.markKeyFailed("single-key", 429, "Rate limit");

        // Không được crash hoặc trả về null khi tất cả bị cooldown
        String key = rotator.getNextKey();
        assertEquals("single-key", key);
    }

    @Test
    @DisplayName("Khởi tạo khi không có key nào được cấu hình")
    void testNoConfiguredKeys() {
        LlmProperties props = new LlmProperties();
        LlmKeyRotator rotator = new LlmKeyRotator(props);

        assertFalse(rotator.hasConfiguredKeys());
        assertNull(rotator.getNextKey());
        assertEquals(0, rotator.getTotalKeysCount());
    }
}

