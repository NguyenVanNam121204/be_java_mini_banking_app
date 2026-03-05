package com.bankapp.bankingapp.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Mã 6 số để reset password
 * Gửi qua SMTP
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PasswordResetCode {

    @Setter
    @EqualsAndHashCode.Include
    private Long id;

    private Long userId;
    private String codeHash;        // Hash của mã 6 số (không lưu plain text)
    private LocalDateTime expiresAt;
    private boolean used;
    private int attemptCount;       // Số lần nhập sai

    private LocalDateTime createdAt;
    private LocalDateTime usedAt;

    public PasswordResetCode(Long id, Long userId, String codeHash, LocalDateTime expiresAt) {
        this.id = id;
        this.userId = userId;
        this.codeHash = codeHash;
        this.expiresAt = expiresAt;
        this.used = false;
        this.attemptCount = 0;
        this.createdAt = LocalDateTime.now();
    }

    // BUSINESS LOGIC

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !used && !isExpired() && attemptCount < 5;
    }

    public void markAsUsed() {
        if (this.used) {
            throw new IllegalStateException("Mã đã được sử dụng");
        }
        if (isExpired()) {
            throw new IllegalStateException("Mã đã hết hạn");
        }
        this.used = true;
        this.usedAt = LocalDateTime.now();
    }

    public void incrementAttempt() {
        this.attemptCount++;
    }

    public boolean isLocked() {
        return attemptCount >= 5;
    }

    public int getRemainingAttempts() {
        return Math.max(0, 5 - attemptCount);
    }
}
