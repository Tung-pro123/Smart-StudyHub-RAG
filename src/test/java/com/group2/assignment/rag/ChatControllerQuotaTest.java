package com.group2.assignment.rag;

import com.group2.assignment.auth.controller.AuthController;
import com.group2.assignment.auth.entity.User;
import com.group2.assignment.auth.repository.UserRepository;
import com.group2.assignment.core.entity.Course;
import com.group2.assignment.core.repository.CourseRepository;
import com.group2.assignment.rag.controller.ChatController;
import com.group2.assignment.rag.dto.ChatRequestDto;
import com.group2.assignment.rag.dto.ChatResponseDto;
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
        // User phải được reset lastRagResetDate thành hôm nay và dailyRagCount được reset
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
        // Không bị chặn bởi quota (remainingQuota = -1)
        assertEquals(-1, body.getRemainingQuota());
    }
}

