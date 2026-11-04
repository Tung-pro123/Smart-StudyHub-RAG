package com.group2.assignment.quiz;

import com.group2.assignment.auth.entity.User;
import com.group2.assignment.auth.repository.UserRepository;
import com.group2.assignment.core.entity.Course;
import com.group2.assignment.core.repository.CourseRepository;
import com.group2.assignment.core.service.CourseService;
import com.group2.assignment.quiz.entity.Quiz;
import com.group2.assignment.quiz.repository.QuizRepository;
import com.group2.assignment.quiz.service.QuizService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class SquadQuizHubAndSecurityTest {

    @Autowired
    private QuizService quizService;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserRepository userRepository;

    private User squadLeader;
    private User squadMember;
    private User freeStudent;
    private Course squadCourse;
    private Course personalCourse;
    private Quiz squadQuiz;

    @BeforeEach
    void setUp() {
        squadLeader = userRepository.findByUsernameIgnoreCase("squad_leader")
                .orElseGet(() -> userRepository.save(User.builder()
                        .username("squad_leader")
                        .email("squad_leader@fpt.edu.vn")
                        .fullName("Squad Leader")
                        .password("123456")
                        .role("ROLE_STUDENT")
                        .subscriptionTier("SQUAD")
                        .squadLeaderUsername("squad_leader")
                        .squadStatus("ACTIVE")
                        .build()));

        squadMember = userRepository.findByUsernameIgnoreCase("squad_member1")
                .orElseGet(() -> userRepository.save(User.builder()
                        .username("squad_member1")
                        .email("squad_member1@fpt.edu.vn")
                        .fullName("Squad Member 1")
                        .password("123456")
                        .role("ROLE_STUDENT")
                        .subscriptionTier("SQUAD")
                        .squadLeaderUsername("squad_leader")
                        .squadStatus("ACTIVE")
                        .build()));

        freeStudent = userRepository.findByUsernameIgnoreCase("student")
                .orElseGet(() -> userRepository.save(User.builder()
                        .username("student")
                        .email("student@fpt.edu.vn")
                        .fullName("Free Student")
                        .password("123456")
                        .role("ROLE_STUDENT")
                        .subscriptionTier("FREE")
                        .build()));

        // Môn học dùng chung của Squad
        squadCourse = courseRepository.findByCodeIgnoreCase("EXE101")
                .orElseGet(() -> courseRepository.save(Course.builder()
                        .code("EXE101")
                        .name("Experiential Entrepreneurship")
                        .user(squadLeader)
                        .squadShared(true)
                        .build()));
        squadCourse.setSquadShared(true);
        courseRepository.save(squadCourse);

        // Môn học cá nhân của Free Student
        personalCourse = courseRepository.findByCodeIgnoreCase("HSF302")
                .orElseGet(() -> courseRepository.save(Course.builder()
                        .code("HSF302")
                        .name("JPA and Spring")
                        .user(freeStudent)
                        .squadShared(false)
                        .build()));

        // Đề thi của môn nhóm
        squadQuiz = quizRepository.save(Quiz.builder()
                .course(squadCourse)
                .title("Đề Thi Môn Nhóm Khởi Nghiệp EXE101")
                .chapterTopic("Lean Canvas")
                .difficulty("MEDIUM")
                .totalQuestions(5)
                .timeLimitMinutes(15)
                .createdBy(squadLeader)
                .build());
    }

    @Test
    @DisplayName("Data Isolation: Sinh viên Free chỉ thấy môn cá nhân, không thấy môn của Squad")
    void testDataIsolationForFreeStudent() {
        List<Course> accessibleCourses = courseService.getAccessibleCourses(freeStudent);
        assertNotNull(accessibleCourses);

        boolean seesPersonal = accessibleCourses.stream().anyMatch(c -> "HSF302".equalsIgnoreCase(c.getCode()));
        boolean seesSquad = accessibleCourses.stream().anyMatch(c -> "EXE101".equalsIgnoreCase(c.getCode()));

        assertTrue(seesPersonal, "Sinh viên Free phải thấy môn HSF302 của mình");
        assertFalse(seesSquad, "Sinh viên Free KHÔNG được thấy môn EXE101 của Squad");
    }

    @Test
    @DisplayName("Data Isolation: Thành viên Squad thấy được môn nhóm dùng chung EXE101")
    void testDataIsolationForSquadMember() {
        List<Course> accessibleCourses = courseService.getAccessibleCourses(squadMember);
        assertNotNull(accessibleCourses);

        boolean seesSquad = accessibleCourses.stream().anyMatch(c -> "EXE101".equalsIgnoreCase(c.getCode()));
        assertTrue(seesSquad, "Thành viên Squad phải thấy môn nhóm dùng chung EXE101");
    }

    @Test
    @DisplayName("Tác giả tạo đề: Đề thi nhóm lưu vết createdBy chính xác")
    void testQuizCreatedByAttribution() {
        assertNotNull(squadQuiz.getCreatedBy(), "Đề thi nhóm phải ghi nhận tác giả tạo đề");
        assertEquals("squad_leader", squadQuiz.getCreatedBy().getUsername());
    }

    @Test
    @DisplayName("RBAC: Thành viên thường KHÔNG được xóa đề thi của Môn Nhóm")
    void testSquadMemberCannotDeleteSquadQuiz() {
        assertThrows(SecurityException.class, () ->
                quizService.deleteQuiz(squadQuiz.getId(), squadMember),
                "Thành viên thường cố xóa đề thi nhóm phải ném SecurityException");
    }

    @Test
    @DisplayName("RBAC: Trưởng nhóm được quyền xóa đề thi của Môn Nhóm")
    void testSquadLeaderCanDeleteSquadQuiz() {
        assertDoesNotThrow(() ->
                quizService.deleteQuiz(squadQuiz.getId(), squadLeader),
                "Trưởng nhóm phải xóa được đề thi môn nhóm thành công");

        assertFalse(quizRepository.existsById(squadQuiz.getId()), "Đề thi phải bị xóa khỏi CSDL");
    }

    @Test
    @DisplayName("RBAC: Người khác KHÔNG được xóa đề thi của Môn Cá Nhân")
    void testIntruderCannotDeletePersonalQuiz() {
        Quiz personalQuiz = quizRepository.save(Quiz.builder()
                .course(personalCourse)
                .title("Đề Cá Nhân HSF302")
                .chapterTopic("JPA")
                .difficulty("EASY")
                .totalQuestions(5)
                .timeLimitMinutes(15)
                .createdBy(freeStudent)
                .build());

        assertThrows(SecurityException.class, () ->
                quizService.deleteQuiz(personalQuiz.getId(), squadMember),
                "Kẻ xâm nhập cố xóa đề cá nhân người khác phải bị chặn SecurityException");

        assertDoesNotThrow(() ->
                quizService.deleteQuiz(personalQuiz.getId(), freeStudent),
                "Chủ sở hữu môn học phải xóa được đề thi cá nhân");
    }
}

