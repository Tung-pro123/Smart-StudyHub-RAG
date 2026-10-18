package com.tung.assignment.quiz.entity;

import com.tung.assignment.core.entity.Course;
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
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
