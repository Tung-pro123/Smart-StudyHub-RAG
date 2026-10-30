package com.group2.assignment.settings.service;

import com.group2.assignment.settings.dto.ApiKeyDto;
import com.group2.assignment.settings.dto.PingResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SystemAiKeyService {
    private volatile String apiKey;
    private volatile PingResponseDto lastPing;
    private final ApiKeyService pingService;

    public SystemAiKeyService(@Value("${studyhub.ai.gemini-api-key:}") String apiKey, ApiKeyService pingService) {
        this.apiKey = apiKey;
        this.pingService = pingService;
    }

    public String getApiKey() {
        return isConfigured() ? apiKey : "";
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank() && !apiKey.startsWith("YOUR_");
    }

    public synchronized void update(String key) {
        String value = key == null ? "" : key.trim();
        if (!value.matches("[A-Za-z0-9_.-]{1,512}") || value.startsWith("YOUR_")) {
            throw new IllegalArgumentException("API key không hợp lệ. Vui lòng kiểm tra lại.");
        }
        apiKey = value;
        lastPing = null;
    }

    public synchronized PingResponseDto testPing() {
        ApiKeyDto dto = new ApiKeyDto();
        dto.setApiKey(getApiKey());
        lastPing = pingService.testConnection(dto, null);
        return lastPing;
    }

    public PingResponseDto getLastPing() {
        return lastPing;
    }
}
