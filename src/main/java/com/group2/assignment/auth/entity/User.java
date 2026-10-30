package com.group2.assignment.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_username", columnList = "username", unique = true),
        @Index(name = "idx_users_email", columnList = "email", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password; // BCrypt hash

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(length = 30)
    @Builder.Default
    private String role = "ROLE_STUDENT"; // ROLE_STUDENT, ROLE_ADMIN

    @Column(length = 20)
    @Builder.Default
    private String subscriptionTier = "FREE";

    @Builder.Default
    private Integer dailyRagCount = 0;
    private LocalDate lastRagResetDate;
    private LocalDateTime tierExpiresAt;

    @Column(length = 20)
    @Builder.Default
    private String provider = "local"; // "local", "google"

    @Column(length = 255)
    @Builder.Default
    private String avatar = "https://api.dicebear.com/7.x/bottts/svg?seed=student";

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
        if (this.role == null || this.role.isBlank()) {
            this.role = "ROLE_STUDENT";
        }
        if (this.provider == null || this.provider.isBlank()) {
            this.provider = "local";
        }
        if (this.avatar == null || this.avatar.isBlank()) {
            this.avatar = "https://api.dicebear.com/7.x/bottts/svg?seed=" + (this.username != null ? this.username : "student");
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
