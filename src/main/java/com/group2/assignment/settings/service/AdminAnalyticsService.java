package com.group2.assignment.settings.service;

import com.group2.assignment.auth.repository.UserRepository;
import com.group2.assignment.auth.entity.User;
import com.group2.assignment.core.entity.Course;
import java.util.List;
import com.group2.assignment.core.repository.CourseRepository;
import com.group2.assignment.core.repository.ChunkRepository;
import com.group2.assignment.settings.dto.AdminStatsDto;
import com.group2.assignment.settings.dto.PingResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class AdminAnalyticsService {
    private final UserRepository users;
    private final CourseRepository courses;
    private final ChunkRepository chunks;
    private final SystemAiKeyService systemKey;

    public List<User> getRecentUsers() {
        return users.findTop5ByOrderByCreatedAtDescIdDesc();
    }

    public List<Course> getRecentCourses() {
        return courses.findTop5ByOrderByCreatedAtDescIdDesc();
    }

    @Transactional(readOnly = true)
    public AdminStatsDto getStats() {
        PingResponseDto ping = systemKey.getLastPing();
        return new AdminStatsDto(users.count(), users.countByTier("FREE"), users.countByTier("PRO"), users.countByTier("SQUAD"),
                courses.count(), chunks.count(), users.countTodayRagQueries(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"))),
                ping != null && ping.success() ? ping.latencyMs() : null);
    }
}
