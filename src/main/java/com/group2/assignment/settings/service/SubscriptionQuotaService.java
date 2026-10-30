package com.group2.assignment.settings.service;

import com.group2.assignment.auth.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class SubscriptionQuotaService {
    public static final int MAX_RAG_DAILY_FREE = 50;
    private final EntityManager entityManager;

    private User lockedUser(Long id) {
        entityManager.flush();
        User user = entityManager.find(User.class, id);
        if (user == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản.");
        // The MVC session refresh may already have loaded a stale managed entity before this lock.
        entityManager.refresh(user, LockModeType.PESSIMISTIC_WRITE);
        return user;
    }

    private void resetIfNewDay(User user) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        if (user.getLastRagResetDate() == null || today.isAfter(user.getLastRagResetDate())) {
            user.setDailyRagCount(0);
            user.setLastRagResetDate(today);
        }
        if (user.getDailyRagCount() == null) user.setDailyRagCount(0);
    }

    public boolean hasUnlimitedRag(User user) {
        return "ROLE_ADMIN".equals(user.getRole()) || "PRO".equals(user.getSubscriptionTier())
                || "CAMPUS".equals(user.getSubscriptionTier());
    }

    // shortcut: preflight and recording are separate; add reservations to cap in-flight AI calls in RAG.
    @Transactional
    public boolean canQueryRag(Long userId) {
        User user = lockedUser(userId);
        resetIfNewDay(user);
        return hasUnlimitedRag(user) || user.getDailyRagCount() < MAX_RAG_DAILY_FREE;
    }

    /** Call once after a successful answer; the lock keeps the persisted counter within quota. */
    @Transactional
    public boolean recordSuccessfulRagQuery(Long userId) {
        User user = lockedUser(userId);
        resetIfNewDay(user);
        if (!hasUnlimitedRag(user) && user.getDailyRagCount() >= MAX_RAG_DAILY_FREE) return false;
        user.setDailyRagCount(user.getDailyRagCount() + 1);
        return true;
    }

    @Transactional
    public void changeTier(Long userId, String tier) {
        if (!"FREE".equals(tier) && !"PRO".equals(tier)) throw new IllegalArgumentException("Gói cước không hợp lệ.");
        User user = lockedUser(userId);
        if (!"ROLE_STUDENT".equals(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ được đổi gói cho sinh viên.");
        }
        user.setSubscriptionTier(tier);
        user.setTierExpiresAt(null);
    }
}
