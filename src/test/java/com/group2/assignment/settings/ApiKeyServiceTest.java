package com.group2.assignment.settings;

import com.group2.assignment.settings.dto.ApiKeyDto;
import com.group2.assignment.settings.dto.PingResponseDto;
import com.group2.assignment.settings.service.ApiKeyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class ApiKeyServiceTest {
    private ApiKeyService service;
    private MockRestServiceServer server;
    private MockHttpSession session;
    private static final String KEY = "test-key-never-a-real-secret";

    @BeforeEach
    void setup() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        service = new ApiKeyService(builder.build(), SettingsConstants.DEFAULT_MODEL);
        session = new MockHttpSession();
    }

    private ApiKeyDto dto(String provider, String key) {
        ApiKeyDto dto = new ApiKeyDto();
        dto.setProvider(provider);
        dto.setApiKey(key);
        return dto;
    }

    @Test
    void savesNormalizedKeyOnlyInCurrentSessionAndClearsLegacyKeyWhenSwitchingProvider() {
        service.save(dto("gemini", "  " + KEY + "  "), session);
        assertThat(session.getAttribute(SettingsConstants.SESSION_API_KEY)).isEqualTo(KEY);
        assertThat(service.getApiKey(new MockHttpSession())).isNull();
        service.save(dto("groq", "groq-test-key"), session);
        assertThat(service.getProvider(session)).isEqualTo("GROQ");
        assertThat(session.getAttribute(SettingsConstants.SESSION_API_KEY)).isNull();
        service.clear(session);
        assertThat(service.getApiKey(session)).isNull();
        assertThat(session.getAttribute(SettingsConstants.SESSION_PROVIDER)).isNull();
    }

    @ParameterizedTest
    @CsvSource({"GEMINI,''", "GEMINI,'bad key'", "GEMINI,'bad\r\nheader'", "UNKNOWN,test-key"})
    void invalidSavePreservesExistingConfiguration(String provider, String key) {
        service.save(dto("GEMINI", KEY), session);
        assertThatThrownBy(() -> service.save(dto(provider, key), session)).isInstanceOf(IllegalArgumentException.class);
        assertThat(service.getApiKey(session)).isEqualTo(KEY);
        assertThat(service.getProvider(session)).isEqualTo("GEMINI");
    }

    @Test
    void geminiPingUsesHeaderAndSavedKeyWithoutPuttingKeyInUrl() {
        service.save(dto("GEMINI", KEY), session);
        server.expect(requestTo("https://generativelanguage.googleapis.com/v1beta/models/" + SettingsConstants.DEFAULT_MODEL + ":generateContent"))
                .andExpect(method(HttpMethod.POST)).andExpect(header("x-goog-api-key", KEY))
                .andExpect(content().json("{\"contents\":[{\"parts\":[{\"text\":\"Reply OK.\"}]}]}"))
                .andRespond(withSuccess("{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"OK\"}]}}]}", MediaType.APPLICATION_JSON));
        PingResponseDto result = service.testConnection(dto("GEMINI", ""), session);
        assertThat(result.success()).isTrue();
        assertThat(result.latencyMs()).isGreaterThanOrEqualTo(0);
        assertThat(result.message()).doesNotContain(KEY);
        server.verify();
    }

    @ParameterizedTest
    @CsvSource({"GROQ,https://api.groq.com/openai/v1/chat/completions,llama-3.3-70b-versatile",
            "OPENAI,https://api.openai.com/v1/chat/completions,gpt-4.1-mini"})
    void otherProvidersUseTheirOwnEndpointWithoutSavingPing(String provider, String url, String model) {
        server.expect(requestTo(url)).andExpect(header("Authorization", "Bearer " + KEY))
                .andExpect(jsonPath("$.model").value(model))
                .andRespond(withSuccess("{\"choices\":[{\"message\":{\"content\":\"OK\"}}]}", MediaType.APPLICATION_JSON));
        assertThat(service.testConnection(dto(provider, KEY), session).success()).isTrue();
        assertThat(service.getApiKey(session)).isNull();
        server.verify();
    }

    @ParameterizedTest
    @CsvSource({"400,Yêu cầu không hợp lệ", "401,API key không hợp lệ", "403,API key không hợp lệ",
            "404,Model không khả dụng", "429,Đã hết hạn mức", "500,Nhà cung cấp đang gặp lỗi"})
    void upstreamErrorsAreFriendlyAndDoNotExposeSecrets(int status, String message) {
        server.expect(anything()).andRespond(withStatus(HttpStatus.valueOf(status))
                .body("upstream leaked " + KEY));
        PingResponseDto result = service.testConnection(dto("GEMINI", KEY), session);
        assertThat(result.success()).isFalse();
        assertThat(result.message()).contains(message).doesNotContain(KEY);
        server.verify();
    }

    @Test
    void networkFailureDoesNotExposeExceptionDetails() {
        server.expect(anything()).andRespond(withException(new IOException("secret=" + KEY)));
        PingResponseDto result = service.testConnection(dto("GEMINI", KEY), session);
        assertThat(result.success()).isFalse();
        assertThat(result.message()).contains("Kiểm tra mạng").doesNotContain(KEY);
        server.verify();
    }

    @Test
    void emptySuccessResponseIsNotAValidPing() {
        server.expect(anything()).andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
        assertThat(service.testConnection(dto("GEMINI", KEY), session).success()).isFalse();
        server.verify();
    }

    @Test
    void blankKeyCannotReuseKeyFromDifferentProvider() {
        service.save(dto("GEMINI", KEY), session);
        assertThat(service.testConnection(dto("GROQ", ""), session).success()).isFalse();
        server.verify();
    }
}
