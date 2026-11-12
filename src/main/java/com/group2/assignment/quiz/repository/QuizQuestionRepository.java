package com.group2.assignment.quiz.repository;

import com.group2.assignment.quiz.entity.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    List<QuizQuestion> findAllByQuizIdOrderBySortOrderAsc(Long quizId);

    long countByQuizId(Long quizId);

    void deleteAllByQuizId(Long quizId);
}
