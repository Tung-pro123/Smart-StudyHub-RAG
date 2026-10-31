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
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class Milestone1EmpiricalStressTest {

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private PdfTextExtractor pdfTextExtractor;

    // =========================================================================
    // 1. EMPIRICAL CHALLENGE: OpenHTMLToPDF + PDFBox 2.0.31 Compatibility
    // =========================================================================

    @Test
    @DisplayName("Stress 1: OpenHTMLToPDF PdfRendererBuilder runs without NoSuchFieldError")
    void testOpenHtmlToPdfBasicRenderingWithoutNoSuchFieldError() throws Exception {
        String html = "<!DOCTYPE html><html><head><meta charset='UTF-8'/></head><body><h1>Milestone 1 Test</h1><p>Testing OpenHTMLToPDF with PDFBox 2.0.31</p></body></html>";

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.withHtmlContent(html, null);
        builder.toStream(os);

        assertDoesNotThrow(() -> builder.run(), "PdfRendererBuilder.run() must execute without NoSuchFieldError");

        byte[] pdfBytes = os.toByteArray();
        assertTrue(pdfBytes.length > 0, "Generated PDF bytes must not be empty");

        // Verify PDF Header %PDF-
        String header = new String(pdfBytes, 0, 5);
        assertEquals("%PDF-", header, "Generated file must start with %PDF- header");

        // Verify PDFBox 2.0.31 can parse the generated PDF
        try (PDDocument doc = PDDocument.load(pdfBytes)) {
            assertEquals(1, doc.getNumberOfPages(), "PDF must have 1 page");
            PDFTextStripper stripper = new PDFTextStripper();
            String extracted = stripper.getText(doc);
            assertTrue(extracted.contains("Milestone 1 Test"), "Extracted text must match rendered content");
        }
    }

    @Test
    @DisplayName("Stress 2: OpenHTMLToPDF with Arial font builder and Vietnamese text")
    void testOpenHtmlToPdfWithVietnameseFont() throws Exception {
        File fontFile = new File("src/main/resources/fonts/Arial.ttf");
        assertTrue(fontFile.exists(), "src/main/resources/fonts/Arial.ttf must exist for Vietnamese rendering");

        String html = "<!DOCTYPE html><html><head><meta charset='UTF-8'/>"
                + "<style>body { font-family: 'Arial', sans-serif; }</style>"
                + "</head><body>"
                + "<h1>Đề thi trắc nghiệm Kiến trúc Máy tính</h1>"
                + "<p>Thí sinh: Nguyễn Văn Nhật - Nhóm: Study Squad</p>"
                + "<p>Điểm số: 10/10 - Đạt kết quả Xuất Sắc</p>"
                + "</body></html>";

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFont(fontFile, "Arial");
        builder.withHtmlContent(html, null);
        builder.toStream(os);

        assertDoesNotThrow(() -> builder.run(), "OpenHTMLToPDF with font must not fail");

        byte[] pdfBytes = os.toByteArray();
        assertTrue(pdfBytes.length > 1000, "PDF with embedded font should be reasonably sized");

        try (PDDocument doc = PDDocument.load(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String extracted = stripper.getText(doc);
            assertTrue(extracted.contains("Đề thi trắc nghiệm"), "Unicode Vietnamese text must be preserved");
            assertTrue(extracted.contains("Nguyễn Văn Nhật"), "Author name must be preserved");
        }
    }

    // =========================================================================
    // 2. EMPIRICAL CHALLENGE: QuizAttempt User Nullable & Legacy Queries
    // =========================================================================

    @Test
    @DisplayName("Stress 3: Mixed QuizAttempts (null user and non-null user) under legacy queries")
    void testMixedQuizAttemptsAndLegacyQueries() {
        Course course = courseRepository.save(Course.builder().code("CS-EMP-01").name("Empirical Course").build());
        Quiz quiz = quizRepository.save(Quiz.builder().title("Quiz Empirical").course(course).totalQuestions(5).build());

        User user1 = userRepository.save(User.builder().username("emp_user_1").email("emp1@test.com").password("pass").fullName("Emp One").build());
        User user2 = userRepository.save(User.builder().username("emp_user_2").email("emp2@test.com").password("pass").fullName("Emp Two").build());

        // 1. Attempt with user1
        QuizAttempt a1 = quizAttemptRepository.save(QuizAttempt.builder()
                .quiz(quiz).user(user1).studentName("Emp One").totalScore(10.0).correctCount(5).totalCount(5).build());

        // 2. Attempt with null user (legacy / anonymous)
        QuizAttempt a2 = quizAttemptRepository.save(QuizAttempt.builder()
                .quiz(quiz).user(null).studentName("Anonymous").totalScore(6.0).correctCount(3).totalCount(5).build());

        // 3. Attempt with user2
        QuizAttempt a3 = quizAttemptRepository.save(QuizAttempt.builder()
                .quiz(quiz).user(user2).studentName("Emp Two").totalScore(8.0).correctCount(4).totalCount(5).build());

        // Verify count
        assertEquals(3, quizAttemptRepository.countByQuizId(quiz.getId()));

        // Verify findAllByQuizIdOrderByCompletedAtDesc works seamlessly across mixed null/non-null users
        List<QuizAttempt> attempts = quizAttemptRepository.findAllByQuizIdOrderByCompletedAtDesc(quiz.getId());
        assertEquals(3, attempts.size());

        // Verify findRecentAttemptsByCourseId works seamlessly across mixed null/non-null users
        List<QuizAttempt> recentCourseAttempts = quizAttemptRepository.findRecentAttemptsByCourseId(course.getId());
        assertEquals(3, recentCourseAttempts.size());

        // Verify null-safety when checking user field
        long nullUserCount = attempts.stream().filter(a -> a.getUser() == null).count();
        assertEquals(1, nullUserCount, "Exactly one attempt must have null user");

        long validUserCount = attempts.stream().filter(a -> a.getUser() != null).count();
        assertEquals(2, validUserCount, "Two attempts must have valid user");

        // Verify toString() does not trigger NPE or LazyInit
        assertDoesNotThrow(() -> {
            for (QuizAttempt a : attempts) {
                String s = a.toString();
                assertNotNull(s);
            }
        });
    }

    // =========================================================================
    // 3. EMPIRICAL CHALLENGE: User Squad Methods Boundary Testing
    // =========================================================================

    @ParameterizedTest
    @CsvSource({
            "SQUAD, , true, leaderUser",
            "SQUAD, leader1, true, leader1",
            "squad, leader2, true, leader2",
            "FREE, , false, null",
            "FREE, leader3, true, leader3",
            "CAMPUS, , false, null",
            "PRO, , false, null"
    })
    @DisplayName("Stress 4: User isSquad and getEffectiveSquadLeader boundary cases")
    void testUserSquadPermutations(String tier, String squadLeader, boolean expectedIsSquad, String expectedEffectiveLeader) {
        String effectiveExpected = "null".equals(expectedEffectiveLeader) ? null :
                ("leaderUser".equals(expectedEffectiveLeader) ? "testUser" : expectedEffectiveLeader);

        User user = User.builder()
                .username("testUser")
                .subscriptionTier(tier)
                .squadLeaderUsername(squadLeader)
                .build();

        assertEquals(expectedIsSquad, user.isSquad(), "isSquad mismatch for tier=" + tier + ", leader=" + squadLeader);
        assertEquals(effectiveExpected, user.getEffectiveSquadLeader(), "getEffectiveSquadLeader mismatch");
    }

    @Test
    @DisplayName("Stress 5: User squad whitespace trimming and null safety")
    void testUserSquadWhitespaceAndNullSafety() {
        User userWithSpaces = User.builder()
                .username("memberX")
                .subscriptionTier("FREE")
                .squadLeaderUsername("   bossLeader   ")
                .build();
        assertTrue(userWithSpaces.isSquad());
        assertEquals("bossLeader", userWithSpaces.getEffectiveSquadLeader(), "Must trim leading and trailing spaces");

        User userWithBlankLeader = User.builder()
                .username("freeMember")
                .subscriptionTier("FREE")
                .squadLeaderUsername("   ")
                .build();
        assertFalse(userWithBlankLeader.isSquad());
        assertNull(userWithBlankLeader.getEffectiveSquadLeader());

        User userNullTier = User.builder()
                .username("noTier")
                .subscriptionTier(null)
                .squadLeaderUsername(null)
                .build();
        assertFalse(userNullTier.isSquad());
        assertNull(userNullTier.getEffectiveSquadLeader());
    }

    // =========================================================================
    // 4. EMPIRICAL CHALLENGE: PdfTextExtractor Robustness with PDDocument.load
    // =========================================================================

    @Test
    @DisplayName("Stress 6: PdfTextExtractor boundary conditions and corrupt file handling")
    void testPdfTextExtractorBoundaryAndCorrupt() throws Exception {
        // 1. Corrupt byte stream
        byte[] garbage = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        MockMultipartFile corruptFile = new MockMultipartFile("file", "corrupt.pdf", "application/pdf", garbage);
        assertThrows(IOException.class, () -> pdfTextExtractor.extractText(corruptFile));

        // 2. Empty file
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);
        assertThrows(IllegalArgumentException.class, () -> pdfTextExtractor.extractText(emptyFile));

        // 3. Multi-page PDF with inverted page range
        byte[] pdfBytes;
        try (PDDocument doc = new PDDocument()) {
            for (int i = 1; i <= 3; i++) {
                PDPage page = new PDPage();
                doc.addPage(page);
                try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                    cs.beginText();
                    cs.setFont(PDType1Font.HELVETICA, 12);
                    cs.showText("Page " + i + " Content for Testing");
                    cs.endText();
                }
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            pdfBytes = baos.toByteArray();
        }

        MockMultipartFile validFile = new MockMultipartFile("file", "test3p.pdf", "application/pdf", pdfBytes);

        // Inverted range: fromPage = 3, toPage = 1 -> should auto-swap to 1..3
        String swappedText = pdfTextExtractor.extractText(validFile, 3, 1);
        assertTrue(swappedText.contains("Page 1 Content"));
        assertTrue(swappedText.contains("Page 3 Content"));

        // Single page extraction: fromPage = 2, toPage = 2
        String p2Text = pdfTextExtractor.extractText(validFile, 2, 2);
        assertTrue(p2Text.contains("Page 2 Content"));
        assertFalse(p2Text.contains("Page 1 Content"));

        // Out of bounds page: fromPage = 99 -> clamped to totalPages
        String clampedText = pdfTextExtractor.extractText(validFile, 99, 99);
        assertTrue(clampedText.contains("Page 3 Content"));
    }
}
