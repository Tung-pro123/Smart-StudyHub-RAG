package com.group2.assignment.planner.repository;

import com.group2.assignment.planner.entity.StudyPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyPlanRepository extends JpaRepository<StudyPlan, Long> {
    List<StudyPlan> findAllByCourseIdOrderByCreatedAtDesc(Long courseId);
    List<StudyPlan> findAllByOrderByCreatedAtDesc();

    @Query("SELECT p FROM StudyPlan p WHERE p.course.user.id = :userId ORDER BY p.createdAt DESC")
    List<StudyPlan> findAllByCourseUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    @Query("SELECT p FROM StudyPlan p WHERE p.course.id = :courseId AND p.course.user.id = :userId ORDER BY p.createdAt DESC")
    List<StudyPlan> findAllByCourseIdAndCourseUserIdOrderByCreatedAtDesc(@Param("courseId") Long courseId,
                                                                         @Param("userId") Long userId);

    @Query("SELECT p FROM StudyPlan p WHERE p.id = :id AND p.course.user.id = :userId")
    Optional<StudyPlan> findByIdAndCourseUserId(@Param("id") Long id, @Param("userId") Long userId);
    Optional<StudyPlan> findFirstByCourseIdOrderByCreatedAtDesc(Long courseId);
    void deleteAllByCourseId(Long courseId);
}
