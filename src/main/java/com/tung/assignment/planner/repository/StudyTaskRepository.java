package com.tung.assignment.planner.repository;

import com.tung.assignment.planner.entity.StudyTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyTaskRepository extends JpaRepository<StudyTask, Long> {
    List<StudyTask> findAllByPlanIdOrderByWeekNumberAscSortOrderAsc(Long planId);
    List<StudyTask> findAllByPlanIdAndWeekNumberOrderBySortOrderAsc(Long planId, Integer weekNumber);
    long countByPlanId(Long planId);
    long countByPlanIdAndIsCompletedTrue(Long planId);
    void deleteAllByPlanId(Long planId);
}
