package com.group2.assignment.quiz;

import com.group2.assignment.auth.entity.User;
import com.group2.assignment.auth.repository.UserRepository;
import com.group2.assignment.core.entity.Course;
import com.group2.assignment.core.repository.CourseRepository;
import com.group2.assignment.quiz.entity.Quiz;
import com.group2.assignment.quiz.entity.QuizAttempt;
import com.group2.assignment.quiz.repository.QuizAttemptRepository;
import com.group2.assignment.quiz.repository.QuizRepository;
import com.group2.assignment.quiz.service.PdfTextExtractor;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import jakarta.persistence.EntityManager;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class M1AdversarialChallengeTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PdfTextExtractor pdfTextExtractor;

    @Nested
    @DisplayName("Adversarial Tests: User Squad Methods & Boundary Cases")
    class UserSquadAdversarialTests {

        @Test
        @DisplayName("Edge Case: Null subscriptionTier and null squadLeaderUsername - safe handling")
        void testNullSubscriptionTierAndSquadLeader() {
            User user = new User();
            user.setUsername("null_tier_user");
            user.setSubscriptionTier(null);
            user.setSquadLeaderUsername(null);

            assertFalse(user.isSquad(), "Null subscription tier with null leader must not be squad");
            assertNull(user.getEffectiveSquadLeader(), "Effective squad leader must be null");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Edge Case: Blank squadLeaderUsername with SQUAD tier returns self username")
        void testBlankSquadLeaderWithSquadTier(String blankLeader) {
            User user = User.builder()
                    .username("actual_leader")
                    .subscriptionTier("SQUAD")
                    .squadLeaderUsername(blankLeader)
                    .build();

            assertTrue(user.isSquad());
            assertEquals("actual_leader", user.getEffectiveSquadLeader());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Edge Case: Blank squadLeaderUsername with FREE tier returns null")
        void testBlankSquadLeaderWithFreeTier(String blankLeader) {
            User user = User.builder()
                    .username("free_student")
                    .subscriptionTier("FREE")
                    .squadLeaderUsername(blankLeader)
                    .build();

            assertFalse(user.isSquad());
            assertNull(user.getEffectiveSquadLeader());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Edge Case: Blank squadLeaderUsername with PRO tier returns null")
        void testBlankSquadLeaderWithProTier(String blankLeader) {
            User user = User.builder()
                    .username("pro_student")
                    .subscriptionTier("PRO")
                    .squadLeaderUsername(blankLeader)
                    .build();

            assertFalse(user.isSquad());
            assertNull(user.getEffectiveSquadLeader());
            assertTrue(user.isPro());
        }

        @Test
        @DisplayName("Edge Case: Squad member with untrimmed leader username")
        void testSquadLeaderUsernameTrimming() {
            User member = User.builder()
                    .username("member_bob")
                    .subscriptionTier("SQUAD")
                    .squadLeaderUsername("   leader_alice   ")
                    .build();

            assertTrue(member.isSquad());
            assertEquals("leader_alice", member.getEffectiveSquadLeader(), "Must trim leader username");
        }

        @Test
        @DisplayName("Edge Case: Case insensitivity of SQUAD tier")
        void testSquadTierCaseInsensitive() {
            User userLower = User.builder().username("u1").subscriptionTier("squad").build();
            User userMixed = User.builder().username("u2").subscriptionTier("SqUaD").build();

            assertTrue(userLower.isSquad());
            assertEquals("u1", userLower.getEffectiveSquadLeader());
            assertTrue(userMixed.isSquad());
            assertEquals("u2", userMixed.getEffectiveSquadLeader());
        }

        @Test
        @DisplayName("Persistence: squad_leader_username up to 50 chars persisted and reloaded correctly")
        void testSquadLeaderUsernamePersistenceBoundary() {
            String fiftyChars = "a".repeat(50);
            User user = User.builder()
                    .username("persist_boundary_usr")
                    .email("persist_boundary@example.com")
                    .password("secret")
                    .fullName("Boundary User")
                    .subscriptionTier("SQUAD")
                    .squadLeaderUsername(fiftyChars)
                    .build();

            user = userRepository.saveAndFlush(user);
            entityManager.clear();

            User reloaded = userRepository.findById(user.getId()).orElseThrow();
            assertEquals(fiftyChars, reloaded.getSquadLeaderUsername());
            assertEquals(fiftyChars, reloaded.getEffectiveSquadLeader());
        }
    }

    @Nested
    @DisplayName("Adversarial Tests: QuizAttempt User Mapping & JPA Lifecycle")
    class QuizAttemptMappingAdversarialTests {

        private Course createCourse(String code) {
            Course course = Course.builder()
                    .code(code)
                    .name("Stress Course " + code)
                    .build();
            return courseRepository.save(course);
        }

        private Quiz createQuiz(Course course, String title) {
            Quiz quiz = Quiz.builder()
                    .title(title)
                    .course(course)
                    .totalQuestions(10)
                    .build();
            return quizRepository.save(quiz);
        }

        @Test
        @DisplayName("Stress: Multiple anonymous attempts (user = null) for the same quiz")
        void testMultipleAnonymousAttempts() {
            Course course = createCourse("ANON-101");
            Quiz quiz = createQuiz(course, "Anonymous Quiz");

            for (int i = 1; i <= 5; i++) {
                QuizAttempt attempt = QuizAttempt.builder()
                        .quiz(quiz)
                        .user(null)
                        .studentName("Khách " + i)
                        .totalScore(8.0 + (i * 0.2))
                        .correctCount(8)
                        .totalCount(10)
                        .build();
                quizAttemptRepository.save(attempt);
            }
            quizAttemptRepository.flush();

            List<QuizAttempt> attempts = quizAttemptRepository.findAll();
            long anonCount = attempts.stream()
                    .filter(a -> a.getQuiz().getId().equals(quiz.getId()) && a.getUser() == null)
                    .count();

            assertEquals(5, anonCount, "Should allow multiple anonymous attempts with user = null");
        }

        @Test
        @DisplayName("Stress: Multiple attempts for the same User (1-to-N relationship)")
        void testMultipleAttemptsSameUser() {
            Course course = createCourse("USER-101");
            Quiz quiz = createQuiz(course, "User Repeat Quiz");

            User user = User.builder()
                    .username("repeat_user")
                    .email("repeat_user@example.com")
                    .password("pwd123")
                    .fullName("Repeat User")
                    .subscriptionTier("SQUAD")
                    .build();
            user = userRepository.save(user);

            for (int i = 1; i <= 3; i++) {
                QuizAttempt attempt = QuizAttempt.builder()
                        .quiz(quiz)
                        .user(user)
                        .studentName(user.getFullName())
                        .totalScore((double) i * 3)
                        .correctCount(i * 3)
                        .totalCount(10)
                        .build();
                quizAttemptRepository.save(attempt);
            }
            quizAttemptRepository.flush();
            entityManager.clear();

            List<QuizAttempt> reloaded = quizAttemptRepository.findAll();
            Long userId = user.getId();
            long userAttemptCount = reloaded.stream()
                    .filter(a -> a.getUser() != null && a.getUser().getId().equals(userId))
                    .count();

            assertEquals(3, userAttemptCount);
        }

        @Test
        @DisplayName("Lifecycle: Transition QuizAttempt from user=null to user!=null (claim attempt)")
        void testTransitionNullUserToRegisteredUser() {
            Course course = createCourse("TRANS-101");
            Quiz quiz = createQuiz(course, "Transition Quiz");

            QuizAttempt attempt = QuizAttempt.builder()
                    .quiz(quiz)
                    .user(null)
                    .studentName("Vãng lai")
                    .totalScore(10.0)
                    .correctCount(10)
                    .totalCount(10)
                    .build();
            attempt = quizAttemptRepository.saveAndFlush(attempt);
            assertNull(attempt.getUser());

            User registered = User.builder()
                    .username("newly_registered")
                    .email("newly_registered@example.com")
                    .password("pwd")
                    .fullName("Newly Registered")
                    .build();
            registered = userRepository.saveAndFlush(registered);

            // Gán user sau khi đăng ký
            attempt.setUser(registered);
            attempt.setStudentName(registered.getFullName());
            quizAttemptRepository.saveAndFlush(attempt);
            entityManager.clear();

            QuizAttempt found = quizAttemptRepository.findById(attempt.getId()).orElseThrow();
            assertNotNull(found.getUser());
            assertEquals(registered.getId(), found.getUser().getId());
            assertEquals("newly_registered", found.getUser().getUsername());
        }

        @Test
        @DisplayName("Detached Entity: Calling toString() on QuizAttempt does not trigger LazyInitializationException")
        void testToStringExcludesUserLazy() {
            Course course = createCourse("LAZY-101");
            Quiz quiz = createQuiz(course, "Lazy Quiz");

            User user = User.builder()
                    .username("lazy_usr")
                    .email("lazy_usr@example.com")
                    .password("pwd")
                    .fullName("Lazy User")
                    .build();
            user = userRepository.saveAndFlush(user);

            QuizAttempt attempt = QuizAttempt.builder()
                    .quiz(quiz)
                    .user(user)
                    .studentName("Lazy User")
                    .totalScore(7.0)
                    .correctCount(7)
                    .totalCount(10)
                    .build();
            final QuizAttempt savedAttempt = quizAttemptRepository.saveAndFlush(attempt);

            entityManager.detach(savedAttempt);

            // Khi entity detached, toString() không được gọi user.toString() gây LazyInitializationException
            assertDoesNotThrow(() -> {
                String str = savedAttempt.toString();
                assertNotNull(str);
                assertFalse(str.contains("user="), "Lombok @ToString.Exclude must exclude user field");
            });
        }

        @Test
        @DisplayName("Integrity: Deleting QuizAttempt does NOT delete associated User")
        void testDeletingAttemptDoesNotDeleteUser() {
            Course course = createCourse("DEL-101");
            Quiz quiz = createQuiz(course, "Delete Quiz");

            User user = User.builder()
                    .username("safe_user")
                    .email("safe_user@example.com")
                    .password("pwd")
                    .fullName("Safe User")
                    .build();
            user = userRepository.saveAndFlush(user);

            QuizAttempt attempt = QuizAttempt.builder()
                    .quiz(quiz)
                    .user(user)
                    .studentName("Safe User")
                    .totalScore(8.0)
                    .correctCount(8)
                    .totalCount(10)
                    .build();
            attempt = quizAttemptRepository.saveAndFlush(attempt);
            Long attemptId = attempt.getId();
            Long userId = user.getId();

            quizAttemptRepository.deleteById(attemptId);
            quizAttemptRepository.flush();
            entityManager.clear();

            assertFalse(quizAttemptRepository.findById(attemptId).isPresent());
            assertTrue(userRepository.findById(userId).isPresent(), "User must NOT be cascade deleted!");
        }
    }

    @Nested
    @DisplayName("Adversarial Tests: PDFBox 2.x & OpenHTMLToPDF Runtime Interop")
    class PdfBoxOpenHtmlToPdfRuntimeTests {

        @Test
        @DisplayName("Runtime: OpenHTMLToPDF builds PDF using PDFBox 2.0.31 without NoSuchFieldError")
        void testOpenHtmlToPdfBuildsSuccessfully() throws IOException {
            String html = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="UTF-8"/>
                        <style>
                            body { font-family: sans-serif; font-size: 14pt; }
                            h1 { color: #2563eb; }
                        </style>
                    </head>
                    <body>
                        <h1>Smart StudyHub Quiz Test</h1>
                        <p>Testing PDF generation under OpenHTMLToPDF 1.0.10 and PDFBox 2.0.31 compatibility.</p>
                    </body>
                    </html>
                    """;

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(os);

            assertDoesNotThrow(() -> builder.run(), "OpenHTMLToPDF must run without NoSuchFieldError under PDFBox 2.x");
            byte[] pdfBytes = os.toByteArray();
            assertTrue(pdfBytes.length > 0, "Generated PDF bytes should not be empty");

            // Verify with PDFBox 2.x PDDocument.load
            try (PDDocument doc = PDDocument.load(pdfBytes)) {
                assertEquals(1, doc.getNumberOfPages());
            }
        }

        @Test
        @DisplayName("Round-trip: PdfTextExtractor extracts text from PDFBox 2.x document")
        void testPdfTextExtractorRoundTrip() throws Exception {
            byte[] pdfBytes;
            try (PDDocument doc = new PDDocument()) {
                PDPage page = new PDPage();
                doc.addPage(page);
                try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                    cs.beginText();
                    cs.setFont(PDType1Font.HELVETICA, 12);
                    cs.showText("Empirical Stress Testing for Milestone M1 PDFBox 2.x compatibility.");
                    cs.endText();
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                doc.save(baos);
                pdfBytes = baos.toByteArray();
            }

            org.springframework.mock.web.MockMultipartFile file =
                    new org.springframework.mock.web.MockMultipartFile("file", "test.pdf", "application/pdf", pdfBytes);

            String extracted = pdfTextExtractor.extractText(file, null, null);
            assertNotNull(extracted);
            assertTrue(extracted.contains("Empirical Stress Testing for Milestone M1"));
        }
    }
}
