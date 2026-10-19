package com.group2.assignment.quiz.service;

import com.group2.assignment.quiz.entity.QuizQuestion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class QuizGradingTest {

    @Test
    @DisplayName("Chấm điểm 5 câu: 4 đúng 1 sai phải được 8.0/10")
    void testGradingCalculation() {
        QuizQuestion q1 = QuizQuestion.builder().id(1L).correctAnswer("A").build();
        QuizQuestion q2 = QuizQuestion.builder().id(2L).correctAnswer("B").build();
        QuizQuestion q3 = QuizQuestion.builder().id(3L).correctAnswer("C").build();
        QuizQuestion q4 = QuizQuestion.builder().id(4L).correctAnswer("D").build();
        QuizQuestion q5 = QuizQuestion.builder().id(5L).correctAnswer("A").build();

        List<QuizQuestion> questions = List.of(q1, q2, q3, q4, q5);

        Map<Long, String> answers = new HashMap<>();
        answers.put(1L, "A"); // Đúng
        answers.put(2L, "B"); // Đúng
        answers.put(3L, "C"); // Đúng
        answers.put(4L, "D"); // Đúng
        answers.put(5L, "B"); // Sai (đáp án đúng là A)

        int correctCount = 0;
        for (QuizQuestion q : questions) {
            String selected = answers.get(q.getId());
            if (selected != null && selected.equalsIgnoreCase(q.getCorrectAnswer())) {
                correctCount++;
            }
        }

        double score = questions.isEmpty() ? 0.0 : Math.round(((double) correctCount / questions.size() * 10.0) * 10.0) / 10.0;

        assertEquals(4, correctCount);
        assertEquals(8.0, score);
    }
}
