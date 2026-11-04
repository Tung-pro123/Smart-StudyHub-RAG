package com.group2.assignment.quiz;

import com.group2.assignment.auth.entity.User;
import com.group2.assignment.auth.repository.UserRepository;
import com.group2.assignment.core.entity.Course;
import com.group2.assignment.core.repository.CourseRepository;
import com.group2.assignment.core.service.CourseService;
import com.group2.assignment.quiz.entity.Quiz;
import com.group2.assignment.quiz.entity.QuizAttempt;
import com.group2.assignment.quiz.repository.QuizAttemptRepository;
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
    private QuizAttemptRepository quizAttemptRepository;

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

    @Test
    @DisplayName("Solo Quiz: Đề thi cá nhân chỉ hiển thị với chính người tạo, ẩn với thành viên khác")
    void testSoloQuizVisibleOnlyToCreator() {
        Quiz soloQuiz = quizRepository.save(Quiz.builder()
                .course(squadCourse)
                .title("Đề Ôn Tập Solo Của Member 1")
                .chapterTopic("Chương 1")
                .difficulty("EASY")
                .totalQuestions(5)
                .timeLimitMinutes(15)
                .createdBy(squadMember)
                .squadShared(false) // Solo Quiz
                .build());

        // squadMember xem danh sách đề: phải thấy đề Solo của mình
        List<Quiz> memberQuizzes = quizService.getQuizzesByCourse(squadCourse.getId(), squadMember);
        boolean memberSeesSolo = memberQuizzes.stream().anyMatch(q -> q.getId().equals(soloQuiz.getId()));
        assertTrue(memberSeesSolo, "Tác giả tạo đề Solo phải nhìn thấy đề của mình");

        // squadLeader xem danh sách đề: KHÔNG được thấy đề Solo của member
        List<Quiz> leaderQuizzes = quizService.getQuizzesByCourse(squadCourse.getId(), squadLeader);
        boolean leaderSeesSolo = leaderQuizzes.stream().anyMatch(q -> q.getId().equals(soloQuiz.getId()));
        assertFalse(leaderSeesSolo, "Thành viên khác (kể cả Leader) KHÔNG được thấy đề Solo của member");
    }

    @Test
    @DisplayName("Solo Quiz: Kiểm tra quyền truy cập trực tiếp isAccessibleBy")
    void testSoloQuizAccessControl() {
        Quiz soloQuiz = quizRepository.save(Quiz.builder()
                .course(squadCourse)
                .title("Đề Solo")
                .difficulty("MEDIUM")
                .createdBy(squadMember)
                .squadShared(false)
                .build());

        assertTrue(soloQuiz.isAccessibleBy(squadMember), "Tác giả có quyền truy cập đề Solo");
        assertFalse(soloQuiz.isAccessibleBy(squadLeader), "Thành viên khác không có quyền truy cập đề Solo");
        assertFalse(soloQuiz.isAccessibleBy(freeStudent), "Người ngoài nhóm không có quyền truy cập đề Solo");
    }

    @Test
    @DisplayName("Solo Quiz: Tác giả có quyền tự xóa đề thi Solo của mình trong môn nhóm")
    void testSoloQuizCanBeDeletedByCreator() {
        Quiz soloQuiz = quizRepository.save(Quiz.builder()
                .course(squadCourse)
                .title("Đề Solo Cần Xóa")
                .difficulty("MEDIUM")
                .createdBy(squadMember)
                .squadShared(false)
                .build());

        // Người khác cố xóa -> ném SecurityException
        User otherUser = userRepository.findByUsernameIgnoreCase("squad_member2")
                .orElseGet(() -> userRepository.save(User.builder()
                        .username("squad_member2")
                        .email("squad_member2@fpt.edu.vn")
                        .fullName("Squad Member 2")
                        .password("123456")
                        .role("ROLE_STUDENT")
                        .subscriptionTier("SQUAD")
                        .squadLeaderUsername("squad_leader")
                        .build()));

        assertThrows(SecurityException.class, () ->
                quizService.deleteQuiz(soloQuiz.getId(), otherUser),
                "Thành viên khác không được xóa đề Solo của người tạo");

        // Chính tác giả xóa -> thành công
        assertDoesNotThrow(() ->
                quizService.deleteQuiz(soloQuiz.getId(), squadMember),
                "Tác giả đề Solo phải tự xóa được đề của mình");

        assertFalse(quizRepository.existsById(soloQuiz.getId()), "Đề thi Solo phải bị xóa khỏi CSDL");
    }

    @Test
    @DisplayName("Squad Leaderboard: Lượt thi của Đề Solo KHÔNG được tính vào Bảng Xếp Hạng Nhóm")
    void testSoloQuizAttemptExcludedFromSquadLeaderboard() {
        // Lấy số lượt thi hiện tại của squadLeader
        List<Object[]> beforeLeaderboard = quizAttemptRepository.findSquadLeaderboardRaw(
                "squad_leader", "squad_leader", squadCourse.getId());
        long initialAttempts = beforeLeaderboard.stream()
                .filter(row -> squadLeader.getId().equals(row[0]))
                .map(row -> (Long) row[5])
                .findFirst().orElse(0L);

        // Tạo đề Solo cho squadLeader
        Quiz soloQuiz = quizRepository.save(Quiz.builder()
                .course(squadCourse)
                .title("Đề Solo Luyện Tập Riêng Của Leader")
                .difficulty("EASY")
                .createdBy(squadLeader)
                .squadShared(false) // Solo Quiz
                .build());

        // Lưu lượt thi điểm 9.99 trên đề Solo
        quizAttemptRepository.save(QuizAttempt.builder()
                .quiz(soloQuiz)
                .user(squadLeader)
                .studentName("Squad Leader")
                .totalScore(9.99)
                .correctCount(5)
                .totalCount(5)
                .timeSpentSeconds(120)
                .build());

        // Lấy lại Leaderboard
        List<Object[]> afterLeaderboard = quizAttemptRepository.findSquadLeaderboardRaw(
                "squad_leader", "squad_leader", squadCourse.getId());

        long afterAttempts = afterLeaderboard.stream()
                .filter(row -> squadLeader.getId().equals(row[0]))
                .map(row -> (Long) row[5])
                .findFirst().orElse(0L);

        boolean hasSoloScore = afterLeaderboard.stream()
                .filter(row -> squadLeader.getId().equals(row[0]))
                .anyMatch(row -> Double.valueOf(9.99).equals(row[4]));

        assertEquals(initialAttempts, afterAttempts, "Số lượt thi trên Leaderboard không được tăng khi làm đề Solo");
        assertFalse(hasSoloScore, "Điểm thi của Đề Solo tuyệt đối KHÔNG được xuất hiện trên Squad Leaderboard");
    }
}
