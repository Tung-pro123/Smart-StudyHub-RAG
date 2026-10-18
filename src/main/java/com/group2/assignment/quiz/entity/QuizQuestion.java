package com.group2.assignment.quiz.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "quiz_questions", indexes = {
        @Index(name = "idx_quiz_questions_quiz", columnList = "quiz_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ToString.Exclude
    private Quiz quiz;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText; // Nội dung câu hỏi trắc nghiệm

    @Column(nullable = false, columnDefinition = "TEXT")
    private String optionA; // Đáp án A

    @Column(nullable = false, columnDefinition = "TEXT")
    private String optionB; // Đáp án B

    @Column(nullable = false, columnDefinition = "TEXT")
    private String optionC; // Đáp án C

    @Column(nullable = false, columnDefinition = "TEXT")
    private String optionD; // Đáp án D

    @Column(nullable = false, length = 5)
    private String correctAnswer; // "A", "B", "C", hoặc "D"

    @Column(columnDefinition = "TEXT")
    private String explanation; // Lời giải thích

    @Builder.Default
    private Integer sortOrder = 1;

    @PrePersist
    protected void onCreate() {
        if (this.sortOrder == null) {
            this.sortOrder = 1;
        }
    }
}
