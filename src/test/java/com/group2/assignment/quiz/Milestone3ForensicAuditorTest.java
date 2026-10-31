package com.group2.assignment.quiz;

import com.group2.assignment.auth.controller.AuthController;
import com.group2.assignment.auth.entity.User;
import com.group2.assignment.auth.repository.UserRepository;
import com.group2.assignment.core.entity.Course;
import com.group2.assignment.core.repository.CourseRepository;
import com.group2.assignment.quiz.entity.Quiz;
import com.group2.assignment.quiz.entity.QuizQuestion;
import com.group2.assignment.quiz.repository.QuizQuestionRepository;
import com.group2.assignment.quiz.repository.QuizRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class Milestone3ForensicAuditorTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuizQuestionRepository quizQuestionRepository;

    private User freeUser;
    private User proUser;
    private User campusUser;
    private User adminUser;
    private User squadUser;
    private Course testCourse;
    private Quiz standardQuiz;

    @BeforeEach
    void setupTestData() {
        long ts = System.currentTimeMillis();

        freeUser = userRepository.save(User.builder()
                .username("auditor_free_" + ts)
                .email("auditor_free_" + ts + "@mail.com")
                .fullName("Auditor Free")
                .password("hash")
                .subscriptionTier("FREE")
                .role("ROLE_STUDENT")
                .build());

        proUser = userRepository.save(User.builder()
                .username("auditor_pro_" + ts)
                .email("auditor_pro_" + ts + "@mail.com")
                .fullName("Auditor Pro")
                .password("hash")
                .subscriptionTier("PRO")
                .role("ROLE_STUDENT")
                .build());

        campusUser = userRepository.save(User.builder()
                .username("auditor_campus_" + ts)
                .email("auditor_campus_" + ts + "@mail.com")
                .fullName("Auditor Campus")
                .password("hash")
                .subscriptionTier("CAMPUS")
                .role("ROLE_STUDENT")
                .build());

        adminUser = userRepository.save(User.builder()
                .username("auditor_admin_" + ts)
                .email("auditor_admin_" + ts + "@mail.com")
                .fullName("Auditor Admin")
                .password("hash")
                .subscriptionTier("FREE") // Free tier but Admin role
                .role("ROLE_ADMIN")
                .build());

        squadUser = userRepository.save(User.builder()
                .username("auditor_squad_" + ts)
                .email("auditor_squad_" + ts + "@mail.com")
                .fullName("Auditor Squad")
                .password("hash")
                .subscriptionTier("SQUAD")
                .role("ROLE_STUDENT")
                .build());

        testCourse = courseRepository.save(Course.builder()
                .name("Forensic Audit Course")
                .code("AUD301_" + ts)
                .description("Course for empirical testing")
                .build());

        standardQuiz = quizRepository.save(Quiz.builder()
                .title("Đề thi Đột phá & Ứng dụng AI: Cực đỉnh! 🚀")
                .course(testCourse)
                .difficulty("HARD")
                .totalQuestions(1)
                .timeLimitMinutes(20)
                .build());

        quizQuestionRepository.save(QuizQuestion.builder()
                .quiz(standardQuiz)
                .sortOrder(1)
                .questionText("Hệ thống có bảo vệ tính toàn vẹn (Integrity) không?")
                .optionA("Có, kiểm thử độc lập nghiêm ngặt")
                .optionB("Không, dùng facade")
                .optionC("Chỉ dùng mock cứng")
                .optionD("Không có câu trả lời")
                .correctAnswer("A")
                .explanation("Forensic Auditor kiểm chứng từng luồng xử lý thực tế.")
                .build());
    }

    private MockHttpSession session(User user) {
        MockHttpSession s = new MockHttpSession();
        if (user != null) {
            s.setAttribute(AuthController.SESSION_USER, user);
        }
        return s;
    }

    @Test
    @DisplayName("Forensic 1: Null session -> 401 Unauthorized")
    void testNullSessionReturns401() throws Exception {
        mockMvc.perform(get("/api/quiz/" + standardQuiz.getId() + "/export-pdf"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Forensic 2: Session with null user -> 401 Unauthorized")
    void testEmptySessionReturns401() throws Exception {
        mockMvc.perform(get("/api/quiz/" + standardQuiz.getId() + "/export-pdf")
                        .session(session(null)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Forensic 3: FREE tier user -> 403 Forbidden")
    void testFreeTierReturns403() throws Exception {
        mockMvc.perform(get("/api/quiz/" + standardQuiz.getId() + "/export-pdf")
                        .session(session(freeUser)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Forensic 4: PRO tier user -> 200 OK with authentic PDF bytes")
    void testProTierReturns200() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/quiz/" + standardQuiz.getId() + "/export-pdf")
                        .session(session(proUser)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("application/pdf")))
                .andReturn();

        byte[] bytes = result.getResponse().getContentAsByteArray();
        assertTrue(bytes.length > 500, "PDF must have non-trivial size");
        String magic = new String(bytes, 0, Math.min(5, bytes.length), StandardCharsets.ISO_8859_1);
        assertEquals("%PDF-", magic);
    }

    @Test
    @DisplayName("Forensic 5: CAMPUS tier user -> 200 OK")
    void testCampusTierReturns200() throws Exception {
        mockMvc.perform(get("/api/quiz/" + standardQuiz.getId() + "/export-pdf")
                        .session(session(campusUser)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("application/pdf")));
    }

    @Test
    @DisplayName("Forensic 6: ROLE_ADMIN with FREE subscription -> 200 OK (Admin privilege)")
    void testAdminRoleReturns200() throws Exception {
        mockMvc.perform(get("/api/quiz/" + standardQuiz.getId() + "/export-pdf")
                        .session(session(adminUser)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("application/pdf")));
    }

    @Test
    @DisplayName("Forensic 7: SQUAD tier user -> 200 OK")
    void testSquadTierReturns200() throws Exception {
        mockMvc.perform(get("/api/quiz/" + standardQuiz.getId() + "/export-pdf")
                        .session(session(squadUser)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("application/pdf")));
    }

    @Test
    @DisplayName("Forensic 8: Complex Vietnamese title normalization (Đ/đ, accents, emojis)")
    void testComplexVietnameseFilenameNormalization() throws Exception {
        // "Đề thi Đột phá & Ứng dụng AI: Cực đỉnh! 🚀"
        // Expected: "De_thi_Dot_pha_Ung_dung_AI_Cuc_dinh.pdf"
        mockMvc.perform(get("/api/quiz/" + standardQuiz.getId() + "/export-pdf")
                        .param("showAnswers", "false")
                        .session(session(proUser)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("filename=\"De_thi_Dot_pha_Ung_dung_AI_Cuc_dinh.pdf\"")));
    }

    @Test
    @DisplayName("Forensic 9: ShowAnswers=true appends _kem_dap_an suffix")
    void testShowAnswersSuffix() throws Exception {
        mockMvc.perform(get("/api/quiz/" + standardQuiz.getId() + "/export-pdf")
                        .param("showAnswers", "true")
                        .session(session(proUser)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("filename=\"De_thi_Dot_pha_Ung_dung_AI_Cuc_dinh_kem_dap_an.pdf\"")));
    }

    @Test
    @DisplayName("Forensic 10: Fallback to de-thi.pdf if title is pure symbols or blank")
    void testBlankTitleFallback() throws Exception {
        Quiz blankTitleQuiz = quizRepository.save(Quiz.builder()
                .title("   ??? !!! ###   ")
                .course(testCourse)
                .difficulty("EASY")
                .totalQuestions(1)
                .timeLimitMinutes(10)
                .build());

        mockMvc.perform(get("/api/quiz/" + blankTitleQuiz.getId() + "/export-pdf")
                        .session(session(proUser)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("filename=\"de-thi.pdf\"")));
    }

    @Test
    @DisplayName("Forensic 11: Non-existent quiz ID -> 404 Not Found")
    void testNonExistentQuizReturns404() throws Exception {
        mockMvc.perform(get("/api/quiz/88888888/export-pdf")
                        .session(session(proUser)))
                .andExpect(status().isNotFound());
    }
}
