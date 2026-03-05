package com.bankapp.bankingapp.application.interfaces.service;

import com.bankapp.bankingapp.application.dto.request.*;
import com.bankapp.bankingapp.application.dto.response.AuthResponseDto;

public interface AuthService {

    AuthResponseDto register(RegisterRequestDto request);

    AuthResponseDto login(LoginRequestDto request);

    /**
     * Gọi endpoint này khi access token hết hạn.
     * Trả về cả access token MỚI và refresh token MỚI (token rotation).
     * Refresh token cũ bị revoke trong DB.
     */
    AuthResponseDto refreshToken(RefreshTokenRequestDto request);

    /**
     * Xác thực email bằng OTP sau đăng ký.
     * Thành công → UserStatus.PENDING → ACTIVE
     */
    void verifyEmail(VerifyEmailRequestDto request);

    /**
     * Gửi OTP đặt lại mật khẩu về email.
     * Không tiết lộ email tồn tại hay không (security best practice)
     */
    void forgotPassword(ForgotPasswordRequestDto request);

    /**
     * Xác minh OTP và đặt lại mật khẩu mới.
     */
    void resetPassword(ResetPasswordRequestDto request);

    /**
     * Gửi lại OTP xác thực email (khi OTP cũ hết hạn)
     */
    void resendVerificationOtp(ForgotPasswordRequestDto request);
}
