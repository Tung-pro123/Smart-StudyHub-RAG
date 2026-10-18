package com.tung.assignment.quiz.repository;

import com.tung.assignment.quiz.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findAllByQuizIdOrderByCompletedAtDesc(Long quizId);
    long countByQuizId(Long quizId);
    void deleteAllByQuizId(Long quizId);
}
