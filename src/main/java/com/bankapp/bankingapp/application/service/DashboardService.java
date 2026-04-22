package com.bankapp.bankingapp.application.service;

import com.bankapp.bankingapp.application.dto.response.DashboardStatsResponseDto;
import com.bankapp.bankingapp.application.interfaces.service.IDashboardService;
import com.bankapp.bankingapp.domain.model.enums.UserStatus;
import com.bankapp.bankingapp.infrastructure.persistence.jpaRepository.AuditLogJpaRepository;
import com.bankapp.bankingapp.infrastructure.persistence.jpaRepository.TransactionJpaRepository;
import com.bankapp.bankingapp.infrastructure.persistence.jpaRepository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService implements IDashboardService {

    private final UserJpaRepository userJpaRepository;
    private final TransactionJpaRepository transactionJpaRepository;
    private final AuditLogJpaRepository auditLogJpaRepository;

    @Override
    public DashboardStatsResponseDto getDashboardStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.with(LocalTime.MIN);
        
        long totalUsers = userJpaRepository.count();
        long todayTransactions = transactionJpaRepository.countByCreatedAtAfter(startOfDay);
        long lockedUsers = userJpaRepository.countByStatus(UserStatus.LOCKED);
        
        // Real chart data: Last 6 months transaction volume
        List<Map<String, Object>> chartData = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            LocalDateTime startOfMonth = now.minusMonths(i).withDayOfMonth(1).with(LocalTime.MIN);
            LocalDateTime endOfMonth = now.minusMonths(i).with(LocalTime.MAX).withDayOfMonth(now.minusMonths(i).toLocalDate().lengthOfMonth());
            
            java.math.BigDecimal totalAmount = transactionJpaRepository.sumAmountByCreatedAtBetween(startOfMonth, endOfMonth);
            if (totalAmount == null) totalAmount = java.math.BigDecimal.ZERO;
            
            Map<String, Object> data = new HashMap<>();
            data.put("name", startOfMonth.getMonth().name().substring(0, 3));
            data.put("value", totalAmount);
            chartData.add(data);
        }

        // Real recent activities from Audit Logs
        List<DashboardStatsResponseDto.RecentActivityDto> activities = auditLogJpaRepository.findTop5ByOrderByCreatedAtDesc()
                .stream()
                .map(log -> DashboardStatsResponseDto.RecentActivityDto.builder()
                        .id(log.getId())
                        .action(log.getAction())
                        .username(log.getUsername())
                        .details(log.getDetails())
                        .status("SUCCESS") // Entity currently doesn't store status
                        .createdAt(log.getCreatedAt())
                        .timeAgo(calculateTimeAgo(log.getCreatedAt()))
                        .build())
                .collect(Collectors.toList());

        return DashboardStatsResponseDto.builder()
                .totalUsers(totalUsers)
                .todayTransactions(todayTransactions)
                .lockedUsers(lockedUsers)
                .chartData(chartData)
                .recentActivities(activities)
                .build();
    }

    private String calculateTimeAgo(LocalDateTime createdAt) {
        LocalDateTime now = LocalDateTime.now();
        long seconds = java.time.Duration.between(createdAt, now).getSeconds();
        
        if (seconds < 60) return seconds + " giây trước";
        if (seconds < 3600) return (seconds / 60) + " phút trước";
        if (seconds < 86400) return (seconds / 3600) + " giờ trước";
        return (seconds / 86400) + " ngày trước";
    }
}
