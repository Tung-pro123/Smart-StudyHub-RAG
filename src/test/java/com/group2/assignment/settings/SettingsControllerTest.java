package com.group2.assignment.settings;

import com.group2.assignment.auth.controller.AuthController;
import com.group2.assignment.auth.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SettingsControllerTest {
    @Autowired
    private MockMvc mvc;
    private MockHttpSession session;

    @BeforeEach
    void setup() {
        session = new MockHttpSession();
        session.setAttribute(AuthController.SESSION_USER, new User());
    }

    private String token() throws Exception {
        mvc.perform(get("/settings").session(session)).andExpect(status().isOk());
        return (String) session.getAttribute(SettingsConstants.SESSION_CSRF_TOKEN);
    }

    @Test
    void settingsRequiresLogin() throws Exception {
        mvc.perform(get("/settings")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=unauthorized"));
    }

    @Test
    void rendersSharedLayoutWithoutSavedSecretAndKeepsScriptInFragment() throws Exception {
        session.setAttribute(SettingsConstants.SESSION_API_KEY, "test-hidden-secret");
        mvc.perform(get("/settings").session(session)).andExpect(status().isOk())
                .andExpect(view().name("settings/index"))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(content().string(containsString("Đã lưu key")))
                .andExpect(content().string(containsString("button.addEventListener")))
                .andExpect(content().string(not(containsString("test-hidden-secret"))));
    }

    @Test
    void savesRedirectsAndClearsOnlySettings() throws Exception {
        String csrf = token();
        mvc.perform(post("/settings/save").session(session).param("csrfToken", csrf)
                        .param("provider", "GEMINI").param("apiKey", "test-secret"))
                .andExpect(redirectedUrl("/settings"))
                .andExpect(flash().attributeExists("successMessage"));
        assertThat(session.getAttribute(SettingsConstants.SESSION_API_KEY)).isEqualTo("test-secret");
        mvc.perform(post("/settings/clear").session(session).param("csrfToken", csrf))
                .andExpect(redirectedUrl("/settings"));
        assertThat(session.getAttribute(SettingsConstants.SESSION_API_KEY)).isNull();
        assertThat(session.getAttribute(SettingsConstants.SESSION_PROVIDER_API_KEY)).isNull();
        assertThat(session.getAttribute(AuthController.SESSION_USER)).isNotNull();
    }

    @Test
    void invalidSaveDoesNotFlashCredentialDto() throws Exception {
        mvc.perform(post("/settings/save").session(session).param("csrfToken", token())
                        .param("provider", "INVALID").param("apiKey", "test-secret"))
                .andExpect(redirectedUrl("/settings"))
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attributeCount(1));
        assertThat(session.getAttribute(SettingsConstants.SESSION_API_KEY)).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"/settings/save", "/settings/clear", "/settings/test-ping", "/settings/test-connection"})
    void rejectsMissingOrWrongCsrfToken(String path) throws Exception {
        token();
        mvc.perform(post(path).session(session).param("apiKey", "test-secret"))
                .andExpect(status().isForbidden());
        mvc.perform(post(path).session(session).param("csrfToken", "wrong-token"))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/settings/test-ping", "/settings/test-connection"})
    void pingValidationReturnsJsonWithoutSavingKey(String path) throws Exception {
        mvc.perform(post(path).session(session).param("csrfToken", token()).param("apiKey", ""))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.latencyMs").isNumber())
                .andExpect(jsonPath("$.message").isString());
        assertThat(session.getAttribute(SettingsConstants.SESSION_API_KEY)).isNull();
    }

    @Test
    void logoutInvalidatesApiKeySession() throws Exception {
        session.setAttribute(SettingsConstants.SESSION_API_KEY, "test-secret");
        mvc.perform(get("/logout").session(session)).andExpect(redirectedUrl("/login"));
        assertThat(session.isInvalid()).isTrue();
    }
}
