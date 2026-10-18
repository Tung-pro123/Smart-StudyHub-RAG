package com.tung.assignment.quiz.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_attempts", indexes = {
        @Index(name = "idx_quiz_attempts_quiz", columnList = "quiz_id"),
        @Index(name = "idx_quiz_attempts_completed_at", columnList = "completed_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ToString.Exclude
    private Quiz quiz;

    @Column(length = 100)
    @Builder.Default
    private String studentName = "Sinh viên";

    @Column(nullable = false)
    private Double totalScore; // Điểm số thang 10

    @Column(nullable = false)
    private Integer correctCount; // Số câu đúng

    @Column(nullable = false)
    private Integer totalCount; // Tổng số câu

    @Lob
    @Column(columnDefinition = "CLOB")
    private String userAnswers; // Chuỗi JSON lưu vết câu trả lời: {"questionId":"A", ...}

    @Builder.Default
    private Integer timeSpentSeconds = 0;

    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        if (this.completedAt == null) {
            this.completedAt = LocalDateTime.now();
        }
        if (this.studentName == null || this.studentName.isBlank()) {
            this.studentName = "Sinh viên";
        }
        if (this.timeSpentSeconds == null) {
            this.timeSpentSeconds = 0;
        }
    }
}
