package com.group2.assignment.planner.repository;

import com.group2.assignment.planner.entity.StudyPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyPlanRepository extends JpaRepository<StudyPlan, Long> {
    List<StudyPlan> findAllByCourseIdOrderByCreatedAtDesc(Long courseId);
    List<StudyPlan> findAllByOrderByCreatedAtDesc();
    Optional<StudyPlan> findFirstByCourseIdOrderByCreatedAtDesc(Long courseId);
    void deleteAllByCourseId(Long courseId);
}
