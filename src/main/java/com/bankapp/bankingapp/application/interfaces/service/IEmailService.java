package com.bankapp.bankingapp.application.interfaces.service;

/**
 * Interface cho Email Service - thuộc Application Layer
 * Implementation ở Infrastructure layer (EmailServiceImpl)
 */
public interface IEmailService {

    /**
     * Gửi email với OTP xác thực tài khoản sau đăng ký
     */
    void sendEmailVerificationOtp(String toEmail, String username, String otpCode);

    /**
     * Gửi email với OTP reset mật khẩu
     */
    void sendPasswordResetOtp(String toEmail, String username, String otpCode);
}
