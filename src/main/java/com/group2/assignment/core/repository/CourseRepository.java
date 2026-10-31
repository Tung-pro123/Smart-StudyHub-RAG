package com.group2.assignment.core.repository;

import com.group2.assignment.core.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findTop5ByOrderByCreatedAtDescIdDesc();

    Optional<Course> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Course c WHERE LOWER(c.code) = LOWER(:code) AND c.user.id = :userId")
    boolean existsByCodeIgnoreCaseAndUserId(@Param("code") String code, @Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Course c WHERE LOWER(c.code) = LOWER(:code) AND c.user.id = :userId AND c.id <> :id")
    boolean existsByCodeIgnoreCaseAndUserIdAndIdNot(@Param("code") String code, @Param("userId") Long userId, @Param("id") Long id);

    @Query("SELECT COUNT(c) FROM Course c WHERE c.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM Course c WHERE c.user.id = :userId")
    List<Course> findAllByUserId(@Param("userId") Long userId);

    List<Course> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(String name, String code);

    List<Course> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCaseOrSemesterContainingIgnoreCase(String name, String code, String semester);

    List<Course> findBySemesterIgnoreCase(String semester);

    List<Course> findBySemesterIgnoreCaseAndNameContainingIgnoreCase(String semester, String name);

    @Query("SELECT c FROM Course c WHERE (c.user.id = :userId OR (:includeShared = true AND c.user IS NULL)) " +
           "AND (:semester IS NULL OR :semester = 'ALL' OR LOWER(c.semester) = LOWER(:semester)) " +
           "AND (:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Course> searchCoursesByUser(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("semester") String semester,
            @Param("includeShared") boolean includeShared);

    @Query("SELECT c FROM Course c WHERE (:semester IS NULL OR :semester = 'ALL' OR LOWER(c.semester) = LOWER(:semester)) " +
           "AND (:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Course> searchAll(
            @Param("keyword") String keyword,
            @Param("semester") String semester);
}
