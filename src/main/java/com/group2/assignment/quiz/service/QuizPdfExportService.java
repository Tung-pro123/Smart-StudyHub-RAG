package com.group2.assignment.quiz.service;

import com.group2.assignment.quiz.entity.Quiz;
import com.group2.assignment.quiz.entity.QuizQuestion;
import com.group2.assignment.quiz.repository.QuizQuestionRepository;
import com.openhtmltopdf.extend.FSSupplier;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizPdfExportService {

    private final SpringTemplateEngine templateEngine;
    private final QuizQuestionRepository quizQuestionRepository;

    /**
     * Xuất PDF từ Quiz entity và danh sách câu hỏi.
     *
     * @param quiz        đối tượng Quiz chứa thông tin tiêu đề, thời gian, môn học
     * @param questions   danh sách câu hỏi trắc nghiệm
     * @param showAnswers cờ xác định có in trang đáp án và giải thích hay không
     * @return mảng byte đại diện cho file PDF nhị phân
     */
    public byte[] exportQuizToPdf(Quiz quiz, List<QuizQuestion> questions, boolean showAnswers) {
        try {
            Context context = new Context();
            context.setVariable("quiz", quiz);
            context.setVariable("questions", questions != null ? questions : List.of());
            context.setVariable("showAnswers", showAnswers);

            String htmlContent = templateEngine.process("quiz/pdf-template", context);

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();

                // Cung cấp TrueType font Arial Unicode từ classpath hỗ trợ tiếng Việt đầy đủ
                FSSupplier<InputStream> fontSupplier = () -> {
                    InputStream stream = QuizPdfExportService.class.getResourceAsStream("/fonts/Arial.ttf");
                    if (stream == null) {
                        stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("fonts/Arial.ttf");
                    }
                    return stream;
                };

                // Cấu hình font cho cả Normal (400) và Bold (700) với font subsetting
                builder.useFont(fontSupplier, "Arial", 400, PdfRendererBuilder.FontStyle.NORMAL, true);
                builder.useFont(fontSupplier, "Arial", 700, PdfRendererBuilder.FontStyle.NORMAL, true);

                builder.withHtmlContent(htmlContent, null);
                builder.toStream(outputStream);
                builder.run();

                return outputStream.toByteArray();
            }
        } catch (Exception e) {
            log.error("Lỗi khi kết xuất file PDF đề thi quizId={}", quiz != null ? quiz.getId() : "null", e);
            throw new RuntimeException("Không thể tạo file PDF đề thi: " + e.getMessage(), e);
        }
    }

    /**
     * Xuất PDF từ Quiz entity, tự động tải các câu hỏi thuộc Quiz từ database.
     *
     * @param quiz        đối tượng Quiz đã lưu trong CSDL
     * @param showAnswers cờ xác định có in trang đáp án hay không
     * @return mảng byte PDF
     */
    public byte[] generateQuizPdf(Quiz quiz, boolean showAnswers) {
        List<QuizQuestion> questions = (quiz != null && quiz.getId() != null && quizQuestionRepository != null)
                ? quizQuestionRepository.findAllByQuizIdOrderBySortOrderAsc(quiz.getId())
                : List.of();
        return exportQuizToPdf(quiz, questions, showAnswers);
    }

    /**
     * Overload nhận đầy đủ quiz và danh sách câu hỏi.
     */
    public byte[] generateQuizPdf(Quiz quiz, List<QuizQuestion> questions, boolean showAnswers) {
        return exportQuizToPdf(quiz, questions, showAnswers);
    }

    /**
     * Overload ủy quyền cho generateQuizPdf(quiz, showAnswers).
     */
    public byte[] exportQuizToPdf(Quiz quiz, boolean showAnswers) {
        return generateQuizPdf(quiz, showAnswers);
    }
}
