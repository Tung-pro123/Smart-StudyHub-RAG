package com.group2.assignment.settings.dto;

public record AdminStatsDto(long totalUsers, long freeUsersCount, long proUsersCount, long squadUsersCount,
                            long totalCourses, long totalChunks, long todayRagQueries,
                            Long geminiPingLatencyMs) {
    public long getTotalStudents() {
        return freeUsersCount + proUsersCount + squadUsersCount;
    }

    public int getPaidConversionRate() {
        return getTotalStudents() == 0 ? 0
                : (int) Math.round(100.0 * (proUsersCount + squadUsersCount) / getTotalStudents());
    }
}
