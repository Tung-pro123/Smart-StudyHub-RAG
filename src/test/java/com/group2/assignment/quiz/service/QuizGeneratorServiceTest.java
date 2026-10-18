package com.group2.assignment.quiz.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group2.assignment.quiz.entity.QuizQuestion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class QuizGeneratorServiceTest {

    @Test
    @DisplayName("Smart Mock Fallback trích xuất thành công câu hỏi và 4 đáp án phân biệt từ văn bản")
    void testSmartMockFallbackGeneration() {
        QuizGeneratorService service = new QuizGeneratorService(new ObjectMapper());

        String context = "Spring Framework là một nền tảng mã nguồn mở mạnh mẽ cho Java doanh nghiệp.\n"
                + "Inversion of Control (IoC) là nguyên lý cốt lõi giúp đảo ngược quyền điều khiển tạo đối tượng.\n"
                + "Dependency Injection (DI) là một dạng cụ thể của IoC cho phép nạp các phụ thuộc từ bên ngoài.\n"
                + "Spring Boot giúp tự động cấu hình các ứng dụng Spring nhanh chóng và giảm boilerplate code.\n"
                + "Spring Data JPA cung cấp các abstraction mạnh mẽ để thao tác với cơ sở dữ liệu qua Hibernate.";

        List<QuizQuestion> questions = service.generateMockQuestions(context, 3, "MEDIUM");

        assertEquals(3, questions.size());
        for (QuizQuestion q : questions) {
            assertNotNull(q.getQuestionText());
            assertNotNull(q.getOptionA());
            assertNotNull(q.getOptionB());
            assertNotNull(q.getOptionC());
            assertNotNull(q.getOptionD());
            assertEquals("A", q.getCorrectAnswer());
            assertNotNull(q.getExplanation());
        }
    }
}
