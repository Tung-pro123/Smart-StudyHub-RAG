package com.group2.assignment.quiz.repository;

import com.group2.assignment.quiz.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findAllByCourseIdOrderByCreatedAtDesc(Long courseId);
    long countByCourseId(Long courseId);
    void deleteAllByCourseId(Long courseId);
}
