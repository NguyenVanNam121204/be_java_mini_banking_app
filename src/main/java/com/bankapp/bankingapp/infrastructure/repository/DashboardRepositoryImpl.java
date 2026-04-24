package com.bankapp.bankingapp.infrastructure.repository;

import com.bankapp.bankingapp.application.interfaces.repository.IDashboardRepository;
import com.bankapp.bankingapp.domain.model.enums.UserStatus;
import com.bankapp.bankingapp.infrastructure.persistence.jpaRepository.AuditLogJpaRepository;
import com.bankapp.bankingapp.infrastructure.persistence.jpaRepository.TransactionJpaRepository;
import com.bankapp.bankingapp.infrastructure.persistence.jpaRepository.UserJpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation cua IDashboardRepository.
 * Day la noi DUY NHAT duoc phep inject JPA repositories de lay du lieu cho Dashboard.
 * DashboardServiceImpl chi biet den IDashboardRepository — khong biet den class nay.
 */
@Repository
public class DashboardRepositoryImpl implements IDashboardRepository {

    private final UserJpaRepository userJpaRepository;
    private final TransactionJpaRepository transactionJpaRepository;
    private final AuditLogJpaRepository auditLogJpaRepository;

    public DashboardRepositoryImpl(UserJpaRepository userJpaRepository,
                                   TransactionJpaRepository transactionJpaRepository,
                                   AuditLogJpaRepository auditLogJpaRepository) {
        this.userJpaRepository = userJpaRepository;
        this.transactionJpaRepository = transactionJpaRepository;
        this.auditLogJpaRepository = auditLogJpaRepository;
    }

    @Override
    public long countTotalUsers() {
        return userJpaRepository.count();
    }

    @Override
    public long countTransactionsAfter(LocalDateTime since) {
        return transactionJpaRepository.countByCreatedAtAfter(since);
    }

    @Override
    public long countLockedUsers() {
        return userJpaRepository.countByStatus(UserStatus.LOCKED);
    }

    @Override
    public BigDecimal sumTransactionAmountBetween(LocalDateTime from, LocalDateTime to) {
        BigDecimal result = transactionJpaRepository.sumAmountByCreatedAtBetween(from, to);
        return result != null ? result : BigDecimal.ZERO;
    }

    @Override
    public List<Map<String, Object>> findTop5RecentActivities() {
        List<Map<String, Object>> activities = new ArrayList<>();
        auditLogJpaRepository.findTop5ByOrderByCreatedAtDesc().forEach(log -> {
            Map<String, Object> entry = new HashMap<>();
            entry.put("id", log.getId());
            entry.put("action", log.getAction());
            entry.put("username", log.getUsername());
            entry.put("details", log.getDetails());
            entry.put("createdAt", log.getCreatedAt());
            activities.add(entry);
        });
        return activities;
    }
}
