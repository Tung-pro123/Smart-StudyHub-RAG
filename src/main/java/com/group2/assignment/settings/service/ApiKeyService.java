package com.group2.assignment.settings.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.group2.assignment.settings.SettingsConstants;
import com.group2.assignment.settings.dto.ApiKeyDto;
import com.group2.assignment.settings.dto.PingResponseDto;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ApiKeyService {
    private final RestClient client;
    private final String geminiModel;

    public ApiKeyService(@Qualifier("settingsRestClient") RestClient client,
                         @Value("${settings.gemini.model:" + SettingsConstants.DEFAULT_MODEL + "}") String geminiModel) {
        this.client = client;
        this.geminiModel = geminiModel;
    }

    public String normalizeProvider(String provider) {
        String value = provider == null ? "GEMINI" : provider.trim().toUpperCase(Locale.ROOT);
        if (!List.of("GEMINI", "GROQ", "OPENAI").contains(value)) {
            throw new IllegalArgumentException("Nhà cung cấp không được hỗ trợ.");
        }
        return value;
    }

    private String normalizeKey(String key) {
        String value = key == null ? "" : key.trim();
        if (value.isEmpty() || value.length() > 512 || !value.matches("[A-Za-z0-9_\\-\\.]+")) {
            throw new IllegalArgumentException("Vui lòng nhập API key hợp lệ (tối đa 512 ký tự, không chứa khoảng trắng).");
        }
        return value;
    }

    public void save(ApiKeyDto dto, HttpSession session) {
        String provider = normalizeProvider(dto.getProvider());
        String key = normalizeKey(dto.getApiKey());
        session.setAttribute(SettingsConstants.SESSION_PROVIDER, provider);
        session.setAttribute(SettingsConstants.SESSION_PROVIDER_API_KEY, key);
        // Preserve the shared Gemini contract without sending other providers' keys to Google.
        if ("GEMINI".equals(provider)) {
            session.setAttribute(SettingsConstants.SESSION_API_KEY, key);
        } else {
            session.removeAttribute(SettingsConstants.SESSION_API_KEY);
        }
    }

    public String getProvider(HttpSession session) {
        Object value = session.getAttribute(SettingsConstants.SESSION_PROVIDER);
        return value instanceof String provider ? normalizeProvider(provider) : "GEMINI";
    }

    public String getApiKey(HttpSession session) {
        Object value = session.getAttribute(SettingsConstants.SESSION_PROVIDER_API_KEY);
        if (value == null && "GEMINI".equals(getProvider(session))) {
            value = session.getAttribute(SettingsConstants.SESSION_API_KEY);
        }
        return value instanceof String key ? key : null;
    }

    public void clear(HttpSession session) {
        session.removeAttribute(SettingsConstants.SESSION_API_KEY);
        session.removeAttribute(SettingsConstants.SESSION_PROVIDER_API_KEY);
        session.removeAttribute(SettingsConstants.SESSION_PROVIDER);
    }

    /** Tests the submitted key, or the saved key for the same provider. Never saves a ping. */
    public PingResponseDto testConnection(ApiKeyDto dto, HttpSession session) {
        long start = System.nanoTime();
        try {
            String provider = normalizeProvider(dto.getProvider());
            String key = dto.getApiKey();
            if ((key == null || key.isBlank()) && session != null && provider.equals(getProvider(session))) {
                key = getApiKey(session);
            }
            String validatedKey = normalizeKey(key);
            JsonNode body;
            if ("GEMINI".equals(provider)) {
                body = client.post()
                        .uri("https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent", geminiModel)
                        .header("x-goog-api-key", validatedKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Map.of("contents", List.of(Map.of("parts", List.of(Map.of("text", "Reply OK.")))),
                                "generationConfig", Map.of("maxOutputTokens", 16)))
                        .retrieve().body(JsonNode.class);
            } else {
                String url = "GROQ".equals(provider)
                        ? "https://api.groq.com/openai/v1/chat/completions"
                        : "https://api.openai.com/v1/chat/completions";
                String model = "GROQ".equals(provider) ? "llama-3.3-70b-versatile" : "gpt-4.1-mini";
                body = client.post().uri(url).headers(headers -> headers.setBearerAuth(validatedKey))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Map.of("model", model, "messages", List.of(Map.of("role", "user", "content", "Reply OK.")),
                                "max_tokens", 16))
                        .retrieve().body(JsonNode.class);
            }
            boolean valid = body != null && ("GEMINI".equals(provider)
                    ? !body.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText().isBlank()
                    : !body.path("choices").path(0).path("message").path("content").asText().isBlank());
            return result(valid, start, valid ? "Kết nối thành công với " + provider + "."
                    : "Nhà cung cấp chưa trả về nội dung hợp lệ. Vui lòng thử lại.");
        } catch (IllegalArgumentException ex) {
            return result(false, start, ex.getMessage());
        } catch (RestClientResponseException ex) {
            String message = switch (ex.getStatusCode().value()) {
                case 400 -> "Yêu cầu không hợp lệ. Kiểm tra API key và cấu hình model.";
                case 401, 403 -> "API key không hợp lệ, đã hết hạn hoặc chưa có quyền truy cập model.";
                case 404 -> "Model không khả dụng. Vui lòng kiểm tra cấu hình model của nhà cung cấp.";
                case 429 -> "Đã hết hạn mức hoặc gửi quá nhiều yêu cầu. Kiểm tra quota và thử lại sau.";
                default -> "Nhà cung cấp đang gặp lỗi. Vui lòng thử lại sau.";
            };
            return result(false, start, message);
        } catch (RestClientException ex) {
            // Never expose upstream bodies, URLs, headers or exception messages containing credentials.
            return result(false, start, "Không thể kết nối hoặc đã hết thời gian chờ. Kiểm tra mạng và thử lại.");
        }
    }

    private PingResponseDto result(boolean success, long start, String message) {
        return new PingResponseDto(success, (System.nanoTime() - start) / 1_000_000, message);
    }
}
