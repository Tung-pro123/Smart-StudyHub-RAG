package com.group2.assignment.quiz.repository;

import com.group2.assignment.quiz.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findAllByQuizIdOrderByCompletedAtDesc(Long quizId);
    long countByQuizId(Long quizId);
    void deleteAllByQuizId(Long quizId);

    @Query("SELECT a FROM QuizAttempt a JOIN a.quiz q WHERE q.course.id = :courseId ORDER BY a.completedAt DESC")
    List<QuizAttempt> findRecentAttemptsByCourseId(@Param("courseId") Long courseId);
}
