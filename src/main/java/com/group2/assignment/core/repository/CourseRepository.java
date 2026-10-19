package com.group2.assignment.core.repository;

import com.group2.assignment.core.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    List<Course> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(String name, String code);

    List<Course> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCaseOrSemesterContainingIgnoreCase(String name, String code, String semester);

    List<Course> findBySemesterIgnoreCase(String semester);

    List<Course> findBySemesterIgnoreCaseAndNameContainingIgnoreCase(String semester, String name);
}
