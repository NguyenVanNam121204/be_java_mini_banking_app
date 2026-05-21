package com.bankapp.bankingapp.application.interfaces.service;

/**
 * Interface for email delivery. The implementation lives in infrastructure.
 */
public interface IEmailService {

    void sendEmailVerificationOtp(String toEmail, String username, String otpCode);

    void sendPasswordResetOtp(String toEmail, String username, String otpCode);

    void sendPasswordChangedAlert(String toEmail, String username);

    void sendPinChangedAlert(String toEmail, String username);
}
