package com.group2.assignment.quiz;

import com.group2.assignment.auth.controller.AuthController;
import com.group2.assignment.auth.entity.User;
import com.group2.assignment.auth.repository.UserRepository;
import com.group2.assignment.core.entity.Course;
import com.group2.assignment.core.repository.CourseRepository;
import com.group2.assignment.quiz.entity.Quiz;
import com.group2.assignment.quiz.entity.QuizAttempt;
import com.group2.assignment.quiz.entity.QuizQuestion;
import com.group2.assignment.quiz.dto.QuizSubmissionDto;
import com.group2.assignment.quiz.repository.QuizAttemptRepository;
import com.group2.assignment.quiz.repository.QuizQuestionRepository;
import com.group2.assignment.quiz.repository.QuizRepository;
import com.group2.assignment.quiz.service.QuizService;
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
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class QuizSquadLeaderboardAndExportTest {

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

    @Autowired
    private QuizService quizService;

    private User freeUser;
    private User proUser;
    private User squadLeader;
    private User squadMember;
    private Course course;
    private Quiz testQuiz;

    @BeforeEach
    void setUp() {
        long timestamp = System.currentTimeMillis();

        freeUser = userRepository.save(User.builder()
                .username("free_student_" + timestamp)
                .email("free_" + timestamp + "@gmail.com")
                .fullName("Sinh viên Gói Miễn Phí")
                .password("hash123")
                .subscriptionTier("FREE")
                .role("ROLE_STUDENT")
                .build());

        proUser = userRepository.save(User.builder()
                .username("pro_student_" + timestamp)
                .email("pro_" + timestamp + "@gmail.com")
                .fullName("Sinh viên Gói Chuyên Nghiệp")
                .password("hash123")
                .subscriptionTier("PRO")
                .role("ROLE_STUDENT")
                .build());

        squadLeader = userRepository.save(User.builder()
                .username("squad_leader_" + timestamp)
                .email("leader_" + timestamp + "@gmail.com")
                .fullName("Trưởng Nhóm Học Tập Squad")
                .password("hash123")
                .subscriptionTier("SQUAD")
                .role("ROLE_STUDENT")
                .build());

        squadMember = userRepository.save(User.builder()
                .username("squad_member_" + timestamp)
                .email("member_" + timestamp + "@gmail.com")
                .fullName("Thành Viên Nhóm Squad")
                .password("hash123")
                .subscriptionTier("SQUAD")
                .squadLeaderUsername(squadLeader.getUsername())
                .role("ROLE_STUDENT")
                .build());

        course = courseRepository.save(Course.builder()
                .name("Kỹ Thuật Phần Mềm Nâng Cao")
                .code("SWE301_" + timestamp)
                .description("Môn học kiểm thử xuất PDF")
                .build());

        testQuiz = quizRepository.save(Quiz.builder()
                .title("Đề thi kiểm thử Squad & PDF")
                .course(course)
                .difficulty("MEDIUM")
                .totalQuestions(2)
                .timeLimitMinutes(15)
                .build());

        quizQuestionRepository.saveAll(List.of(
                QuizQuestion.builder()
                        .quiz(testQuiz)
                        .sortOrder(1)
                        .questionText("Mục tiêu chính của TDD trong phát triển phần mềm là gì?")
                        .optionA("Viết test sau khi hoàn tất code")
                        .optionB("Viết test trước để định hình thiết kế và hành vi")
                        .optionC("Không cần viết test tự động")
                        .optionD("Chỉ test giao diện người dùng")
                        .correctAnswer("B")
                        .explanation("TDD (Test-Driven Development) yêu cầu viết test trước để dẫn dắt thiết kế.")
                        .build(),
                QuizQuestion.builder()
                        .quiz(testQuiz)
                        .sortOrder(2)
                        .questionText("Gói cước nào được phép sử dụng tính năng xuất PDF đề thi?")
                        .optionA("Chỉ gói FREE")
                        .optionB("Gói PRO và Gói SQUAD")
                        .optionC("Không có gói nào")
                        .optionD("Tất cả người dùng chưa đăng nhập")
                        .correctAnswer("B")
                        .explanation("Tính năng xuất PDF là tính năng cao cấp dành riêng cho PRO và SQUAD.")
                        .build()
        ));
    }

    private MockHttpSession sessionFor(User user) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthController.SESSION_USER, user);
        return session;
    }

    @Test
    @DisplayName("API Export PDF: Chưa đăng nhập trả về 401 Unauthorized")
    void testExportPdfUnauthenticatedReturns401() throws Exception {
        mockMvc.perform(get("/api/quiz/" + testQuiz.getId() + "/export-pdf"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("API Export PDF: Gói Free bị chặn 403 Forbidden")
    void testExportPdfFreeTierReturns403Forbidden() throws Exception {
        mockMvc.perform(get("/api/quiz/" + testQuiz.getId() + "/export-pdf")
                        .session(sessionFor(freeUser)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("API Export PDF: Gói Pro tải file PDF thành công bắt đầu bằng %PDF-")
    void testExportPdfProTierSuccess() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/quiz/" + testQuiz.getId() + "/export-pdf")
                        .session(sessionFor(proUser)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("application/pdf")))
                .andExpect(header().string("Content-Disposition", containsString("attachment; filename=")))
                .andReturn();

        byte[] content = result.getResponse().getContentAsByteArray();
        assertTrue(content.length > 500);
        String magicHeader = new String(content, 0, Math.min(5, content.length), StandardCharsets.ISO_8859_1);
        assertEquals("%PDF-", magicHeader);
    }

    @Test
    @DisplayName("API Export PDF: Gói Squad tải file PDF thành công")
    void testExportPdfSquadTierSuccess() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/quiz/" + testQuiz.getId() + "/export-pdf")
                        .session(sessionFor(squadMember)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("application/pdf")))
                .andExpect(header().string("Content-Disposition", containsString("attachment; filename=")))
                .andReturn();

        byte[] content = result.getResponse().getContentAsByteArray();
        assertTrue(content.length > 500);
        String magicHeader = new String(content, 0, Math.min(5, content.length), StandardCharsets.ISO_8859_1);
        assertEquals("%PDF-", magicHeader);
    }

    @Test
    @DisplayName("API Export PDF: Chuẩn hóa tên file tiếng Việt có dấu Đ/đ và ký tự đặc biệt")
    void testExportPdfFilenameNormalizedVietnamese() throws Exception {
        mockMvc.perform(get("/api/quiz/" + testQuiz.getId() + "/export-pdf")
                        .param("showAnswers", "false")
                        .session(sessionFor(proUser)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("filename=\"De_thi_kiem_thu_Squad_PDF.pdf\"")));
    }

    @Test
    @DisplayName("API Export PDF: Tên file có hậu tố _kem_dap_an khi showAnswers=true")
    void testExportPdfFilenameWithShowAnswersTrue() throws Exception {
        mockMvc.perform(get("/api/quiz/" + testQuiz.getId() + "/export-pdf")
                        .param("showAnswers", "true")
                        .session(sessionFor(proUser)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("filename=\"De_thi_kiem_thu_Squad_PDF_kem_dap_an.pdf\"")));
    }

    @Test
    @DisplayName("API Export PDF: Đề thi không tồn tại trả về 404 Not Found")
    void testExportPdfNotFoundReturns404() throws Exception {
        mockMvc.perform(get("/api/quiz/999999/export-pdf")
                        .session(sessionFor(proUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("API Export PDF: Route alias /quiz/{id}/export-pdf cũng hoạt động chính xác")
    void testExportPdfRouteAlias() throws Exception {
        mockMvc.perform(get("/quiz/" + testQuiz.getId() + "/export-pdf")
                        .session(sessionFor(proUser)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("application/pdf")));
    }

    @Test
    @DisplayName("Bảng xếp hạng Squad: Thành viên Squad truy cập /quiz hiển thị đúng bảng xếp hạng thi đua")
    void testSquadLeaderboardDisplayedForSquadMember() throws Exception {
        quizAttemptRepository.save(QuizAttempt.builder()
                .quiz(testQuiz)
                .user(squadLeader)
                .studentName(squadLeader.getFullName())
                .totalScore(8.0)
                .correctCount(4)
                .totalCount(5)
                .build());

        quizAttemptRepository.save(QuizAttempt.builder()
                .quiz(testQuiz)
                .user(squadMember)
                .studentName(squadMember.getFullName())
                .totalScore(10.0)
                .correctCount(5)
                .totalCount(5)
                .build());

        mockMvc.perform(get("/quiz")
                        .param("courseId", String.valueOf(course.getId()))
                        .session(sessionFor(squadMember)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("squadLeaderboard"))
                .andExpect(model().attribute("isSquad", true))
                .andExpect(model().attribute("squadLeaderboard", hasSize(2)));
    }

    @Test
    @DisplayName("Bảng xếp hạng Squad: Nhóm trưởng Squad truy cập /quiz cũng nhận đầy đủ leaderboard")
    void testSquadLeaderboardDisplayedForSquadLeader() throws Exception {
        quizAttemptRepository.save(QuizAttempt.builder()
                .quiz(testQuiz)
                .user(squadLeader)
                .studentName(squadLeader.getFullName())
                .totalScore(9.5)
                .correctCount(2)
                .totalCount(2)
                .build());

        mockMvc.perform(get("/quiz")
                        .param("courseId", String.valueOf(course.getId()))
                        .session(sessionFor(squadLeader)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("squadLeaderboard"))
                .andExpect(model().attribute("isSquad", true))
                .andExpect(model().attribute("squadLeaderboard", hasSize(1)));
    }

    @Test
    @DisplayName("Bảng xếp hạng Squad: Người dùng FREE không hiển thị bảng xếp hạng Squad")
    void testSquadLeaderboardNotDisplayedForFreeUser() throws Exception {
        mockMvc.perform(get("/quiz")
                        .param("courseId", String.valueOf(course.getId()))
                        .session(sessionFor(freeUser)))
                .andExpect(status().isOk())
                .andExpect(model().attribute("isSquad", false))
                .andExpect(model().attribute("squadLeaderboard", hasSize(0)));
    }

    @Test
    @DisplayName("Bảng xếp hạng Squad: Thứ tự xếp hạng sắp xếp MAX(totalScore) DESC, COUNT(attempt) DESC")
    void testSquadLeaderboardOrderingAndTieBreaking() {
        long ts = System.currentTimeMillis();
        User squadMember2 = userRepository.save(User.builder()
                .username("squad_member2_" + ts)
                .email("m2_" + ts + "@gmail.com")
                .fullName("Thành Viên 2")
                .password("hash")
                .subscriptionTier("SQUAD")
                .squadLeaderUsername(squadLeader.getUsername())
                .role("ROLE_STUDENT")
                .build());

        User squadMember3 = userRepository.save(User.builder()
                .username("squad_member3_" + ts)
                .email("m3_" + ts + "@gmail.com")
                .fullName("Thành Viên 3")
                .password("hash")
                .subscriptionTier("SQUAD")
                .squadLeaderUsername(squadLeader.getUsername())
                .role("ROLE_STUDENT")
                .build());

        // Member 1 (squadMember): maxScore 8.0, 1 attempt
        quizAttemptRepository.save(QuizAttempt.builder()
                .quiz(testQuiz)
                .user(squadMember)
                .studentName(squadMember.getFullName())
                .totalScore(8.0)
                .correctCount(1)
                .totalCount(2)
                .build());

        // Member 2: maxScore 9.0, 1 attempt
        quizAttemptRepository.save(QuizAttempt.builder()
                .quiz(testQuiz)
                .user(squadMember2)
                .studentName(squadMember2.getFullName())
                .totalScore(9.0)
                .correctCount(2)
                .totalCount(2)
                .build());

        // Leader: maxScore 9.0, 2 attempts (tie-break should place Leader before Member 2)
        quizAttemptRepository.save(QuizAttempt.builder()
                .quiz(testQuiz)
                .user(squadLeader)
                .studentName(squadLeader.getFullName())
                .totalScore(7.0)
                .correctCount(1)
                .totalCount(2)
                .build());
        quizAttemptRepository.save(QuizAttempt.builder()
                .quiz(testQuiz)
                .user(squadLeader)
                .studentName(squadLeader.getFullName())
                .totalScore(9.0)
                .correctCount(2)
                .totalCount(2)
                .build());

        // Member 3: maxScore 10.0, 1 attempt (Rank 1)
        quizAttemptRepository.save(QuizAttempt.builder()
                .quiz(testQuiz)
                .user(squadMember3)
                .studentName(squadMember3.getFullName())
                .totalScore(10.0)
                .correctCount(2)
                .totalCount(2)
                .build());

        List<QuizService.SquadLeaderboardDto> leaderboard = quizService.getSquadLeaderboard(squadLeader.getUsername(), course.getId());
        assertEquals(4, leaderboard.size());

        // Rank 1: Member 3 (10.0)
        assertEquals(squadMember3.getUsername(), leaderboard.get(0).getUsername());
        assertEquals(10.0, leaderboard.get(0).getMaxScore());
        assertEquals(1L, leaderboard.get(0).getAttemptCount());
        assertNotNull(leaderboard.get(0).getAvatar());

        // Rank 2: Leader (9.0, 2 attempts)
        assertEquals(squadLeader.getUsername(), leaderboard.get(1).getUsername());
        assertEquals(9.0, leaderboard.get(1).getMaxScore());
        assertEquals(2L, leaderboard.get(1).getAttemptCount());

        // Rank 3: Member 2 (9.0, 1 attempt)
        assertEquals(squadMember2.getUsername(), leaderboard.get(2).getUsername());
        assertEquals(9.0, leaderboard.get(2).getMaxScore());
        assertEquals(1L, leaderboard.get(2).getAttemptCount());

        // Rank 4: Member 1 (8.0, 1 attempt)
        assertEquals(squadMember.getUsername(), leaderboard.get(3).getUsername());
        assertEquals(8.0, leaderboard.get(3).getMaxScore());
    }

    @Test
    @DisplayName("Nộp bài thi: Lưu liên kết currentUser vào QuizAttempt thành công")
    void testSubmitQuizLinksCurrentUserToQuizAttempt() throws Exception {
        mockMvc.perform(post("/quiz/" + testQuiz.getId() + "/submit")
                        .session(sessionFor(proUser))
                        .param("studentName", "Học viên Pro")
                        .param("timeSpentSeconds", "120"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/quiz/attempt/*"));

        List<QuizAttempt> attempts = quizAttemptRepository.findAllByQuizIdOrderByCompletedAtDesc(testQuiz.getId());
        assertFalse(attempts.isEmpty());
        QuizAttempt latest = attempts.get(0);
        assertNotNull(latest.getUser(), "QuizAttempt phải liên kết với currentUser trong session");
        assertEquals(proUser.getId(), latest.getUser().getId());
        assertEquals("Học viên Pro", latest.getStudentName());
    }

    @Test
    @DisplayName("Nộp bài thi không kèm user: QuizAttempt lưu với user = null tương thích ngược hoàn toàn")
    void testSubmitAndGradeWithoutUserCompatibility() {
        QuizSubmissionDto submission = QuizSubmissionDto.builder()
                .studentName("Khách Vãng Lai")
                .timeSpentSeconds(30)
                .answers(Map.of())
                .build();

        var result = quizService.submitAndGrade(testQuiz.getId(), submission);
        assertNotNull(result);
        assertNotNull(result.getAttemptId());

        QuizAttempt saved = quizAttemptRepository.findById(result.getAttemptId()).orElseThrow();
        assertNull(saved.getUser(), "Khi gọi submitAndGrade không truyền User thì attempt.user phải là null");
        assertEquals("Khách Vãng Lai", saved.getStudentName());
    }

    @Test
    @DisplayName("Tương thích ngược CSDL: QuizAttempt lưu thành công với user = null")
    void testQuizAttemptNullableUserEntityCompatibility() {
        QuizAttempt anonymousAttempt = QuizAttempt.builder()
                .quiz(testQuiz)
                .user(null)
                .studentName("Khách Vãng Lai CSDL")
                .totalScore(8.5)
                .correctCount(1)
                .totalCount(2)
                .build();

        QuizAttempt saved = quizAttemptRepository.save(anonymousAttempt);
        assertNotNull(saved.getId());
        assertNull(saved.getUser());
    }
}

