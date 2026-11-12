package com.group2.assignment.quiz.repository;

import com.group2.assignment.quiz.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findAllByCourseIdOrderByCreatedAtDesc(Long courseId);

    long countByCourseId(Long courseId);

    void deleteAllByCourseId(Long courseId);
}
