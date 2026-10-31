package com.group2.assignment.quiz;

import com.group2.assignment.auth.controller.AuthController;
import com.group2.assignment.auth.entity.User;
import com.group2.assignment.auth.repository.UserRepository;
import com.group2.assignment.core.entity.Course;
import com.group2.assignment.core.repository.CourseRepository;
import com.group2.assignment.quiz.entity.Quiz;
import com.group2.assignment.quiz.entity.QuizAttempt;
import com.group2.assignment.quiz.entity.QuizQuestion;
import com.group2.assignment.quiz.repository.QuizAttemptRepository;
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

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class QuizDoDVerificationTest {

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

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    private User freeUser;
    private User proUser;
    private User squadLeader;
    private User squadMember;
    private Course testCourse;
    private Quiz testQuiz;

    @BeforeEach
    void setUp() {
        long timestamp = System.currentTimeMillis();

        testCourse = courseRepository.save(Course.builder()
                .code("DOD_" + timestamp)
                .name("DoD Verification Course")
                .description("Course for DoD integration tests")
                .build());

        freeUser = userRepository.save(User.builder()
                .username("dod_free_" + timestamp)
                .email("dod_free_" + timestamp + "@studyhub.vn")
                .fullName("DoD Free Student")
                .password("hash")
                .subscriptionTier("FREE")
                .role("ROLE_STUDENT")
                .build());

        proUser = userRepository.save(User.builder()
                .username("dod_pro_" + timestamp)
                .email("dod_pro_" + timestamp + "@studyhub.vn")
                .fullName("DoD Pro Student")
                .password("hash")
                .subscriptionTier("PRO")
                .role("ROLE_STUDENT")
                .build());

        squadLeader = userRepository.save(User.builder()
                .username("dod_leader_" + timestamp)
                .email("dod_leader_" + timestamp + "@studyhub.vn")
                .fullName("DoD Squad Leader")
                .password("hash")
                .subscriptionTier("SQUAD")
                .role("ROLE_STUDENT")
                .build());

        squadMember = userRepository.save(User.builder()
                .username("dod_member_" + timestamp)
                .email("dod_member_" + timestamp + "@studyhub.vn")
                .fullName("DoD Squad Member")
                .password("hash")
                .subscriptionTier("SQUAD")
                .squadLeaderUsername(squadLeader.getUsername())
                .role("ROLE_STUDENT")
                .build());

        testQuiz = quizRepository.save(Quiz.builder()
                .title("Đề thi Đánh Giá Năng Lực 2026")
                .course(testCourse)
                .difficulty("MEDIUM")
                .totalQuestions(1)
                .timeLimitMinutes(15)
                .build());

        quizQuestionRepository.save(QuizQuestion.builder()
                .quiz(testQuiz)
                .questionText("Hệ thống StudyHub sử dụng công nghệ gì cho RAG?")
                .optionA("Spring AI")
                .optionB("LangChain")
                .optionC("LlamaIndex")
                .optionD("Semantic Kernel")
                .correctAnswer("A")
                .explanation("StudyHub tích hợp Spring AI với PGvector.")
                .sortOrder(1)
                .build());
    }

    private MockHttpSession sessionFor(User user) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthController.SESSION_USER, user);
        return session;
    }

    @Test
    @DisplayName("DoD 1: Chưa đăng nhập gọi /api/quiz/{id}/export-pdf trả về 401 Unauthorized")
    void testUnauthenticatedExportPdfReturns401() throws Exception {
        mockMvc.perform(get("/api/quiz/" + testQuiz.getId() + "/export-pdf"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DoD 2: User FREE gọi /api/quiz/{id}/export-pdf bị chặn 403 Forbidden")
    void testFreeUserExportPdfReturns403() throws Exception {
        mockMvc.perform(get("/api/quiz/" + testQuiz.getId() + "/export-pdf")
                        .session(sessionFor(freeUser)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DoD 3: User PRO xuất PDF đề thi sạch (showAnswers=false) nhận file PDF hợp lệ với tên file khử dấu Đ/đ")
    void testProUserExportPdfClean() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/api/quiz/" + testQuiz.getId() + "/export-pdf")
                        .param("showAnswers", "false")
                        .session(sessionFor(proUser)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("application/pdf")))
                .andExpect(header().string("Content-Disposition", containsString("filename=\"De_thi_Danh_Gia_Nang_Luc_2026.pdf\"")))
                .andReturn();

        byte[] pdfBytes = mvcResult.getResponse().getContentAsByteArray();
        assertTrue(pdfBytes.length > 500, "PDF phải có dung lượng lớn hơn 500 bytes");
        String magicHeader = new String(pdfBytes, 0, Math.min(5, pdfBytes.length), StandardCharsets.ISO_8859_1);
        assertEquals("%PDF-", magicHeader, "File phải có magic header chuẩn %PDF-");
    }

    @Test
    @DisplayName("DoD 4: User PRO xuất PDF kèm đáp án (showAnswers=true) có hậu tố _kem_dap_an.pdf")
    void testProUserExportPdfWithAnswers() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/api/quiz/" + testQuiz.getId() + "/export-pdf")
                        .param("showAnswers", "true")
                        .session(sessionFor(proUser)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("application/pdf")))
                .andExpect(header().string("Content-Disposition", containsString("filename=\"De_thi_Danh_Gia_Nang_Luc_2026_kem_dap_an.pdf\"")))
                .andReturn();

        byte[] pdfBytes = mvcResult.getResponse().getContentAsByteArray();
        assertTrue(pdfBytes.length > 500);
        String magicHeader = new String(pdfBytes, 0, Math.min(5, pdfBytes.length), StandardCharsets.ISO_8859_1);
        assertEquals("%PDF-", magicHeader);
    }

    @Test
    @DisplayName("DoD 5: Bảng xếp hạng Squad: Hiển thị bảng xếp hạng khi thành viên Squad xem trang quiz")
    void testSquadLeaderboardOnQuizIndex() throws Exception {
        quizAttemptRepository.save(QuizAttempt.builder()
                .quiz(testQuiz)
                .user(squadLeader)
                .studentName(squadLeader.getFullName())
                .totalScore(9.0)
                .correctCount(1)
                .totalCount(1)
                .build());

        mockMvc.perform(get("/quiz")
                        .param("courseId", testCourse.getId().toString())
                        .session(sessionFor(squadMember)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("squadLeaderboard"))
                .andExpect(model().attribute("isSquad", true));
    }

    @Test
    @DisplayName("DoD 6: Tương thích ngược: QuizAttempt lưu thành công với user = null")
    void testQuizAttemptNullableUserCompatibility() {
        QuizAttempt anonymousAttempt = QuizAttempt.builder()
                .quiz(testQuiz)
                .user(null)
                .studentName("Khách Vãng Lai")
                .totalScore(7.5)
                .correctCount(1)
                .totalCount(1)
                .build();

        QuizAttempt saved = quizAttemptRepository.save(anonymousAttempt);
        assertNotNull(saved.getId());
        assertNull(saved.getUser());
    }

    @Test
    @DisplayName("DoD 7: Sinh viên FREE cố chọn 20 câu bị chặn lại và thông báo lỗi")
    void testFreeUserSelecting20QuestionsBlocked() throws Exception {
        mockMvc.perform(post("/quiz/generate")
                        .session(sessionFor(freeUser))
                        .param("courseId", testCourse.getId().toString())
                        .param("totalQuestions", "20"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/quiz?courseId=" + testCourse.getId()))
                .andExpect(flash().attribute("errorMessage", containsString("Gói Free chỉ hỗ trợ tối đa 10 câu hỏi")));
    }

    @Test
    @DisplayName("DoD 8: Sinh viên PRO chọn 20 câu không bị chặn bởi quota gói cước")
    void testProUserSelecting20QuestionsNotBlockedByQuota() throws Exception {
        // PRO user sending 20 questions won't be rejected by tier validation (though document validation will require document)
        mockMvc.perform(post("/quiz/generate")
                        .session(sessionFor(proUser))
                        .param("courseId", testCourse.getId().toString())
                        .param("totalQuestions", "20"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/quiz?courseId=" + testCourse.getId()))
                .andExpect(flash().attribute("errorMessage", not(containsString("Gói Free chỉ hỗ trợ tối đa 10 câu hỏi"))));
    }
}

