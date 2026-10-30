package com.group2.assignment.settings.dto;

public record AdminStatsDto(long totalUsers, long freeUsersCount, long proUsersCount,
                            long totalCourses, long totalChunks, long todayRagQueries,
                            Long geminiPingLatencyMs) {
    public int getProConversionRate() {
        return totalUsers == 0 ? 0 : (int) Math.round(100.0 * proUsersCount / totalUsers);
    }
}
