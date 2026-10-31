package com.group2.assignment.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private String subscriptionTier = "FREE"; // "FREE", "PRO", "CAMPUS", "SQUAD"

    @Column(length = 50)
    private String squadLeaderUsername; // Username của nhóm trưởng sở hữu gói SQUAD

    @Column(name = "squad_leader_username", length = 50)
    private String squadLeaderUsername; // Username nhóm trưởng sở hữu gói Study Squad

    @Builder.Default
    private Integer dailyRagCount = 0; // Đếm số câu chat RAG trong ngày

    private LocalDate lastRagResetDate; // Ngày reset gần nhất

    private LocalDateTime tierExpiresAt; // Ngày hết hạn gói Pro

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
        if (this.subscriptionTier == null || this.subscriptionTier.isBlank()) {
            this.subscriptionTier = "FREE";
        }
        if (this.dailyRagCount == null) {
            this.dailyRagCount = 0;
        }
        if (this.lastRagResetDate == null) {
            this.lastRagResetDate = LocalDate.now();
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

    public boolean isPro() {
        return "ROLE_ADMIN".equalsIgnoreCase(this.role)
                || "PRO".equalsIgnoreCase(this.subscriptionTier)
                || "CAMPUS".equalsIgnoreCase(this.subscriptionTier)
                || "SQUAD".equalsIgnoreCase(this.subscriptionTier);
    }

    public boolean isSquad() {
        return "SQUAD".equalsIgnoreCase(this.subscriptionTier);
    }

    public boolean isSquadLeader() {
        return "SQUAD".equalsIgnoreCase(this.subscriptionTier)
                && (this.squadLeaderUsername == null || this.username.equalsIgnoreCase(this.squadLeaderUsername));
    }

    public boolean isAdmin() {
        return "ROLE_ADMIN".equalsIgnoreCase(this.role);
    }

    public boolean isSquad() {
        return "SQUAD".equalsIgnoreCase(this.subscriptionTier)
                || (this.squadLeaderUsername != null && !this.squadLeaderUsername.isBlank());
    }

    public String getEffectiveSquadLeader() {
        if (!isSquad()) {
            return null;
        }
        if (this.squadLeaderUsername != null && !this.squadLeaderUsername.isBlank()) {
            return this.squadLeaderUsername.trim();
        }
        return this.username;
    }
}
