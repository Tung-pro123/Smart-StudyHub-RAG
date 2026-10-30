package com.group2.assignment.settings;

import com.group2.assignment.auth.controller.AuthController;
import com.group2.assignment.auth.entity.User;
import com.group2.assignment.auth.repository.UserRepository;
import com.group2.assignment.auth.service.JwtService;
import com.group2.assignment.settings.dto.PingResponseDto;
import com.group2.assignment.settings.service.*;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.ZoneId;
import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminHubTest {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired JwtService jwt;
    @Autowired SubscriptionQuotaService quota;
    @Autowired AdminAnalyticsService analytics;
    @Autowired SystemAiKeyService systemKey;
    @MockBean ApiKeyService pingService;
    private User admin;
    private User student;
    private MockHttpSession session;
    private final LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));

    @BeforeEach
    void setup() {
        admin = users.findByUsernameIgnoreCase("truong").orElseThrow();
        student = users.saveAndFlush(User.builder().username("admin-hub-test").email("admin-hub-test@example.com")
                .password("test-hash-never-render").fullName("Admin Hub Student").build());
        session = sessionFor(admin);
        systemKey.update("test-system-key");
    }

    private MockHttpSession sessionFor(User user) {
        MockHttpSession result = new MockHttpSession();
        result.setAttribute(AuthController.SESSION_USER, user);
        return result;
    }

    private String csrf() throws Exception {
        mvc.perform(get("/admin/settings").session(session)).andExpect(status().isOk());
        return (String) session.getAttribute(SettingsConstants.SESSION_CSRF_TOKEN);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/admin/dashboard", "/admin/users", "/admin/settings"})
    void adminPagesRenderButDenyStudentsAndGuests(String path) throws Exception {
        mvc.perform(get(path).session(session)).andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(content().string(not(containsString("test-system-key"))))
                .andExpect(content().string(not(containsString(student.getPassword()))));
        mvc.perform(get(path).session(sessionFor(student))).andExpect(status().isForbidden());
        mvc.perform(get(path)).andExpect(redirectedUrl("/login?error=unauthorized"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/admin/settings/save", "/admin/settings/test-ping",
            "/admin/users/1/upgrade-pro", "/admin/users/1/downgrade-free"})
    void mutationsRequireAdminAndCsrf(String path) throws Exception {
        String token = csrf();
        mvc.perform(post(path).session(session).param("apiKey", "test-key"))
                .andExpect(status().isForbidden());
        mvc.perform(post(path).session(session).param("csrfToken", "wrong").param("apiKey", "test-key"))
                .andExpect(status().isForbidden());
        mvc.perform(post(path).session(sessionFor(student)).param("csrfToken", token))
                .andExpect(status().isForbidden());
        mvc.perform(post(path)).andExpect(redirectedUrl("/login?error=unauthorized"));
        verifyNoInteractions(pingService);
    }

    @Test
    void upgradesAndDowngradesPersistAndRefreshAnExistingStudentSession() throws Exception {
        MockHttpSession studentSession = sessionFor(User.builder().id(student.getId()).username(student.getUsername())
                .role("ROLE_STUDENT").subscriptionTier("FREE").build());
        String token = csrf();
        mvc.perform(post("/admin/users/{id}/upgrade-pro", student.getId()).session(session).param("csrfToken", token))
                .andExpect(redirectedUrl("/admin/users"));
        assertThat(users.findById(student.getId()).orElseThrow().getSubscriptionTier()).isEqualTo("PRO");
        mvc.perform(get("/settings").session(studentSession)).andExpect(redirectedUrl("/courses"));
        assertThat(((User) studentSession.getAttribute(AuthController.SESSION_USER)).getSubscriptionTier()).isEqualTo("PRO");
        mvc.perform(post("/admin/users/{id}/downgrade-free", student.getId()).session(session).param("csrfToken", token))
                .andExpect(redirectedUrl("/admin/users"));
        assertThat(student.getSubscriptionTier()).isEqualTo("FREE");
        assertThat(student.getRole()).isEqualTo("ROLE_STUDENT");
    }

    @Test
    void rejectsMissingTargetAndCannotChangeAdminTier() throws Exception {
        String token = csrf();
        mvc.perform(post("/admin/users/99999999/upgrade-pro").session(session).param("csrfToken", token))
                .andExpect(status().isNotFound());
        mvc.perform(post("/admin/users/{id}/downgrade-free", admin.getId()).session(session).param("csrfToken", token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void roleIsCheckedFromDatabaseAndJwtCookieRestoresAdminSession() throws Exception {
        String cookie = jwt.generateToken(admin.getUsername(), "ROLE_ADMIN", admin.getFullName());
        mvc.perform(get("/admin/dashboard").cookie(new Cookie(AuthController.AUTH_COOKIE_NAME, cookie)))
                .andExpect(status().isOk());
        User forged = User.builder().id(student.getId()).username(student.getUsername()).role("ROLE_ADMIN").build();
        mvc.perform(get("/admin/users").session(sessionFor(forged))).andExpect(status().isForbidden());
    }

    @Test
    void systemKeyUpdatesWithoutEchoingOrSavingToSessionAndPingUsesOnlySystemKey() throws Exception {
        String token = csrf();
        mvc.perform(post("/admin/settings/save").session(session).param("csrfToken", token).param("apiKey", "new-system-secret"))
                .andExpect(redirectedUrl("/admin/settings"));
        assertThat(systemKey.getApiKey()).isEqualTo("new-system-secret");
        assertThat(session.getAttribute(SettingsConstants.SESSION_API_KEY)).isNull();
        mvc.perform(get("/admin/settings").session(session)).andExpect(content().string(not(containsString("new-system-secret"))));
        when(pingService.testConnection(any(), isNull())).thenReturn(new PingResponseDto(true, 75, "Kết nối thành công"));
        mvc.perform(post("/admin/settings/test-ping").session(session).param("csrfToken", token).param("apiKey", "ignored-secret"))
                .andExpect(redirectedUrl("/admin/settings"));
        verify(pingService).testConnection(argThat(dto -> "new-system-secret".equals(dto.getApiKey())), isNull());
        assertThat(analytics.getStats().geminiPingLatencyMs()).isEqualTo(75);
        mvc.perform(get("/admin/settings").session(session)).andExpect(content().string(containsString("Kết nối siêu tốc")));
    }

    @Test
    void invalidSystemKeyKeepsWorkingKeyAndDoesNotFlashSecret() throws Exception {
        mvc.perform(post("/admin/settings/save").session(session).param("csrfToken", csrf()).param("apiKey", "bad secret"))
                .andExpect(flash().attributeExists("errorMessage")).andExpect(flash().attributeCount(1));
        assertThat(systemKey.getApiKey()).isEqualTo("test-system-key");
        assertThatThrownBy(() -> systemKey.update("YOUR_GEMINI_API_KEY")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void pingFailureIsShownAndClearsSuccessfulLatency() throws Exception {
        when(pingService.testConnection(any(), isNull())).thenReturn(new PingResponseDto(false, 120, "API key không hợp lệ"));
        mvc.perform(post("/admin/settings/test-ping").session(session).param("csrfToken", csrf()))
                .andExpect(flash().attributeExists("errorMessage"));
        mvc.perform(get("/admin/settings").session(session)).andExpect(status().isOk())
                .andExpect(content().string(containsString("alert-error")));
        assertThat(analytics.getStats().geminiPingLatencyMs()).isNull();
        assertThat(new SystemAiKeyService("YOUR_GEMINI_API_KEY", pingService).isConfigured()).isFalse();
        assertThat(new SystemAiKeyService("", pingService).getApiKey()).isEmpty();
    }

    @Test
    void freeQuotaResetsOnlyOnNewDayAndStopsAtFifty() {
        student.setDailyRagCount(50);
        student.setLastRagResetDate(today.minusDays(1));
        assertThat(quota.canQueryRag(student.getId())).isTrue();
        assertThat(student.getDailyRagCount()).isZero();
        assertThat(student.getLastRagResetDate()).isEqualTo(today);
        student.setDailyRagCount(49);
        assertThat(quota.recordSuccessfulRagQuery(student.getId())).isTrue();
        assertThat(quota.recordSuccessfulRagQuery(student.getId())).isFalse();
        assertThat(quota.canQueryRag(student.getId())).isFalse();
        assertThat(student.getDailyRagCount()).isEqualTo(50);
        student.setLastRagResetDate(null);
        student.setDailyRagCount(null);
        assertThat(quota.recordSuccessfulRagQuery(student.getId())).isTrue();
        assertThat(student.getDailyRagCount()).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"PRO", "CAMPUS"})
    void paidQuotaIsUnlimitedAndDowngradeDoesNotResetUsage(String tier) {
        student.setSubscriptionTier(tier);
        student.setLastRagResetDate(today);
        student.setDailyRagCount(80);
        assertThat(quota.recordSuccessfulRagQuery(student.getId())).isTrue();
        quota.changeTier(student.getId(), "FREE");
        assertThat(quota.canQueryRag(student.getId())).isFalse();
        assertThat(student.getDailyRagCount()).isEqualTo(81);
    }

    @Test
    void statisticsExcludeYesterdayAndTreatLegacyNullTierAsFree() {
        var before = analytics.getStats();
        student.setLastRagResetDate(today);
        student.setDailyRagCount(7);
        student.setSubscriptionTier("PRO");
        users.flush();
        var after = analytics.getStats();
        assertThat(after.totalUsers()).isEqualTo(users.count());
        assertThat(after.todayRagQueries()).isEqualTo(before.todayRagQueries() + 7);
        assertThat(after.proUsersCount()).isEqualTo(before.proUsersCount() + 1);
        student.setLastRagResetDate(today.minusDays(1));
        student.setSubscriptionTier(null);
        users.flush();
        assertThat(analytics.getStats().todayRagQueries()).isEqualTo(before.todayRagQueries());
        assertThat(analytics.getStats().freeUsersCount()).isEqualTo(before.freeUsersCount());
        assertThat(new com.group2.assignment.settings.dto.AdminStatsDto(0, 0, 0, 0, 0, 0, null).getProConversionRate()).isZero();
    }

    @Test
    void legacySettingsHasNoPersonalKeyFormOrWriteAccess() throws Exception {
        mvc.perform(get("/settings").session(session)).andExpect(redirectedUrl("/admin/settings"));
        mvc.perform(get("/settings").session(sessionFor(student))).andExpect(redirectedUrl("/courses"));
        mvc.perform(post("/settings/save").session(sessionFor(student)).param("apiKey", "secret"))
                .andExpect(status().isGone());
    }
}
