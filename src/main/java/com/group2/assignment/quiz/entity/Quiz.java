package com.group2.assignment.quiz.entity;

import com.group2.assignment.auth.entity.User;
import com.group2.assignment.core.entity.Course;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "quizzes", indexes = {
        @Index(name = "idx_quizzes_course", columnList = "course_id"),
        @Index(name = "idx_quizzes_difficulty", columnList = "difficulty")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ToString.Exclude
    private Course course;

    /** Người tạo/sinh đề thi (null = hệ thống/seeder) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    @ToString.Exclude
    private User createdBy;

    @Column(nullable = false, length = 255)
    private String title; // Tên đề thi

    @Column(length = 255)
    private String chapterTopic; // Tên chương/bài

    @Column(length = 20)
    @Builder.Default
    private String difficulty = "MEDIUM"; // EASY, MEDIUM, HARD

    @Builder.Default
    private Integer totalQuestions = 5;

    @Builder.Default
    private Integer timeLimitMinutes = 15;

    /**
     * Cờ đánh dấu đề thi dùng chung cho cả Squad hay là Đề thi cá nhân (Solo):
     * - true (hoặc null): Mọi thành viên Squad đều thấy, cùng làm và tính điểm Squad Leaderboard.
     * - false: Đề thi riêng tư (Solo). Chỉ tác giả (createdBy) thấy và làm bài.
     */
    @Column(name = "squad_shared")
    @Builder.Default
    private Boolean squadShared = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
        if (this.difficulty == null) this.difficulty = "MEDIUM";
        if (this.totalQuestions == null) this.totalQuestions = 5;
        if (this.timeLimitMinutes == null) this.timeLimitMinutes = 15;
        if (this.squadShared == null) this.squadShared = true;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /** Kiểm tra xem đề thi có phải là đề riêng tư (Solo) hay không */
    public boolean isSolo() {
        return Boolean.FALSE.equals(this.squadShared);
    }

    /** Kiểm tra xem người dùng có quyền nhìn thấy và làm đề thi này hay không */
    public boolean isAccessibleBy(User user) {
        if (user == null) return false;
        if (user.isAdmin()) return true;
        // Nếu không phải đề thi Solo (dùng chung trong môn): ai có quyền truy cập môn cũng được xem
        if (!isSolo()) return true;
        // Nếu là đề thi Solo: chỉ chính tác giả tạo đề mới được xem và làm bài
        return this.createdBy != null && user.getId() != null && user.getId().equals(this.createdBy.getId());
    }

    /** Kiểm tra xem người dùng có quyền xóa đề thi này hay không (RBAC) */
    public boolean canDelete(User user) {
        if (user == null) return false;
        if (user.isAdmin()) return true;
        if (this.course != null && Boolean.TRUE.equals(this.course.getSquadShared())) {
            if (isSolo()) {
                // Đề thi Solo trong môn nhóm: tác giả tự xóa được đề của mình
                return this.createdBy != null && user.getId() != null && user.getId().equals(this.createdBy.getId());
            }
            // Đề thi dùng chung của nhóm: chỉ Trưởng nhóm hoặc Admin
            return user.isSquadLeader();
        }
        // Môn học cá nhân: chủ sở hữu môn học
        return this.course == null || this.course.getUser() == null ||
                (user.getId() != null && user.getId().equals(this.course.getUser().getId()));
    }
}
