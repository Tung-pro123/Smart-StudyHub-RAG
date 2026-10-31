package com.group2.assignment.quiz;

import com.group2.assignment.auth.entity.User;
import com.group2.assignment.auth.repository.UserRepository;
import com.group2.assignment.core.entity.Course;
import com.group2.assignment.core.repository.CourseRepository;
import com.group2.assignment.quiz.entity.Quiz;
import com.group2.assignment.quiz.entity.QuizAttempt;
import com.group2.assignment.quiz.repository.QuizAttemptRepository;
import com.group2.assignment.quiz.repository.QuizRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class QuizAttemptUserMappingTest {

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Test
    @DisplayName("User: Kiểm tra xác định nhóm trưởng hiệu dụng getEffectiveSquadLeader")
    void testEffectiveSquadLeader() {
        User leader = User.builder().username("leader1").subscriptionTier("SQUAD").build();
        assertEquals("leader1", leader.getEffectiveSquadLeader());

        User member = User.builder().username("member1").subscriptionTier("SQUAD").squadLeaderUsername("leader1").build();
        assertEquals("leader1", member.getEffectiveSquadLeader());

        User freeUser = User.builder().username("free1").subscriptionTier("FREE").build();
        assertNull(freeUser.getEffectiveSquadLeader());
    }

    @Test
    @DisplayName("User: Kiểm tra logic isSquad")
    void testIsSquad() {
        User squadUser = User.builder().username("leader").subscriptionTier("SQUAD").build();
        assertTrue(squadUser.isSquad());

        User memberWithLeader = User.builder().username("member").squadLeaderUsername("leader").build();
        assertTrue(memberWithLeader.isSquad());

        User freeUser = User.builder().username("free").subscriptionTier("FREE").build();
        assertFalse(freeUser.isSquad());
    }

    @Test
    @DisplayName("QuizAttempt: Gán User và Quiz hợp lệ (in-memory)")
    void testQuizAttemptUserAssociation() {
        User user = User.builder().id(10L).username("testuser").build();
        QuizAttempt attempt = QuizAttempt.builder()
                .id(1L)
                .user(user)
                .studentName("Test User")
                .totalScore(9.0)
                .correctCount(9)
                .totalCount(10)
                .build();

        assertNotNull(attempt.getUser());
        assertEquals(10L, attempt.getUser().getId());
    }

    @Test
    @DisplayName("QuizAttempt: Backward compatibility - Tạo QuizAttempt với user = null thành công trong DB")
    void testNullableUserPersistenceBackwardCompatibility() {
        Course course = Course.builder()
                .code("CS101-MAP")
                .name("Computer Science Mapping")
                .build();
        course = courseRepository.save(course);

        Quiz quiz = Quiz.builder()
                .title("Test Quiz Null User")
                .course(course)
                .totalQuestions(5)
                .build();
        quiz = quizRepository.save(quiz);

        QuizAttempt attempt = QuizAttempt.builder()
                .quiz(quiz)
                .user(null) // Backward compatibility: anonymous or legacy attempt
                .studentName("Khách Ẩn Danh")
                .totalScore(8.0)
                .correctCount(4)
                .totalCount(5)
                .build();

        QuizAttempt saved = quizAttemptRepository.saveAndFlush(attempt);
        assertNotNull(saved.getId(), "QuizAttempt id phải được sinh tự động");
        assertNull(saved.getUser(), "user phải là null khi không gán user");

        QuizAttempt found = quizAttemptRepository.findById(saved.getId()).orElse(null);
        assertNotNull(found);
        assertNull(found.getUser());
        assertEquals("Khách Ẩn Danh", found.getStudentName());
    }

    @Test
    @DisplayName("QuizAttempt: Lưu QuizAttempt liên kết với User tồn tại thành công trong DB")
    void testQuizAttemptWithUserPersistence() {
        Course course = Course.builder()
                .code("CS102-MAP")
                .name("Computer Science Mapping 2")
                .build();
        course = courseRepository.save(course);

        Quiz quiz = Quiz.builder()
                .title("Test Quiz With User")
                .course(course)
                .totalQuestions(10)
                .build();
        quiz = quizRepository.save(quiz);

        User user = User.builder()
                .username("student_quiz_test")
                .email("student_quiz_test@example.com")
                .password("hashed_pwd")
                .fullName("Student Quiz Test")
                .subscriptionTier("SQUAD")
                .build();
        user = userRepository.save(user);

        QuizAttempt attempt = QuizAttempt.builder()
                .quiz(quiz)
                .user(user)
                .studentName(user.getFullName())
                .totalScore(9.5)
                .correctCount(9)
                .totalCount(10)
                .build();

        QuizAttempt saved = quizAttemptRepository.saveAndFlush(attempt);
        assertNotNull(saved.getId());
        assertNotNull(saved.getUser());
        assertEquals(user.getId(), saved.getUser().getId());
        assertEquals("student_quiz_test", saved.getUser().getUsername());
    }
}
