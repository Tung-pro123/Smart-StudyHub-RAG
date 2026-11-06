package com.group2.assignment.payment.entity;

import com.group2.assignment.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions", indexes = {
        @Index(name = "idx_txn_ref", columnList = "txnRef", unique = true),
        @Index(name = "idx_payment_status", columnList = "status"),
        @Index(name = "idx_payment_created_at", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String txnRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 50)
    private String username;

    @Column(nullable = false, length = 30)
    private String targetTier; // "PRO", "SQUAD"

    @Column(nullable = false)
    private Long amount; // 49000, 129000 (VNĐ)

    @Column(length = 50)
    private String bankCode; // "NCB", "VISA"

    @Column(length = 100)
    private String vnpayTransactionNo;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "PENDING"; // "PENDING", "SUCCESS", "FAILED"

    @Column(length = 20)
    private String responseCode; // "00" for success

    @Column(length = 255)
    private String orderInfo;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

