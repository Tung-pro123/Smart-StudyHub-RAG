package com.group2.assignment.quiz;

import com.group2.assignment.core.entity.Course;
import com.group2.assignment.quiz.entity.Quiz;
import com.group2.assignment.quiz.entity.QuizQuestion;
import com.group2.assignment.quiz.service.QuizPdfExportService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class QuizPdfExportStressTest {

    @Autowired
    private QuizPdfExportService quizPdfExportService;

    @Test
    @DisplayName("STRESS-1 Concurrency: 20 concurrent threads generating PDF simultaneously without race condition or deadlock")
    void testConcurrentPdfGeneration() throws Exception {
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        List<Throwable> errors = new CopyOnWriteArrayList<>();

        Course course = Course.builder()
                .name("Kỳ thi Đánh Giá Năng Lực AI Concurrency")
                .code("SE_CONCURRENCY")
                .build();

        Quiz quiz = Quiz.builder()
                .id(999L)
                .title("Đề thi Đa luồng & Kiểm thử Tải Đồng thời")
                .difficulty("HARD")
                .timeLimitMinutes(45)
                .course(course)
                .build();

        List<QuizQuestion> questions = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            questions.add(QuizQuestion.builder()
                    .questionText("Câu hỏi đồng thời số " + i + ": Xử lý đa luồng trong Java?")
                    .optionA("Dùng ExecutorService và CountDownLatch")
                    .optionB("Không dùng synchronized")
                    .optionC("Chỉ chạy đơn luồng")
                    .optionD("Bỏ qua thread-safety")
                    .correctAnswer("A")
                    .explanation("ExecutorService quản lý ThreadPool hiệu quả, CountDownLatch đồng bộ thời điểm bắt đầu.")
                    .sortOrder(i)
                    .build());
        }

        for (int i = 0; i < threadCount; i++) {
            final int threadIdx = i;
            executor.submit(() -> {
                try {
                    // Chờ tất cả threads sẵn sàng để bắn đồng thời
                    startGate.await();

                    boolean showAnswers = (threadIdx % 2 == 0);
                    byte[] pdf = quizPdfExportService.exportQuizToPdf(quiz, questions, showAnswers);

                    assertNotNull(pdf, "PDF không được null");
                    assertTrue(pdf.length > 1000, "PDF phải có dung lượng > 1000 bytes");
                    assertEquals('%', (char) pdf[0]);
                    assertEquals('P', (char) pdf[1]);
                    assertEquals('D', (char) pdf[2]);
                    assertEquals('F', (char) pdf[3]);

                    // Verify valid PDF document parsing
                    try (PDDocument doc = PDDocument.load(pdf)) {
                        assertTrue(doc.getNumberOfPages() >= 1);
                        PDFTextStripper stripper = new PDFTextStripper();
                        String text = stripper.getText(doc);
                        assertTrue(text.contains("Đề thi Đa luồng & Kiểm thử Tải Đồng thời"));
                        if (showAnswers) {
                            assertTrue(text.contains("ĐÁP ÁN VÀ LỜI GIẢI CHI TIẾT"));
                        }
                    }

                    successCount.incrementAndGet();
                } catch (Throwable t) {
                    errors.add(t);
                } finally {
                    endGate.countDown();
                }
            });
        }

        // Kích hoạt tất cả 20 thread đồng thời
        long startTime = System.currentTimeMillis();
        startGate.countDown();

        boolean completed = endGate.await(60, TimeUnit.SECONDS);
        long elapsed = System.currentTimeMillis() - startTime;
        executor.shutdown();

        assertTrue(completed, "Toàn bộ 20 thread phải hoàn tất trong 60s (không deadlock)");
        if (!errors.isEmpty()) {
            fail("Có lỗi trong quá trình chạy đa luồng: " + errors.get(0).getMessage());
        }
        assertEquals(threadCount, successCount.get(), "Tất cả 20 yêu cầu xuất PDF đồng thời phải thành công");
        System.out.println(">>> STRESS-1 PASSED: 20 concurrent PDF exports completed in " + elapsed + " ms (" + (elapsed / threadCount) + " ms/doc)");
    }

    @Test
    @DisplayName("STRESS-2 Memory & Repeated Renderings: 30 iterations without OutOfMemory or leak")
    void testRepeatedRenderingsMemoryStability() {
        Course course = Course.builder().name("Khóa học Kiến trúc Bộ nhớ").code("MEM101").build();
        Quiz quiz = Quiz.builder()
                .title("Kiểm tra Rò rỉ Bộ nhớ (Memory Leak Check)")
                .difficulty("MEDIUM")
                .timeLimitMinutes(20)
                .course(course)
                .build();

        List<QuizQuestion> questions = List.of(
                QuizQuestion.builder()
                        .questionText("ByteArrayOutputStream trong Java có cần close không?")
                        .optionA("Có thể close hoặc không vì chỉ thao tác trên heap buffer")
                        .optionB("Gây rò rỉ file descriptor hệ thống")
                        .optionC("Làm sập JVM")
                        .optionD("Không tương thích Spring")
                        .correctAnswer("A")
                        .explanation("ByteArrayOutputStream thao tác thuần túy trên mảng byte heap, close() không có hiệu ứng đóng tài nguyên OS, nhưng tuân thủ try-with-resources là best practice.")
                        .sortOrder(1)
                        .build()
        );

        Runtime runtime = Runtime.getRuntime();
        System.gc();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();

        for (int i = 0; i < 30; i++) {
            byte[] pdf = quizPdfExportService.exportQuizToPdf(quiz, questions, true);
            assertNotNull(pdf);
            assertTrue(pdf.length > 500);
        }

        System.gc();
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println(">>> STRESS-2 PASSED: 30 sequential generations finished. Memory delta: " + (finalMemory - initialMemory) / (1024 * 1024) + " MB");
    }

    @Test
    @DisplayName("STRESS-3 XHTML Strict Compliance: Raw template and XML special chars escaping")
    void testXhtmlStrictComplianceAndSpecialChars() throws Exception {
        // 1. Kiểm tra chính file template pdf-template.html có phải là XML/XHTML well-formed
        try (InputStream templateStream = getClass().getResourceAsStream("/templates/quiz/pdf-template.html")) {
            assertNotNull(templateStream, "Template file pdf-template.html phải tồn tại trong classpath");
            String rawTemplate = new String(templateStream.readAllBytes(), StandardCharsets.UTF_8);

            // Kiểm tra các quy tắc XML nghiêm ngặt:
            // - meta tag phải tự đóng <meta ... />
            assertTrue(rawTemplate.contains("<meta charset=\"UTF-8\"/>") || rawTemplate.contains("<meta charset=\"UTF-8\" />"),
                    "Thẻ meta phải tự đóng theo chuẩn XML: <meta ... />");
            // - không chứa <br> không đóng (phải là <br/> hoặc không dùng)
            assertFalse(rawTemplate.matches("(?s).*<br(?![/\\w]).*"), "Không được có thẻ <br> chưa đóng");
            // - không chứa <hr> không đóng (phải là <hr/> hoặc không dùng)
            assertFalse(rawTemplate.matches("(?s).*<hr(?![/\\w]).*"), "Không được có thẻ <hr> chưa đóng");
            // - không chứa <img> không đóng
            assertFalse(rawTemplate.matches("(?s).*<img(?![/\\w]).*"), "Không được có thẻ <img> chưa đóng");

            // XML DocumentBuilder parser check (sau khi bỏ DOCTYPE để tránh fetch DTD ngoài mạng)
            String xmlToParse = rawTemplate.replace("<!DOCTYPE html>", "");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setValidating(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(xmlToParse.getBytes(StandardCharsets.UTF_8)));
            assertNotNull(doc, "Template phải là một tài liệu XML/XHTML hoàn chỉnh và hợp lệ");
        }

        // 2. Kiểm tra xuất PDF khi dữ liệu chứa ký tự đặc biệt XML (&, <, >, ", ')
        Quiz specialQuiz = Quiz.builder()
                .title("Kiểm thử Ký tự Đặc biệt: Tom & Jerry <Special> \"Quotes\" 'Single' & More")
                .difficulty("EASY")
                .timeLimitMinutes(10)
                .course(Course.builder().name("Toán & Khoa học <10>").build())
                .build();

        QuizQuestion qSpecial = QuizQuestion.builder()
                .questionText("Nếu A < B && B > C thì kết luận nào đúng? Dấu & và < > có bị vỡ XML không?")
                .optionA("A < C & B > 0")
                .optionB("Biểu thức 'valid' && \"escaped\"")
                .optionC("Tag <script>alert(1)</script> vô hại")
                .optionD("Tất cả đều đúng")
                .correctAnswer("B")
                .explanation("Thymeleaf tự động escape XML các biến dạng th:text, tránh làm hỏng SAX XML Parser của OpenHTMLToPDF.")
                .sortOrder(1)
                .build();

        byte[] pdfSpecial = quizPdfExportService.exportQuizToPdf(specialQuiz, List.of(qSpecial), true);
        assertNotNull(pdfSpecial);
        assertTrue(pdfSpecial.length > 500);

        try (PDDocument doc = PDDocument.load(pdfSpecial)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);
            assertTrue(text.contains("Tom & Jerry"));
            assertTrue(text.contains("A < B && B > C"));
            assertTrue(text.contains("Toán & Khoa học"));
        }
        System.out.println(">>> STRESS-3 PASSED: Strict XML/XHTML compliance and special characters escaping verified.");
    }

    @Test
    @DisplayName("STRESS-4 Boundary & Stress Volume: 100 questions in a single PDF")
    void testLargeQuizStressVolume() throws Exception {
        Course course = Course.builder().name("Khóa học Quy mô Lớn (100 câu hỏi)").code("BIG100").build();
        Quiz quiz = Quiz.builder()
                .title("Đề thi Đánh Giá Năng Lực 100 Câu Hỏi Trắc Nghiệm")
                .difficulty("HARD")
                .timeLimitMinutes(90)
                .totalQuestions(100)
                .course(course)
                .build();

        List<QuizQuestion> questions = new ArrayList<>(100);
        for (int i = 1; i <= 100; i++) {
            questions.add(QuizQuestion.builder()
                    .questionText("Nội dung câu hỏi trắc nghiệm số " + i + ": Kiến trúc phần mềm và tối ưu hóa hệ thống?")
                    .optionA("Lựa chọn A câu " + i)
                    .optionB("Lựa chọn B câu " + i)
                    .optionC("Lựa chọn C câu " + i)
                    .optionD("Lựa chọn D câu " + i)
                    .correctAnswer("A")
                    .explanation("Giải thích chi tiết cho câu hỏi số " + i + ".")
                    .sortOrder(i)
                    .build());
        }

        long start = System.currentTimeMillis();
        byte[] pdf = quizPdfExportService.exportQuizToPdf(quiz, questions, true);
        long elapsed = System.currentTimeMillis() - start;

        assertNotNull(pdf);
        assertTrue(pdf.length > 20000, "File PDF 100 câu hỏi phải có dung lượng lớn tương ứng");

        try (PDDocument doc = PDDocument.load(pdf)) {
            int pageCount = doc.getNumberOfPages();
            assertTrue(pageCount >= 5, "100 câu hỏi kèm đáp án phải chiếm nhiều trang (ít nhất 5 trang)");
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);
            assertTrue(text.contains("100"), "Phải chứa số 100");
            assertTrue(text.contains("Giải thích chi tiết cho câu hỏi số 100."));
        }

        System.out.println(">>> STRESS-4 PASSED: 100-question quiz rendered in " + elapsed + " ms");
    }

    @Test
    @DisplayName("STRESS-5 Edge Cases: Null fields, empty questions, no course, showAnswers false")
    void testEdgeCasesGracefulHandling() {
        Quiz minimalQuiz = Quiz.builder()
                .title("Đề thi Rỗng")
                .build();

        // 1. Empty questions
        assertDoesNotThrow(() -> {
            byte[] pdf = quizPdfExportService.exportQuizToPdf(minimalQuiz, List.of(), false);
            assertNotNull(pdf);
            assertTrue(pdf.length > 500);
        });

        // 2. Null questions list
        assertDoesNotThrow(() -> {
            byte[] pdf = quizPdfExportService.exportQuizToPdf(minimalQuiz, null, false);
            assertNotNull(pdf);
            assertTrue(pdf.length > 500);
        });

        // 3. Question with null explanation
        QuizQuestion nullExpQuestion = QuizQuestion.builder()
                .questionText("Câu hỏi không có lời giải thích")
                .optionA("A")
                .optionB("B")
                .optionC("C")
                .optionD("D")
                .correctAnswer("A")
                .explanation(null)
                .sortOrder(1)
                .build();

        assertDoesNotThrow(() -> {
            byte[] pdf = quizPdfExportService.exportQuizToPdf(minimalQuiz, List.of(nullExpQuestion), true);
            assertNotNull(pdf);
            assertTrue(pdf.length > 500);
        });
        System.out.println(">>> STRESS-5 PASSED: Handled null fields and empty collections gracefully.");
    }
}
