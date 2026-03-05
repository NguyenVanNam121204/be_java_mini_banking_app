package com.bankapp.bankingapp.domain.model;

import com.bankapp.bankingapp.domain.model.enums.OtpType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Domain model cho OTP (One-Time Password)
 * Dùng chung cho:
 * - Xác thực email sau đăng ký (OtpType.EMAIL_VERIFICATION)
 * - Quên mật khẩu (OtpType.PASSWORD_RESET)
 *
 * Lưu ý: codeHash là BCrypt hash của mã 6 số gốc, không lưu plain text
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OtpCode {

    @Setter
    @EqualsAndHashCode.Include
    private Long id;

    private Long userId;
    private String codeHash; // BCrypt hash của mã 6 số
    private OtpType type; // Loại OTP: xác thực email hoặc reset password
    private LocalDateTime expiresAt;
    private boolean used;
    private int attemptCount; // Số lần nhập sai
    private LocalDateTime createdAt;
    private LocalDateTime usedAt;

    public OtpCode(Long userId, String codeHash, OtpType type, LocalDateTime expiresAt) {
        this.userId = userId;
        this.codeHash = codeHash;
        this.type = type;
        this.expiresAt = expiresAt;
        this.used = false;
        this.attemptCount = 0;
        this.createdAt = LocalDateTime.now();
    }

    // BUSINESS LOGIC

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isLocked() {
        return attemptCount >= 5;
    }

    /**
     * OTP hợp lệ khi: chưa dùng + chưa hết hạn + chưa bị lock (< 5 lần sai)
     */
    public boolean isValid() {
        return !used && !isExpired() && !isLocked();
    }

    public void incrementAttempt() {
        this.attemptCount++;
    }

    public int getRemainingAttempts() {
        return Math.max(0, 5 - attemptCount);
    }

    public void markAsUsed() {
        if (this.used) {
            throw new IllegalStateException("Mã OTP đã được sử dụng");
        }
        if (isExpired()) {
            throw new IllegalStateException("Mã OTP đã hết hạn");
        }
        this.used = true;
        this.usedAt = LocalDateTime.now();
    }
}
