package com.group2.assignment.auth.repository;

import com.group2.assignment.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByRoleOrderByUsernameAsc(String role);

    List<User> findTop5ByOrderByCreatedAtDescIdDesc();

    @Query("select u from User u where u.role = 'ROLE_STUDENT' " +
            "and (:tier = '' or coalesce(u.subscriptionTier, 'FREE') = :tier) " +
            "and (lower(u.username) like lower(concat('%', :keyword, '%')) " +
            "or lower(u.fullName) like lower(concat('%', :keyword, '%')) " +
            "or lower(u.email) like lower(concat('%', :keyword, '%'))) order by u.username")
    List<User> searchStudents(String keyword, String tier);

    @Query("select count(u) from User u where u.role = 'ROLE_STUDENT' and coalesce(u.subscriptionTier, 'FREE') = :tier")
    long countByTier(String tier);

    @Query("select coalesce(sum(u.dailyRagCount), 0) from User u where u.lastRagResetDate = :today")
    long countTodayRagQueries(LocalDate today);
    Optional<User> findByUsernameIgnoreCase(String username);
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByEmailIgnoreCase(String email);
}
