package com.group2.assignment.quiz.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group2.assignment.quiz.entity.QuizQuestion;
import com.group2.assignment.settings.service.SystemAiKeyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class QuizGeneratorServiceTest {

    @Mock
    private SystemAiKeyService systemAiKeyService;

    private QuizGeneratorService quizGeneratorService;

    @BeforeEach
    void setUp() {
        quizGeneratorService = new QuizGeneratorService(new ObjectMapper(), systemAiKeyService);
    }

    @Test
    @DisplayName("Fallback sinh câu hỏi mẫu khi System Key rỗng hoặc không cấu hình")
    void testGenerateQuestionsFallbackWhenNoKey() {
        when(systemAiKeyService.getApiKey()).thenReturn("");

        String context = "Spring Framework là một nền tảng mã nguồn mở mạnh mẽ cho Java doanh nghiệp.\n"
                + "Inversion of Control (IoC) là nguyên lý cốt lõi giúp đảo ngược quyền điều khiển tạo đối tượng.\n"
                + "Dependency Injection (DI) là một dạng cụ thể của IoC cho phép nạp các phụ thuộc từ bên ngoài.\n"
                + "Spring Boot giúp tự động cấu hình các ứng dụng Spring nhanh chóng và giảm boilerplate code.\n"
                + "Spring Data JPA cung cấp các abstraction mạnh mẽ để thao tác với cơ sở dữ liệu qua Hibernate.";

        List<QuizQuestion> questions = quizGeneratorService.generateQuestions(context, 3, "MEDIUM");

        assertThat(questions).isNotNull();
        assertThat(questions).hasSize(3);
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
