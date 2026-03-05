package com.bankapp.bankingapp.domain.model.enums;

public enum UserStatus {
    PENDING, // Mới đăng ký, chờ xác thực email OTP
    ACTIVE,
    LOCKED,
    DISABLED
}
