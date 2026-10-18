package com.group2.assignment.quiz.repository;

import com.group2.assignment.quiz.entity.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    List<QuizQuestion> findAllByQuizIdOrderBySortOrderAsc(Long quizId);
    long countByQuizId(Long quizId);
    void deleteAllByQuizId(Long quizId);
}
