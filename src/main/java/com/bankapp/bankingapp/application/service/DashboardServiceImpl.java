package com.bankapp.bankingapp.application.service;

import com.bankapp.bankingapp.application.dto.response.DashboardStatsResponseDto;
import com.bankapp.bankingapp.application.interfaces.repository.IDashboardRepository;
import com.bankapp.bankingapp.application.interfaces.service.IDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DashboardServiceImpl — Application layer.
 * Chi phu thuoc vao IDashboardRepository (interface).
 * KHONG import bat ky class nao tu Infrastructure layer (JPA Repository, Entity,...).
 * Tuan thu Clean Architecture va Dependency Inversion Principle (DIP).
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements IDashboardService {

    private final IDashboardRepository dashboardRepository;

    @Override
    public DashboardStatsResponseDto getDashboardStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.with(LocalTime.MIN);

        long totalUsers = dashboardRepository.countTotalUsers();
        long todayTransactions = dashboardRepository.countTransactionsAfter(startOfDay);
        long lockedUsers = dashboardRepository.countLockedUsers();

        // Lay du lieu bieu do: 6 thang gan nhat
        List<Map<String, Object>> chartData = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            LocalDateTime startOfMonth = now.minusMonths(i).withDayOfMonth(1).with(LocalTime.MIN);
            LocalDateTime endOfMonth = now.minusMonths(i)
                    .with(LocalTime.MAX)
                    .withDayOfMonth(now.minusMonths(i).toLocalDate().lengthOfMonth());

            java.math.BigDecimal totalAmount = dashboardRepository.sumTransactionAmountBetween(startOfMonth, endOfMonth);

            Map<String, Object> data = new java.util.HashMap<>();
            data.put("name", startOfMonth.getMonth().name().substring(0, 3));
            data.put("value", totalAmount);
            chartData.add(data);
        }

        // Lay hoat dong gan nhat tu Audit Log thong qua Repository interface
        List<DashboardStatsResponseDto.RecentActivityDto> activities = new ArrayList<>();
        dashboardRepository.findTop5RecentActivities().forEach(entry -> {
            activities.add(DashboardStatsResponseDto.RecentActivityDto.builder()
                    .id((Long) entry.get("id"))
                    .action((String) entry.get("action"))
                    .username((String) entry.get("username"))
                    .details((String) entry.get("details"))
                    .status("SUCCESS")
                    .createdAt((java.time.LocalDateTime) entry.get("createdAt"))
                    .timeAgo(calculateTimeAgo((java.time.LocalDateTime) entry.get("createdAt")))
                    .build());
        });

        return DashboardStatsResponseDto.builder()
                .totalUsers(totalUsers)
                .todayTransactions(todayTransactions)
                .lockedUsers(lockedUsers)
                .chartData(chartData)
                .recentActivities(activities)
                .build();
    }

    private String calculateTimeAgo(LocalDateTime createdAt) {
        if (createdAt == null) return "N/A";
        long seconds = java.time.Duration.between(createdAt, LocalDateTime.now()).getSeconds();

        if (seconds < 60) return seconds + " giay truoc";
        if (seconds < 3600) return (seconds / 60) + " phut truoc";
        if (seconds < 86400) return (seconds / 3600) + " gio truoc";
        return (seconds / 86400) + " ngay truoc";
    }
}
