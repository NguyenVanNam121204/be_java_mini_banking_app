package com.bankapp.bankingapp.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponseDto {
    private long totalUsers;
    private long todayTransactions;
    private long lockedUsers;
    private double uptime;
    private List<Map<String, Object>> chartData;
    private List<RecentActivityDto> recentActivities;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentActivityDto {
        private String type;
        private String description;
        private String timeAgo;
    }
}
