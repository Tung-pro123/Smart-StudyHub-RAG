package com.tung.assignment.quiz.repository;

import com.tung.assignment.quiz.entity.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    List<QuizQuestion> findAllByQuizIdOrderBySortOrderAsc(Long quizId);
    long countByQuizId(Long quizId);
    void deleteAllByQuizId(Long quizId);
}
