package com.tung.assignment.planner.entity;

import com.tung.assignment.core.entity.Course;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "study_plans", indexes = {
        @Index(name = "idx_study_plans_course", columnList = "course_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ToString.Exclude
    private Course course;

    @Column(nullable = false, length = 255)
    private String title; // Ví dụ: Kế hoạch ôn thi HSF302 4 tuần

    @Builder.Default
    private Integer hoursPerWeek = 6; // Số giờ học mỗi tuần

    @Column(length = 100)
    private String selectedDays; // Ví dụ: "TUE,THU,SAT"

    private LocalDate examDate; // Ngày thi cuối kỳ

    @Column(length = 500)
    private String goal; // Mục tiêu

    @Column(length = 30)
    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, COMPLETED, ARCHIVED

    @Builder.Default
    private Integer progressPercent = 0; // % tiến độ hoàn thành (0 - 100)

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
        if (this.hoursPerWeek == null) this.hoursPerWeek = 6;
        if (this.status == null) this.status = "ACTIVE";
        if (this.progressPercent == null) this.progressPercent = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
