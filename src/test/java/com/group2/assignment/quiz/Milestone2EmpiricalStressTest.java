package com.group2.assignment.quiz;

import com.group2.assignment.core.entity.Course;
import com.group2.assignment.core.repository.CourseRepository;
import com.group2.assignment.quiz.entity.Quiz;
import com.group2.assignment.quiz.entity.QuizQuestion;
import com.group2.assignment.quiz.repository.QuizQuestionRepository;
import com.group2.assignment.quiz.repository.QuizRepository;
import com.group2.assignment.quiz.service.QuizPdfExportService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class Milestone2EmpiricalStressTest {

    @Autowired
    private QuizPdfExportService quizPdfExportService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuizQuestionRepository quizQuestionRepository;

    private Course sampleCourse;

    @BeforeEach
    void setUp() {
        sampleCourse = courseRepository.save(Course.builder()
                .name("Kỹ Thuật Lập Trình & RAG AI")
                .code("CS401_" + System.currentTimeMillis())
                .description("Khoá học nền tảng AI nâng cao")
                .build());
    }

    // =========================================================================
    // 1. EMPIRICAL CHALLENGE: Comprehensive Vietnamese Diacritics Oracle
    // =========================================================================

    @Test
    @DisplayName("Stress 1: Full Vietnamese alphabet diacritics coverage (all tones, vowels, uppercase/lowercase)")
    void testFullVietnameseDiacriticsCoverage() throws IOException {
        String fullVietnameseCharset = 
                "A À Á Ả Ã Ạ Ă Ằ Ắ Ẳ Ẵ Ặ Â Ầ Ấ Ẩ Ẫ Ậ " +
                "a à á ả ã ạ ă ằ ắ ẳ ẵ ặ â ầ ấ ẩ ẫ ậ " +
                "E È É Ẻ Ẽ Ẹ Ê Ề Ế Ể Ễ Ệ " +
                "e è é ẻ ẽ ẹ ê ề ế ể ễ ệ " +
                "I Ì Í Ỉ Ĩ Ị " +
                "i ì í ỉ ĩ ị " +
                "O Ò Ó Ỏ Õ Ọ Ô Ồ Ố Ổ Ỗ Ộ Ơ Ờ Ớ Ở Ỡ Ợ " +
                "o ò ó ỏ õ ọ ô ồ ố ổ ỗ ộ ơ ờ ớ ở ỡ ợ " +
                "U Ù Ú Ủ Ũ Ụ Ư Ừ Ứ Ử Ữ Ự " +
                "u ù ú ủ ũ ụ ư ừ ứ ử ữ ự " +
                "Y Ỳ Ý Ỷ Ỹ Ỵ " +
                "y ỳ ý ỷ ỹ ỵ " +
                "D Đ d đ";

        String sentenceTest = "Cộng hòa Xã hội Chủ nghĩa Việt Nam - Độc lập - Tự do - Hạnh phúc. " +
                "Trường Đại học Bách Khoa Thành phố Hồ Chí Minh: Kiểm thử phần mềm tự động hóa!";

        Quiz quiz = Quiz.builder()
                .title("Kiểm tra Bảng chữ cái Tiếng Việt: " + sentenceTest)
                .chapterTopic("Chương 1: Tiếng Việt Unicode Hoàn Chỉnh")
                .difficulty("EASY")
                .timeLimitMinutes(45)
                .totalQuestions(1)
                .course(sampleCourse)
                .build();

        QuizQuestion q = QuizQuestion.builder()
                .quiz(quiz)
                .questionText("Chọn tập hợp ký tự tiếng Việt chuẩn: " + fullVietnameseCharset)
                .optionA("Nguyễn Văn A - Đã hoàn thành bài thi xuất sắc")
                .optionB("Phạm Thị Bưởi - Điểm số đạt tuyệt đối 10/10")
                .optionC("Trần Đức Thắng - Ứng dụng công nghệ Trí tuệ nhân tạo")
                .optionD("Lê Hoàng Đăng - Đổi mới sáng tạo trong giáo dục số")
                .correctAnswer("A")
                .explanation("Giải thích chi tiết: Ký tự Tiếng Việt phải được nhúng (subset) đầy đủ trong PDF không lỗi font hay ô vuông: " + fullVietnameseCharset)
                .sortOrder(1)
                .build();

        byte[] pdfBytes = quizPdfExportService.exportQuizToPdf(quiz, List.of(q), true);
        assertNotNull(pdfBytes, "PDF không được null");
        assertTrue(pdfBytes.length > 1000, "PDF phải chứa font nhúng đầy đủ");

        try (PDDocument document = PDDocument.load(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String extractedText = stripper.getText(document);

            // Kiểm tra không chứa ký tự lỗi thay thế
            assertFalse(extractedText.contains("???"), "Text không được chứa dấu hỏi ô vuông '???'");
            assertFalse(extractedText.contains("\uFFFD"), "Text không được chứa unicode replacement char");

            // Kiểm tra các ký tự tiếng Việt đặc thù có tồn tại nguyên vẹn
            assertTrue(extractedText.contains("Cộng hòa Xã hội Chủ nghĩa Việt Nam"), "Phải giữ nguyên quốc hiệu tiếng Việt");
            assertTrue(extractedText.contains("Bách Khoa Thành phố Hồ Chí Minh"), "Phải chứa tên trường");
            assertTrue(extractedText.contains("Phạm Thị Bưởi"), "Phải chứa ký tự ưở");
            assertTrue(extractedText.contains("Lê Hoàng Đăng"), "Phải chứa chữ Đ, ă");
            assertTrue(extractedText.contains("Trần Đức Thắng"), "Phải chứa chữ ắ");
            assertTrue(extractedText.contains("đạt tuyệt đối"), "Phải chứa chữ ệ, ố");
            assertTrue(extractedText.contains("Nguyễn Văn A"), "Phải chứa chữ ễ");
        }
    }

    // =========================================================================
    // 2. EMPIRICAL CHALLENGE: HTML Special Characters & XHTML Escaping Stress
    // =========================================================================

    @Test
    @DisplayName("Stress 2: Questions and options containing HTML special characters (<, >, &, \", ') parse without XML syntax error")
    void testHtmlAndXmlSpecialCharactersEscaping() throws IOException {
        Quiz quiz = Quiz.builder()
                .title("Đề thi Lập trình: Ký tự đặc biệt <Test> & \"Quote\" 'Single' & More")
                .chapterTopic("Chương 2: XML & HTML Entities & Logic Operators")
                .difficulty("HARD")
                .timeLimitMinutes(30)
                .totalQuestions(1)
                .course(sampleCourse)
                .build();

        QuizQuestion q = QuizQuestion.builder()
                .quiz(quiz)
                .questionText("Trong Java, so sánh biểu thức: a < 10 && b > 20 || c != null & d == true có kết quả gì?")
                .optionA("List<Map<String, Object>> là generic hợp lệ")
                .optionB("Biểu thức 1 < 2 && 3 > 2 trả về true & in ra màn hình")
                .optionC("Tag HTML <script>alert('xss');</script> & thẻ <div>")
                .optionD("Ký tự ampersand: Rock & Roll & Tom & Jerry")
                .correctAnswer("B")
                .explanation("Vì toán tử && ngắn mạch: (a < 10) được đánh giá trước, & là bitwise/non-short-circuit.")
                .sortOrder(1)
                .build();

        // Must NOT throw SAXParseException or any XML parsing error
        byte[] pdfBytes = quizPdfExportService.exportQuizToPdf(quiz, List.of(q), true);
        assertNotNull(pdfBytes);

        try (PDDocument document = PDDocument.load(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String extractedText = stripper.getText(document);

            assertTrue(extractedText.contains("a < 10 && b > 20"), "PDF text phải hiển thị đúng dấu < và &&");
            assertTrue(extractedText.contains("List<Map<String, Object>>"), "PDF text phải hiển thị generic types");
            assertTrue(extractedText.contains("Rock & Roll"), "PDF text phải hiển thị ký tự &");
            assertTrue(extractedText.contains("Rock & Roll & Tom & Jerry"));
        }
    }

    // =========================================================================
    // 3. EMPIRICAL CHALLENGE: Boundary Conditions & Null Safety
    // =========================================================================

    @Test
    @DisplayName("Stress 3: Empty questions list renders valid PDF with 0 questions without error")
    void testEmptyQuestionsListRendersCleanly() throws IOException {
        Quiz quiz = Quiz.builder()
                .title("Đề thi chưa có câu hỏi nào")
                .chapterTopic("Rỗng")
                .difficulty("EASY")
                .timeLimitMinutes(10)
                .totalQuestions(0)
                .course(sampleCourse)
                .build();

        byte[] pdfBytesWithoutAnswers = quizPdfExportService.exportQuizToPdf(quiz, List.of(), false);
        assertNotNull(pdfBytesWithoutAnswers);
        assertTrue(pdfBytesWithoutAnswers.length > 500);

        byte[] pdfBytesWithAnswers = quizPdfExportService.exportQuizToPdf(quiz, List.of(), true);
        assertNotNull(pdfBytesWithAnswers);
        assertTrue(pdfBytesWithAnswers.length > 500);

        try (PDDocument doc = PDDocument.load(pdfBytesWithoutAnswers)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);
            assertTrue(text.contains("Đề thi chưa có câu hỏi nào"));
            assertTrue(text.contains("Số câu: 0"));
        }
    }

    @Test
    @DisplayName("Stress 4: Null questions parameter in exportQuizToPdf is gracefully handled")
    void testNullQuestionsParameterHandled() throws IOException {
        Quiz quiz = Quiz.builder()
                .title("Đề thi null questions list")
                .course(sampleCourse)
                .build();

        byte[] pdfBytes = quizPdfExportService.exportQuizToPdf(quiz, null, false);
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 500);
    }

    @Test
    @DisplayName("Stress 5: Null question explanation, blank explanation, and null course fields")
    void testNullAndBlankExplanationsAndNullCourse() throws IOException {
        Quiz quiz = Quiz.builder()
                .title("Đề thi kiểm tra Null Fields")
                .chapterTopic(null)
                .difficulty(null)
                .timeLimitMinutes(null)
                .totalQuestions(null)
                .course(null) // course null: should fallback to 'Chung'
                .build();

        QuizQuestion q1 = QuizQuestion.builder()
                .quiz(quiz)
                .questionText("Câu hỏi 1 với explanation null")
                .optionA("A1").optionB("B1").optionC("C1").optionD("D1")
                .correctAnswer("A")
                .explanation(null) // null explanation
                .sortOrder(1)
                .build();

        QuizQuestion q2 = QuizQuestion.builder()
                .quiz(quiz)
                .questionText("Câu hỏi 2 với explanation blank")
                .optionA("A2").optionB("B2").optionC("C2").optionD("D2")
                .correctAnswer("B")
                .explanation("   ") // whitespace only
                .sortOrder(2)
                .build();

        QuizQuestion q3 = QuizQuestion.builder()
                .quiz(quiz)
                .questionText("Câu hỏi 3 với explanation rỗng")
                .optionA("A3").optionB("B3").optionC("C3").optionD("D3")
                .correctAnswer("C")
                .explanation("") // empty string
                .sortOrder(3)
                .build();

        byte[] pdfBytes = quizPdfExportService.exportQuizToPdf(quiz, List.of(q1, q2, q3), true);
        assertNotNull(pdfBytes);

        try (PDDocument doc = PDDocument.load(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);
            assertTrue(text.contains("Chung"), "Course null phải fallback thành 'Chung'");
            assertTrue(text.contains("MEDIUM"), "Difficulty null phải fallback thành 'MEDIUM'");
            assertTrue(text.contains("15") && text.contains("phút"), "TimeLimitMinutes null phải fallback thành '15'");
            assertTrue(text.contains("ĐÁP ÁN VÀ LỜI GIẢI CHI TIẾT"));
        }
    }

    // =========================================================================
    // 4. EMPIRICAL CHALLENGE: Multi-Page Scaling (50 Questions)
    // =========================================================================

    @Test
    @DisplayName("Stress 6: Large quiz with 50 questions generates multi-page PDF without OOM or layout corruption")
    void testLargeQuizWith50Questions() throws IOException {
        Quiz quiz = Quiz.builder()
                .title("Kỳ thi Đánh giá Năng lực Toàn diện: 50 câu trắc nghiệm chuyên sâu")
                .chapterTopic("Toàn bộ chương trình")
                .difficulty("HARD")
                .timeLimitMinutes(90)
                .totalQuestions(50)
                .course(sampleCourse)
                .build();

        List<QuizQuestion> questions = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            questions.add(QuizQuestion.builder()
                    .quiz(quiz)
                    .questionText(String.format("Câu hỏi số %d: Phân tích kiến trúc hệ thống phân tán và cơ chế chịu lỗi Byzantine Fault Tolerance trong mạng lưới blockchain ngang hàng?", i))
                    .optionA("Độ trễ thấp, không cần đồng thuận phân tán giữa các node mạng")
                    .optionB("Thuật toán đồng thuận PBFT đảm bảo an toàn với tối đa 1/3 số node bị lỗi hoặc tấn công")
                    .optionC("Chỉ áp dụng cho các cơ sở dữ liệu tập trung nguyên khối quan hệ ACID")
                    .optionD("Không hỗ trợ xác thực mã hóa khóa công khai RSA và ECDSA")
                    .correctAnswer(i % 2 == 0 ? "B" : "A")
                    .explanation(String.format("Lời giải chi tiết câu %d: PBFT (Practical Byzantine Fault Tolerance) giải quyết bài toán các vị tướng Byzantine khi n >= 3f + 1.", i))
                    .sortOrder(i)
                    .build());
        }

        byte[] pdfBytes = quizPdfExportService.exportQuizToPdf(quiz, questions, true);
        assertNotNull(pdfBytes);

        try (PDDocument doc = PDDocument.load(pdfBytes)) {
            int pageCount = doc.getNumberOfPages();
            assertTrue(pageCount >= 5, "50 câu hỏi kèm đáp án phải tạo ra nhiều trang (thực tế: " + pageCount + " trang)");

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);
            assertTrue(text.contains("Câu hỏi số 1:"));
            assertTrue(text.contains("Câu hỏi số 50:"));
            assertTrue(text.contains("Lời giải chi tiết câu 50:"));
        }
    }

    // =========================================================================
    // 5. EMPIRICAL CHALLENGE: Concurrency & Thread-Safety Stress Test
    // =========================================================================

    @Test
    @DisplayName("Stress 7: Concurrent PDF generation across 10 threads runs safely without race condition")
    void testConcurrentPdfGeneration() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        List<Throwable> errors = new CopyOnWriteArrayList<>();

        Quiz quiz = Quiz.builder()
                .title("Đề thi Đa Luồng Concurrency Test")
                .course(sampleCourse)
                .timeLimitMinutes(20)
                .build();

        QuizQuestion q = QuizQuestion.builder()
                .quiz(quiz)
                .questionText("Trong Java, từ khóa volatile có tác dụng gì?")
                .optionA("Đảm bảo tính hiển thị (visibility) giữa các luồng bộ nhớ")
                .optionB("Khóa đoạn mã như synchronized block")
                .optionC("Ngăn chặn tạo thread mới")
                .optionD("Tự động Garbage Collection")
                .correctAnswer("A")
                .explanation("volatile đảm bảo giá trị biến được đọc trực tiếp từ Main Memory thay vì CPU Cache.")
                .sortOrder(1)
                .build();

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    byte[] pdf = quizPdfExportService.exportQuizToPdf(quiz, List.of(q), index % 2 == 0);
                    assertNotNull(pdf);
                    assertTrue(pdf.length > 500);
                    assertEquals('%', (char) pdf[0]);
                    successCount.incrementAndGet();
                } catch (Throwable t) {
                    errors.add(t);
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean completed = latch.await(60, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(completed, "Tất cả các luồng phải hoàn thành trong 60 giây");
        assertTrue(errors.isEmpty(), "Không được có ngoại lệ khi chạy đa luồng: " + errors);
        assertEquals(threadCount, successCount.get(), "Tất cả 10 luồng phải xuất PDF thành công");
    }

    // =========================================================================
    // 6. EMPIRICAL CHALLENGE: Long Text & Word Wrap Stress
    // =========================================================================

    @Test
    @DisplayName("Stress 8: Extremely long question text (1000+ chars) wraps without overflow or parser crash")
    void testExtremelyLongQuestionText() throws IOException {
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            longText.append("Đoạn văn bản câu hỏi siêu dài nhằm kiểm tra khả năng tự động xuống dòng và ngắt trang của bộ kết xuất OpenHTMLToPDF. ");
        }

        Quiz quiz = Quiz.builder()
                .title("Đề thi Văn bản Cực Dài")
                .course(sampleCourse)
                .build();

        QuizQuestion q = QuizQuestion.builder()
                .quiz(quiz)
                .questionText(longText.toString())
                .optionA("Lựa chọn A với nội dung dài: " + longText.substring(0, 150))
                .optionB("Lựa chọn B")
                .optionC("Lựa chọn C")
                .optionD("Lựa chọn D")
                .correctAnswer("A")
                .explanation("Lời giải siêu dài: " + longText.toString())
                .sortOrder(1)
                .build();

        byte[] pdf = quizPdfExportService.exportQuizToPdf(quiz, List.of(q), true);
        assertNotNull(pdf);

        try (PDDocument doc = PDDocument.load(pdf)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);
            assertTrue(text.contains("Đoạn văn bản câu hỏi siêu dài"));
            assertTrue(text.contains("Lời giải siêu dài"));
        }
    }

    // =========================================================================
    // 7. EMPIRICAL CHALLENGE: Error Handling when Quiz is null
    // =========================================================================

    @Test
    @DisplayName("Stress 9: exportQuizToPdf with null Quiz throws RuntimeException with clear error message")
    void testNullQuizThrowsException() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            quizPdfExportService.exportQuizToPdf(null, List.of(), false);
        });
        assertTrue(exception.getMessage().contains("Không thể tạo file PDF đề thi"), 
                "Thông điệp ngoại lệ phải rõ ràng: " + exception.getMessage());
    }

    // =========================================================================
    // 8. EMPIRICAL CHALLENGE: Font Registration & Embedded Subsets (400 & 700)
    // =========================================================================

    @Test
    @DisplayName("Stress 10: Verify Arial font asset, registration, and embedded font subsets in PDF")
    void testFontAssetAndEmbeddedSubsetWeights() throws Exception {
        // 1. Verify font file on classpath
        try (var is = getClass().getResourceAsStream("/fonts/Arial.ttf")) {
            assertNotNull(is, "File font /fonts/Arial.ttf phải tồn tại trên classpath");
            java.awt.Font awtFont = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, is);
            assertNotNull(awtFont);
            assertEquals("Arial", awtFont.getFamily(), "Font family phải là Arial");
        }

        // 2. Generate PDF containing both normal (400) and bold (700) text (h1, strong, .question-title)
        Quiz quiz = Quiz.builder()
                .title("Kiểm tra Font Subsetting và Trọng số Normal/Bold")
                .chapterTopic("Chương Font")
                .course(sampleCourse)
                .build();

        QuizQuestion q = QuizQuestion.builder()
                .quiz(quiz)
                .questionText("Nội dung câu hỏi in đậm và bình thường")
                .optionA("Lựa chọn A")
                .optionB("Lựa chọn B")
                .optionC("Lựa chọn C")
                .optionD("Lựa chọn D")
                .correctAnswer("A")
                .explanation("Giải thích in nghiêng với CSS font-style: italic")
                .sortOrder(1)
                .build();

        byte[] pdf = quizPdfExportService.exportQuizToPdf(quiz, List.of(q), true);
        assertNotNull(pdf);

        // 3. Inspect PDFBox font resources in document pages
        try (PDDocument doc = PDDocument.load(pdf)) {
            assertTrue(doc.getNumberOfPages() >= 2);
            boolean foundArialFont = false;

            for (org.apache.pdfbox.pdmodel.PDPage page : doc.getPages()) {
                org.apache.pdfbox.pdmodel.PDResources resources = page.getResources();
                for (org.apache.pdfbox.cos.COSName fontName : resources.getFontNames()) {
                    org.apache.pdfbox.pdmodel.font.PDFont font = resources.getFont(fontName);
                    assertNotNull(font);
                    if (font.getName().contains("Arial")) {
                        foundArialFont = true;
                        // In OpenHTMLToPDF subset fonts are named like XXXXXX+Arial
                        assertTrue(font.isEmbedded(), "Font Arial phải được nhúng trực tiếp (embedded) vào file PDF");
                    }
                }
            }

            assertTrue(foundArialFont, "PDF phải chứa font nhúng Arial đã đăng ký");
        }
    }
}
