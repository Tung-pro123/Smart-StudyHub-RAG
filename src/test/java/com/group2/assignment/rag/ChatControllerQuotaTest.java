package com.group2.assignment.rag;

import com.group2.assignment.auth.controller.AuthController;
import com.group2.assignment.auth.entity.User;
import com.group2.assignment.auth.repository.UserRepository;
import com.group2.assignment.core.entity.Course;
import com.group2.assignment.core.repository.CourseRepository;
import com.group2.assignment.rag.controller.ChatController;
import com.group2.assignment.rag.dto.ChatRequestDto;
import com.group2.assignment.rag.dto.ChatResponseDto;
import com.group2.assignment.rag.entity.ChatSession;
import com.group2.assignment.rag.repository.ChatSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ChatControllerQuotaTest {

    @Autowired
    private ChatController chatController;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    private Course sampleCourse;

    @BeforeEach
    void setUp() {
        sampleCourse = courseRepository.findByCodeIgnoreCase("HSF302")
                .orElseGet(() -> courseRepository.save(Course.builder()
                        .code("HSF302")
                        .name("JPA and Spring Framework")
                        .build()));
    }

    @Test
    @DisplayName("Sinh viên gói FREE đạt 50 câu/ngày sẽ bị chặn và không gọi LLM")
    void testFreeUserBlockedWhenReachingLimit() {
        User freeUser = userRepository.save(User.builder()
                .username("test_free_limit")
                .email("test_free_limit@fpt.edu.vn")
                .password("123456")
                .fullName("Sinh Viên Đạt Quota")
                .role("ROLE_STUDENT")
                .subscriptionTier("FREE")
                .dailyRagCount(50)
                .lastRagResetDate(LocalDate.now())
                .build());

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthController.SESSION_USER, freeUser);

        ChatRequestDto req = new ChatRequestDto();
        req.setQuestion("Spring Boot có những annotation nào?");

        ResponseEntity<ChatResponseDto> res = chatController.sendChatMessageJson(sampleCourse.getId(), req, session);

        assertNotNull(res.getBody());
        ChatResponseDto body = res.getBody();
        assertFalse(body.isSuccess());
        assertTrue(body.getAnswer().contains("hết 50 câu hỏi RAG miễn phí"));
        assertEquals(0, body.getRemainingQuota());
        assertEquals(50, body.getDailyRagCount());
    }

    @Test
    @DisplayName("Tự động Reset hạn ngạch về 0 khi bước sang ngày mới")
    void testAutoResetQuotaOnNewDay() {
        User yesterdayUser = userRepository.save(User.builder()
                .username("test_yesterday_user")
                .email("test_yesterday_user@fpt.edu.vn")
                .password("123456")
                .fullName("Sinh Viên Ngày Cũ")
                .role("ROLE_STUDENT")
                .subscriptionTier("FREE")
                .dailyRagCount(50)
                .lastRagResetDate(LocalDate.now().minusDays(1)) // Ngày hôm qua
                .build());

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthController.SESSION_USER, yesterdayUser);

        ChatRequestDto req = new ChatRequestDto();
        req.setQuestion("Spring Data JPA là gì?");

        ResponseEntity<ChatResponseDto> res = chatController.sendChatMessageJson(sampleCourse.getId(), req, session);

        assertNotNull(res.getBody());
        User updatedUser = userRepository.findById(yesterdayUser.getId()).orElseThrow();
        assertEquals(LocalDate.now(), updatedUser.getLastRagResetDate());
        assertNotEquals(50, updatedUser.getDailyRagCount(), "Bộ đếm phải được reset");
    }

    @Test
    @DisplayName("Người dùng PRO hoặc ADMIN không bị giới hạn 50 câu")
    void testProUserNotBlockedAt50() {
        User proUser = userRepository.save(User.builder()
                .username("test_pro_user")
                .email("test_pro_user@fpt.edu.vn")
                .password("123456")
                .fullName("Sinh Viên Gói Pro")
                .role("ROLE_STUDENT")
                .subscriptionTier("PRO")
                .dailyRagCount(80)
                .lastRagResetDate(LocalDate.now())
                .build());

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthController.SESSION_USER, proUser);

        ChatRequestDto req = new ChatRequestDto();
        req.setQuestion("Giải thích cơ chế Transactional?");

        ResponseEntity<ChatResponseDto> res = chatController.sendChatMessageJson(sampleCourse.getId(), req, session);

        assertNotNull(res.getBody());
        ChatResponseDto body = res.getBody();
        assertEquals(-1, body.getRemainingQuota());
    }

    @Test
    @DisplayName("Bấm nút + Cuộc Trò Chuyện Mới tạo phiên mới và redirect đúng sessionId")
    void testStartNewChatCreatesSessionAndRedirects() {
        String redirectUrl = chatController.startNewChat(sampleCourse.getId());
        assertNotNull(redirectUrl);
        assertTrue(redirectUrl.startsWith("redirect:/courses/" + sampleCourse.getId() + "/chat?sessionId="));

        String sessionIdStr = redirectUrl.substring(redirectUrl.lastIndexOf("=") + 1);
        Long newSessionId = Long.parseLong(sessionIdStr);

        ChatSession sessionInDb = chatSessionRepository.findById(newSessionId).orElse(null);
        assertNotNull(sessionInDb);
        assertEquals("Cuộc trò chuyện mới", sessionInDb.getTitle());
        assertEquals(sampleCourse.getId(), sessionInDb.getCourse().getId());
    }

    @Test
    @DisplayName("Câu hỏi đầu tiên gửi vào phiên mới tự động cập nhật tiêu đề theo câu hỏi")
    void testFirstQuestionAutoUpdatesSessionTitle() {
        // Tạo phiên mới
        ChatSession newSession = chatSessionRepository.save(ChatSession.builder()
                .course(sampleCourse)
                .title("Cuộc trò chuyện mới")
                .build());

        User testUser = userRepository.save(User.builder()
                .username("test_auto_title_user")
                .email("test_auto_title@fpt.edu.vn")
                .password("123456")
                .fullName("Sinh Viên Test Title")
                .role("ROLE_STUDENT")
                .subscriptionTier("FREE")
                .dailyRagCount(0)
                .lastRagResetDate(LocalDate.now())
                .build());

        MockHttpSession session = new MockHttpSession();
        session.setAttribute(AuthController.SESSION_USER, testUser);

        String question = "Khái niệm Entity lifecycle trong Spring Data JPA là gì?";
        ChatRequestDto req = new ChatRequestDto();
        req.setSessionId(newSession.getId());
        req.setQuestion(question);

        ResponseEntity<ChatResponseDto> res = chatController.sendChatMessageJson(sampleCourse.getId(), req, session);
        assertNotNull(res.getBody());

        // Kiểm tra tiêu đề phiên chat trong CSDL đã tự động được đổi
        ChatSession updatedSession = chatSessionRepository.findById(newSession.getId()).orElseThrow();
        assertNotEquals("Cuộc trò chuyện mới", updatedSession.getTitle());
        assertTrue(updatedSession.getTitle().contains("Entity lifecycle") || updatedSession.getTitle().contains("Spring Data JPA"));
    }
}
