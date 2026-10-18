package com.group2.assignment.core.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "courses", indexes = {
        @Index(name = "idx_courses_code", columnList = "code", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code; // Mã môn học: e.g. HSF302, PRJ301, MAS291

    @Column(nullable = false, length = 200)
    private String name; // Tên môn học: e.g. JPA and Spring Framework

    @Column(length = 1000)
    private String description;

    @Column(length = 100)
    @Builder.Default
    private String color = "from-indigo-600 to-purple-600"; // Class gradient Tailwind

    @Column(length = 50)
    @Builder.Default
    private String icon = "fa-book-open"; // FontAwesome icon

    @Column(length = 50)
    @Builder.Default
    private String semester = "Fall 2026"; // Học kỳ

    @Builder.Default
    private Integer credits = 3; // Số tín chỉ

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
        if (this.color == null || this.color.isBlank()) {
            this.color = "from-indigo-600 to-purple-600";
        }
        if (this.icon == null || this.icon.isBlank()) {
            this.icon = "fa-book-open";
        }
        if (this.semester == null || this.semester.isBlank()) {
            this.semester = "Fall 2026";
        }
        if (this.credits == null) {
            this.credits = 3;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
