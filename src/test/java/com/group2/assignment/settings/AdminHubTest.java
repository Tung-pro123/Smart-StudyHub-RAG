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
            "/admin/users/1/upgrade-pro", "/admin/users/1/downgrade-free",
            "/admin/users/1/upgrade-squad", "/admin/users/1/reset-rag"})
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
    @ValueSource(strings = {"PRO", "CAMPUS", "SQUAD"})
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
        assertThat(new com.group2.assignment.settings.dto.AdminStatsDto(0, 0, 0, 0, 0, 0, 0, null).getPaidConversionRate()).isZero();
    }

    @Test
    void squadUpgradeAndQuotaResetKeepRoleAndData() throws Exception {
        student.setDailyRagCount(50);
        student.setLastRagResetDate(today);
        String token = csrf();
        mvc.perform(post("/admin/users/{id}/upgrade-squad", student.getId()).session(session).param("csrfToken", token))
                .andExpect(redirectedUrl("/admin/users"));
        assertThat(student.getSubscriptionTier()).isEqualTo("SQUAD");
        assertThat(student.getDailyRagCount()).isEqualTo(50);
        assertThat(quota.canQueryRag(student.getId())).isTrue();
        mvc.perform(post("/admin/users/{id}/downgrade-free", student.getId()).session(session).param("csrfToken", token))
                .andExpect(redirectedUrl("/admin/users"));
        assertThat(quota.canQueryRag(student.getId())).isFalse();
        mvc.perform(post("/admin/users/{id}/reset-rag", student.getId()).session(session).param("csrfToken", token))
                .andExpect(redirectedUrl("/admin/users"));
        assertThat(student.getDailyRagCount()).isZero();
        assertThat(student.getLastRagResetDate()).isEqualTo(today);
        assertThat(student.getRole()).isEqualTo("ROLE_STUDENT");
    }

    @ParameterizedTest
    @ValueSource(strings = {"upgrade-squad", "reset-rag"})
    void newActionsRejectMissingUsersAndAdminTargets(String action) throws Exception {
        String token = csrf();
        mvc.perform(post("/admin/users/99999999/" + action).session(session).param("csrfToken", token))
                .andExpect(status().isNotFound());
        mvc.perform(post("/admin/users/" + admin.getId() + "/" + action).session(session).param("csrfToken", token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchesAllStudentIdentityFieldsAndCombinesTierFilter() throws Exception {
        student.setSubscriptionTier("SQUAD");
        for (String keyword : java.util.List.of("ADMIN-HUB-TEST", "Hub Student", "test@example.com")) {
            mvc.perform(get("/admin/users").session(session).param("keyword", keyword).param("tier", "SQUAD"))
                    .andExpect(status().isOk()).andExpect(model().attribute("students", hasSize(1)))
                    .andExpect(content().string(containsString("Reset RAG")));
        }
        mvc.perform(get("/admin/users").session(session).param("keyword", student.getUsername()).param("tier", "PRO"))
                .andExpect(status().isOk()).andExpect(model().attribute("students", empty()));
        mvc.perform(get("/admin/users").session(session).param("keyword", admin.getUsername()))
                .andExpect(status().isOk()).andExpect(model().attribute("students", empty()));
        mvc.perform(get("/admin/users").session(session).param("tier", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void statisticsCountOnlyStudentsAndIncludeSquadInPaidRate() {
        var before = analytics.getStats();
        student.setSubscriptionTier("SQUAD");
        users.flush();
        var after = analytics.getStats();
        assertThat(after.squadUsersCount()).isEqualTo(before.squadUsersCount() + 1);
        assertThat(after.freeUsersCount()).isEqualTo(before.freeUsersCount() - 1);
        long proCount = after.proUsersCount();
        admin.setSubscriptionTier("PRO");
        users.flush();
        assertThat(analytics.getStats().proUsersCount()).isEqualTo(proCount);
        assertThat(new com.group2.assignment.settings.dto.AdminStatsDto(5, 1, 1, 2, 0, 0, 0, null)
                .getPaidConversionRate()).isEqualTo(75);
    }

    @Test
    void recentActivityIsBoundedAndKeyIsMaskedEvenForShortSecrets() {
        for (int i = 0; i < 6; i++) {
            users.saveAndFlush(User.builder().username("recent-" + i).email("recent-" + i + "@example.com")
                    .password("hash").fullName("Recent").createdAt(java.time.LocalDateTime.now().plusDays(i + 1)).build());
        }
        assertThat(analytics.getRecentUsers()).hasSize(5);
        assertThat(analytics.getRecentUsers().get(0).getUsername()).isEqualTo("recent-5");
        assertThat(analytics.getRecentCourses()).hasSizeLessThanOrEqualTo(5);
        systemKey.update("AIzaSy-test-secret");
        assertThat(systemKey.getMaskedKey()).isEqualTo("AIzaSy...****");
        systemKey.update("short");
        assertThat(systemKey.getMaskedKey()).isEqualTo("********");
    }

    @Test
    void legacySettingsHasNoPersonalKeyFormOrWriteAccess() throws Exception {
        mvc.perform(get("/settings").session(session)).andExpect(redirectedUrl("/admin/settings"));
        mvc.perform(get("/settings").session(sessionFor(student))).andExpect(redirectedUrl("/courses"));
        mvc.perform(post("/settings/save").session(sessionFor(student)).param("apiKey", "secret"))
                .andExpect(status().isGone());
    }
}
