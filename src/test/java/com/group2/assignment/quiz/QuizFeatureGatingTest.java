package com.group2.assignment.quiz;

import com.group2.assignment.auth.controller.AuthController;
import com.group2.assignment.auth.entity.User;
import com.group2.assignment.auth.repository.UserRepository;
import com.group2.assignment.core.entity.Course;
import com.group2.assignment.core.repository.CourseRepository;
import com.group2.assignment.quiz.service.QuizService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class QuizFeatureGatingTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private QuizService quizService;

    private Course testCourse;

    @BeforeEach
    void setUp() {
        testCourse = courseRepository.findByCodeIgnoreCase("HSF302")
                .orElseGet(() -> courseRepository.save(Course.builder()
                        .code("HSF302")
                        .name("Spring Framework")
                        .build()));
    }

    private MockHttpSession sessionFor(String username, String tier) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseGet(() -> userRepository.save(User.builder()
                        .username(username)
                        .email(username + "@fpt.edu.vn")
                        .password("hashed_pwd")
                        .fullName("Test Student")
                        .role("ROLE_STUDENT")
                        .subscriptionTier(tier)
                        .build()));
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthController.SESSION_USER, user);
        return session;
    }

    @Test
    @DisplayName("Gửi giá trị số lượng câu hỏi sai định dạng chuỗi (abc) -> Bắt lỗi nhẹ nhàng và báo lỗi Flash")
    void testInvalidNumberFormat() throws Exception {
        MockHttpSession session = sessionFor("student_invalid_fmt", "FREE");
        MockMultipartFile file = new MockMultipartFile("pdfFile", "test.pdf", "application/pdf", "dummy content".getBytes());

        mvc.perform(multipart("/quiz/generate")
                        .file(file)
                        .param("courseId", testCourse.getId().toString())
                        .param("totalQuestions", "abc")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/quiz?courseId=" + testCourse.getId()))
                .andExpect(flash().attribute("errorMessage", "Số lượng câu hỏi không hợp lệ!"));

        verify(quizService, never()).createQuizFromPdf(any(), any(), any(), any(), any(), anyInt(), anyInt(), any(), any());
    }

    @Test
    @DisplayName("Gửi số lượng câu hỏi <= 0 hoặc > 20 -> Báo lỗi miền giá trị")
    void testOutOfBoundsQuestionCount() throws Exception {
        MockHttpSession session = sessionFor("student_bounds", "FREE");
        MockMultipartFile file = new MockMultipartFile("pdfFile", "test.pdf", "application/pdf", "dummy content".getBytes());

        mvc.perform(multipart("/quiz/generate")
                        .file(file)
                        .param("courseId", testCourse.getId().toString())
                        .param("totalQuestions", "-5")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/quiz?courseId=" + testCourse.getId()))
                .andExpect(flash().attribute("errorMessage", "Số lượng câu hỏi phải từ 1 đến 20 câu!"));
    }

    @Test
    @DisplayName("Gói Free yêu cầu 15 câu hỏi -> Chặn lại và yêu cầu nâng cấp PRO")
    void testFreeUserExceedsLimit() throws Exception {
        MockHttpSession session = sessionFor("student_free_over", "FREE");
        MockMultipartFile file = new MockMultipartFile("pdfFile", "test.pdf", "application/pdf", "dummy content".getBytes());

        mvc.perform(multipart("/quiz/generate")
                        .file(file)
                        .param("courseId", testCourse.getId().toString())
                        .param("totalQuestions", "15")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/quiz?courseId=" + testCourse.getId()))
                .andExpect(flash().attribute("errorMessage", "Gói Free chỉ hỗ trợ tối đa 10 câu hỏi/đề thi. Vui lòng nâng cấp gói PRO để sinh đề 15 - 20 câu!"));

        verify(quizService, never()).createQuizFromPdf(any(), any(), any(), any(), any(), anyInt(), anyInt(), any(), any());
    }

    @Test
    @DisplayName("Gói Free yêu cầu 10 câu hỏi -> Được phép tạo đề")
    void testFreeUserWithinLimit() throws Exception {
        MockHttpSession session = sessionFor("student_free_ok", "FREE");
        MockMultipartFile file = new MockMultipartFile("pdfFile", "test.pdf", "application/pdf", "dummy content".getBytes());

        mvc.perform(multipart("/quiz/generate")
                        .file(file)
                        .param("courseId", testCourse.getId().toString())
                        .param("totalQuestions", "10")
                        .session(session))
                .andExpect(status().is3xxRedirection());
    }
}
