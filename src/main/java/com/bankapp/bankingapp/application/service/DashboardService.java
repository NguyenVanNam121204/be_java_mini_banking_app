package com.bankapp.bankingapp.application.service;

import com.bankapp.bankingapp.application.dto.response.DashboardStatsResponseDto;
import com.bankapp.bankingapp.application.interfaces.service.IDashboardService;
import com.bankapp.bankingapp.domain.model.enums.UserStatus;
import com.bankapp.bankingapp.infrastructure.persistence.jpaRepository.TransactionJpaRepository;
import com.bankapp.bankingapp.infrastructure.persistence.jpaRepository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService implements IDashboardService {

    private final UserJpaRepository userJpaRepository;
    private final TransactionJpaRepository transactionJpaRepository;

    @Override
    public DashboardStatsResponseDto getDashboardStats() {
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        
        long totalUsers = userJpaRepository.count();
        long todayTransactions = transactionJpaRepository.countByCreatedAtAfter(startOfDay);
        long lockedUsers = userJpaRepository.countByStatus(UserStatus.LOCKED);
        
        // Mock chart data for now (usually you'd aggregate this from DB)
        List<Map<String, Object>> chartData = new ArrayList<>();
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun"};
        int[] flow = {4000, 3000, 2000, 2780, 1890, 2390};
        
        for (int i = 0; i < months.length; i++) {
            Map<String, Object> data = new HashMap<>();
            data.put("name", months[i]);
            data.put("value", flow[i]);
            chartData.add(data);
        }

        // Mock recent activities
        List<DashboardStatsResponseDto.RecentActivityDto> activities = new ArrayList<>();
        activities.add(DashboardStatsResponseDto.RecentActivityDto.builder()
                .type("USER_REGISTER")
                .description("Người dùng mới nambo69@gmail.com vừa đăng ký")
                .timeAgo("5 phút trước")
                .build());
        activities.add(DashboardStatsResponseDto.RecentActivityDto.builder()
                .type("TRANSACTION")
                .description("Chuyển tiền thành công: 500,000 VND")
                .timeAgo("15 phút trước")
                .build());

        return DashboardStatsResponseDto.builder()
                .totalUsers(totalUsers)
                .todayTransactions(todayTransactions)
                .lockedUsers(lockedUsers)
                .uptime(99.99)
                .chartData(chartData)
                .recentActivities(activities)
                .build();
    }
}
