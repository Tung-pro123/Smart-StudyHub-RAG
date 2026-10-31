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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class QuizPdfExportServiceTest {

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
                .name("Kỹ Thuật Phần Mềm & Trí Tuệ Nhân Tạo")
                .code("SE404_" + System.currentTimeMillis())
                .description("Khoá học kiểm thử và phát triển hệ thống AI")
                .build());
    }

    @Test
    @DisplayName("PDF generation with showAnswers = false: returns valid non-empty byte array, starts with %PDF- header, no answer key")
    void testExportPdfWithoutAnswers() throws IOException {
        Quiz quiz = Quiz.builder()
                .title("Đề thi Trắc nghiệm Lập trình Java Cơ bản")
                .chapterTopic("Chương 1: Cú pháp cơ bản")
                .difficulty("EASY")
                .timeLimitMinutes(15)
                .totalQuestions(1)
                .course(sampleCourse)
                .build();

        QuizQuestion q1 = QuizQuestion.builder()
                .quiz(quiz)
                .questionText("Từ khoá nào dùng để kế thừa một lớp trong Java?")
                .optionA("extends")
                .optionB("implements")
                .optionC("inherits")
                .optionD("super")
                .correctAnswer("A")
                .explanation("Từ khóa extends dùng để kế thừa một class trong Java.")
                .sortOrder(1)
                .build();

        byte[] pdfBytes = quizPdfExportService.exportQuizToPdf(quiz, List.of(q1), false);

        assertNotNull(pdfBytes, "PDF byte array không được null");
        assertTrue(pdfBytes.length > 500, "File PDF phải có dung lượng lớn hơn 500 bytes");

        // Verify %PDF- magic header
        assertEquals('%', (char) pdfBytes[0]);
        assertEquals('P', (char) pdfBytes[1]);
        assertEquals('D', (char) pdfBytes[2]);
        assertEquals('F', (char) pdfBytes[3]);
        assertEquals('-', (char) pdfBytes[4]);

        // Verify text content
        try (PDDocument document = PDDocument.load(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            assertTrue(text.contains("Đề thi Trắc nghiệm Lập trình Java Cơ bản"), "Phải chứa tiêu đề đề thi");
            assertTrue(text.contains("Kỹ Thuật Phần Mềm & Trí Tuệ Nhân Tạo"), "Phải chứa tên môn học");
            assertTrue(text.contains("Từ khoá nào dùng để kế thừa một lớp trong Java?"), "Phải chứa câu hỏi");
            assertTrue(text.contains("extends"), "Phải chứa lựa chọn A");
            assertFalse(text.contains("ĐÁP ÁN VÀ LỜI GIẢI CHI TIẾT"), "Không được hiển thị phần đáp án khi showAnswers = false");
        }
    }

    @Test
    @DisplayName("PDF generation with showAnswers = true: returns valid non-empty byte array, starts with %PDF- header, includes answer key and explanation")
    void testExportPdfWithAnswers() throws IOException {
        Quiz quiz = Quiz.builder()
                .title("Đề ôn tập Hệ Quản Trị CSDL Nâng Cao")
                .chapterTopic("Chương 3: Tối ưu hoá truy vấn")
                .difficulty("HARD")
                .timeLimitMinutes(30)
                .totalQuestions(1)
                .course(sampleCourse)
                .build();

        QuizQuestion q1 = QuizQuestion.builder()
                .quiz(quiz)
                .questionText("Mục đích chính của việc tạo Index trong CSDL là gì?")
                .optionA("Tăng tốc độ truy vấn SELECT")
                .optionB("Giảm dung lượng ổ cứng")
                .optionC("Tăng tốc độ INSERT")
                .optionD("Mã hóa mật khẩu người dùng")
                .correctAnswer("A")
                .explanation("Index giúp hệ quản trị CSDL tìm kiếm dữ liệu nhanh hơn mà không cần quét toàn bộ bảng (Table Scan).")
                .sortOrder(1)
                .build();

        byte[] pdfBytes = quizPdfExportService.exportQuizToPdf(quiz, List.of(q1), true);

        assertNotNull(pdfBytes, "PDF byte array không được null");
        assertTrue(pdfBytes.length > 500, "File PDF phải có dung lượng lớn hơn 500 bytes");

        // Verify %PDF- magic header
        assertEquals('%', (char) pdfBytes[0]);
        assertEquals('P', (char) pdfBytes[1]);
        assertEquals('D', (char) pdfBytes[2]);
        assertEquals('F', (char) pdfBytes[3]);
        assertEquals('-', (char) pdfBytes[4]);

        // Verify text content
        try (PDDocument document = PDDocument.load(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            assertTrue(text.contains("Đề ôn tập Hệ Quản Trị CSDL Nâng Cao"), "Phải chứa tiêu đề đề thi");
            assertTrue(text.contains("ĐÁP ÁN VÀ LỜI GIẢI CHI TIẾT"), "Phải chứa tiêu đề phần đáp án khi showAnswers = true");
            assertTrue(text.contains("Đáp án") && text.contains("A"), "Phải hiển thị đáp án đúng A");
            assertTrue(text.contains("Index giúp hệ quản trị CSDL tìm kiếm dữ liệu nhanh hơn"), "Phải hiển thị lời giải chi tiết");
        }
    }

    @Test
    @DisplayName("Vietnamese diacritic rendering: verify title 'Đề thi Kiểm thử AI Quiz & Squad Leaderboard' and Vietnamese characters are preserved")
    void testVietnameseDiacriticRendering() throws IOException {
        String testTitle = "Đề thi Kiểm thử AI Quiz & Squad Leaderboard";
        Quiz quiz = Quiz.builder()
                .title(testTitle)
                .chapterTopic("Kiểm thử Tích Hợp & Font Chữ Tiếng Việt")
                .difficulty("MEDIUM")
                .timeLimitMinutes(20)
                .totalQuestions(1)
                .course(sampleCourse)
                .build();

        QuizQuestion q1 = QuizQuestion.builder()
                .quiz(quiz)
                .questionText("Kiến trúc phần mềm hướng dịch vụ (SOA) có đặc điểm nào dưới đây?")
                .optionA("Các dịch vụ độc lập và giao tiếp qua mạng")
                .optionB("Mọi module gộp chung vào một monolithic")
                .optionC("Không thể triển khai trên môi trường Docker")
                .optionD("Chỉ hỗ trợ hệ điều hành Windows")
                .correctAnswer("A")
                .explanation("SOA phân tách hệ thống thành các dịch vụ độc lập, giao tiếp với nhau qua giao thức mạng.")
                .sortOrder(1)
                .build();

        byte[] pdfBytes = quizPdfExportService.exportQuizToPdf(quiz, List.of(q1), true);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 500);

        try (PDDocument document = PDDocument.load(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            // Kiểm tra chính xác các từ chứa dấu Tiếng Việt phức tạp (đ, ề, ểm, ử, ệ, vv.)
            assertTrue(text.contains(testTitle), "Text trích xuất từ PDF phải chứa tiêu đề tiếng Việt chuẩn: " + testTitle);
            assertTrue(text.contains("Kiến trúc phần mềm hướng dịch vụ"), "Text phải hiển thị đúng dấu tiếng Việt");
            assertTrue(text.contains("Các dịch vụ độc lập và giao tiếp qua mạng"), "Lựa chọn trắc nghiệm phải giữ nguyên dấu tiếng Việt");
            assertTrue(text.contains("ĐÁP ÁN VÀ LỜI GIẢI CHI TIẾT"), "Tiêu đề đáp án phải giữ nguyên dấu");
        }
    }

    @Test
    @DisplayName("generateQuizPdf overload: persists quiz and questions in DB, generates valid PDF with questions loaded from repository")
    void testGenerateQuizPdfFromPersistedQuiz() throws IOException {
        Quiz quiz = quizRepository.save(Quiz.builder()
                .title("Đề thi trắc nghiệm lưu trong DB")
                .chapterTopic("Chương 2: JPA & Hibernate")
                .difficulty("MEDIUM")
                .timeLimitMinutes(25)
                .totalQuestions(2)
                .course(sampleCourse)
                .build());

        QuizQuestion q1 = quizQuestionRepository.save(QuizQuestion.builder()
                .quiz(quiz)
                .questionText("Annotation nào đánh dấu một Entity trong JPA?")
                .optionA("@Entity")
                .optionB("@Table")
                .optionC("@Document")
                .optionD("@Record")
                .correctAnswer("A")
                .explanation("@Entity đánh dấu class là một thực thể JPA quản lý.")
                .sortOrder(1)
                .build());

        QuizQuestion q2 = quizQuestionRepository.save(QuizQuestion.builder()
                .quiz(quiz)
                .questionText("FetchType.LAZY có ý nghĩa gì?")
                .optionA("Chỉ nạp quan hệ khi được truy cập lần đầu")
                .optionB("Luôn nạp quan hệ ngay lập tức")
                .optionC("Không bao giờ nạp dữ liệu")
                .optionD("Xóa cascade các bản ghi con")
                .correctAnswer("A")
                .explanation("LAZY trì hoãn truy vấn lấy thực thể liên kết cho tới khi getter được gọi.")
                .sortOrder(2)
                .build());

        // Test overload generateQuizPdf(quiz, false)
        byte[] pdfWithoutAnswers = quizPdfExportService.generateQuizPdf(quiz, false);
        assertNotNull(pdfWithoutAnswers);
        assertTrue(pdfWithoutAnswers.length > 500);

        try (PDDocument document = PDDocument.load(pdfWithoutAnswers)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            assertTrue(text.contains("Đề thi trắc nghiệm lưu trong DB"));
            assertTrue(text.contains("Annotation nào đánh dấu một Entity trong JPA?"));
            assertTrue(text.contains("FetchType.LAZY có ý nghĩa gì?"));
            assertFalse(text.contains("ĐÁP ÁN VÀ LỜI GIẢI CHI TIẾT"));
        }

        // Test overload generateQuizPdf(quiz, true)
        byte[] pdfWithAnswers = quizPdfExportService.generateQuizPdf(quiz, true);
        assertNotNull(pdfWithAnswers);
        try (PDDocument document = PDDocument.load(pdfWithAnswers)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            assertTrue(text.contains("Đề thi trắc nghiệm lưu trong DB"));
            assertTrue(text.contains("ĐÁP ÁN VÀ LỜI GIẢI CHI TIẾT"));
            assertTrue(text.contains("@Entity đánh dấu class là một thực thể JPA quản lý."));
        }
    }
}
