package com.bankapp.bankingapp.application.interfaces.repository;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Repository interface cho Dashboard Stats — nam o Application layer.
 * Infrastructure se implement interface nay, Application Service chi phu thuoc vao interface.
 * Tuan thu Clean Architecture: Application layer khong biet den JPA, Entity, hay bat ky
 * implementation detail nao cua tang Infrastructure.
 */
public interface IDashboardRepository {

    /** Dem tong so nguoi dung trong he thong */
    long countTotalUsers();

    /** Dem so giao dich duoc tao sau mot moc thoi gian cu the (dung tinh giao dich hom nay) */
    long countTransactionsAfter(LocalDateTime since);

    /** Dem so nguoi dung bi khoa (status = LOCKED) */
    long countLockedUsers();

    /**
     * Lay tong gia tri giao dich (amount) trong mot khoang thoi gian.
     * Tra ve BigDecimal.ZERO neu khong co giao dich nao.
     */
    BigDecimal sumTransactionAmountBetween(LocalDateTime from, LocalDateTime to);

    /**
     * Lay 5 audit log gan nhat de hien thi tren dashboard.
     * Tra ve List cac Map<String, Object> de tranh coupling voi Entity.
     */
    List<Map<String, Object>> findTop5RecentActivities();
}
