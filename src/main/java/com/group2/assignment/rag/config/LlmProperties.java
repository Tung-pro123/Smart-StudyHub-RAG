package com.group2.assignment.rag.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "studyhub.ai")
@Getter
@Setter
public class LlmProperties {

    /**
     * Danh sách nhiều Gemini API Key (cách nhau bởi dấu phẩy hoặc list)
     */
    private List<String> geminiApiKeys = new ArrayList<>();

    /**
     * Gemini API Key đơn (backward compatibility)
     */
    private String geminiApiKey;

    /**
     * URL của LiteLLM Proxy hoặc OpenAI-compatible API (tùy chọn)
     * Ví dụ: http://localhost:4000/v1 hoặc https://api.openai.com/v1
     */
    private String baseUrl;

    /**
     * Tên mô hình AI (mặc định gemini-2.5-flash)
     */
    private String model = "gemini-2.5-flash";

    /**
     * Trích xuất toàn bộ các key hợp lệ từ properties và biến môi trường
     */
    public List<String> extractAllConfiguredKeys() {
        List<String> keys = new ArrayList<>();

        // 1. Đọc từ geminiApiKeys list
        if (geminiApiKeys != null) {
            for (String raw : geminiApiKeys) {
                if (raw != null && !raw.isBlank()) {
                    for (String part : raw.split(",")) {
                        String clean = part.trim();
                        if (!clean.isBlank() && !keys.contains(clean)) {
                            keys.add(clean);
                        }
                    }
                }
            }
        }

        // 2. Đọc từ geminiApiKey đơn lẻ
        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            for (String part : geminiApiKey.split(",")) {
                String clean = part.trim();
                if (!clean.isBlank() && !keys.contains(clean)) {
                    keys.add(clean);
                }
            }
        }

        // 3. Fallback đọc từ biến môi trường GEMINI_API_KEYS / GEMINI_API_KEY
        String envKeys = System.getenv("GEMINI_API_KEYS");
        if (envKeys != null && !envKeys.isBlank()) {
            for (String part : envKeys.split(",")) {
                String clean = part.trim();
                if (!clean.isBlank() && !keys.contains(clean)) {
                    keys.add(clean);
                }
            }
        }

        String singleEnv = System.getenv("GEMINI_API_KEY");
        if (singleEnv != null && !singleEnv.isBlank() && !keys.contains(singleEnv.trim())) {
            keys.add(singleEnv.trim());
        }

        return keys;
    }
}

