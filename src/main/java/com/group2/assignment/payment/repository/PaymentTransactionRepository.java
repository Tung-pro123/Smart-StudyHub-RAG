package com.group2.assignment.payment.repository;

import com.group2.assignment.payment.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    Optional<PaymentTransaction> findByTxnRef(String txnRef);

    List<PaymentTransaction> findTop20ByOrderByCreatedAtDesc();

    List<PaymentTransaction> findAllByOrderByCreatedAtDesc();

    List<PaymentTransaction> findByUsernameOrderByCreatedAtDesc(String username);

    List<PaymentTransaction> findTop10ByUsernameOrderByCreatedAtDesc(String username);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentTransaction p WHERE p.status = 'SUCCESS'")
    Long getTotalRevenue();

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentTransaction p WHERE p.status = 'SUCCESS' AND p.targetTier = :tier")
    Long getRevenueByTier(@Param("tier") String tier);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentTransaction p WHERE p.status = 'SUCCESS' AND p.createdAt >= :since")
    Long getRevenueSince(@Param("since") LocalDateTime since);

    Long countByStatus(String status);
}
