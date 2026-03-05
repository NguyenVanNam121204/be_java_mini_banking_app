package com.bankapp.bankingapp.domain.model.enums;

/**
 * Loại OTP - dùng để phân biệt mục đích của mã OTP
 */
public enum OtpType {
    EMAIL_VERIFICATION, // Xác thực tài khoản sau khi đăng ký
    PASSWORD_RESET // Quên mật khẩu
}
