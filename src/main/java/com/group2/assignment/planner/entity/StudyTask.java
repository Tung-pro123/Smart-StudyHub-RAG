package com.group2.assignment.planner.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "study_tasks", indexes = {
        @Index(name = "idx_study_tasks_plan", columnList = "plan_id"),
        @Index(name = "idx_study_tasks_completed", columnList = "is_completed")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ToString.Exclude
    private StudyPlan plan;

    @Column(nullable = false)
    private Integer weekNumber; // Tuần 1, Tuần 2...

    @Column(nullable = false, length = 30)
    private String dayOfWeek; // Thứ 3, Thứ 5...

    @Column(nullable = false, length = 255)
    private String taskTitle; // Tiêu đề nhiệm vụ

    @Column(columnDefinition = "TEXT")
    private String taskDescription;

    @Builder.Default
    private Integer durationMinutes = 120; // Thời gian (phút)

    @Builder.Default
    private Boolean isCompleted = false;

    private LocalDateTime completedAt;

    @Builder.Default
    private Integer sortOrder = 0;

    @PrePersist
    protected void onCreate() {
        if (this.isCompleted == null) this.isCompleted = false;
        if (this.durationMinutes == null) this.durationMinutes = 120;
        if (this.sortOrder == null) this.sortOrder = 0;
    }
}
