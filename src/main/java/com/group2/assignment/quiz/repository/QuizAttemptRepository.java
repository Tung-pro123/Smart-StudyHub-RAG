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

    @Query("SELECT u.id, u.username, u.fullName, u.avatar, MAX(a.totalScore), COUNT(a.id) " +
           "FROM QuizAttempt a JOIN a.user u " +
           "WHERE (:courseId IS NULL OR a.quiz.course.id = :courseId) " +
           "AND (u.username = :leaderUsername OR u.squadLeaderUsername = :leaderUsername " +
           "     OR (u.username = :memberUsername AND u.squadLeaderUsername IS NOT NULL)) " +
           "GROUP BY u.id, u.username, u.fullName, u.avatar " +
           "ORDER BY MAX(a.totalScore) DESC, COUNT(a.id) DESC, u.username ASC")
    List<Object[]> findSquadLeaderboardRaw(
            @Param("leaderUsername") String leaderUsername,
            @Param("memberUsername") String memberUsername,
            @Param("courseId") Long courseId);

    default List<Object[]> findSquadLeaderboardRaw(String leaderUsername, Long courseId) {
        return findSquadLeaderboardRaw(leaderUsername, leaderUsername, courseId);
    }
}

