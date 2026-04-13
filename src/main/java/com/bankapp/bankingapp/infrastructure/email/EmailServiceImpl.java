package com.bankapp.bankingapp.infrastructure.email;

import com.bankapp.bankingapp.application.interfaces.service.IEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Implementation của IEmailService - thuộc Infrastructure layer
 * Sử dụng Spring JavaMailSender để gửi email qua SMTP
 * 
 * @Async để gửi email không block luồng chính (không làm chậm API response)
 */
@Service
public class EmailServiceImpl implements IEmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    @Async
    public void sendEmailVerificationOtp(String toEmail, String username, String otpCode) {
        String subject = "🏦 Banking App - Xác thực tài khoản của bạn";
        String htmlContent = buildEmailVerificationHtml(username, otpCode);
        sendHtmlEmail(toEmail, subject, htmlContent);
    }

    @Override
    @Async
    public void sendPasswordResetOtp(String toEmail, String username, String otpCode) {
        String subject = "🏦 Banking App - Đặt lại mật khẩu";
        String htmlContent = buildPasswordResetHtml(username, otpCode);
        sendHtmlEmail(toEmail, subject, htmlContent);
    }

    private void sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail != null ? fromEmail : "noreply@bankingapp.com");
            helper.setTo(java.util.Objects.requireNonNull(toEmail));
            helper.setSubject(java.util.Objects.requireNonNull(subject));
            helper.setText(java.util.Objects.requireNonNull(htmlContent), true); // true = isHtml

            mailSender.send(message);
            logger.info("✅ Email sent successfully to: {}", toEmail);

        } catch (MessagingException e) {
            logger.error("❌ Failed to send email to {}: {}", toEmail, e.getMessage());
            // Không throw exception để không fail request chính
            // Trong production nên dùng retry mechanism (RabbitMQ, Kafka, v.v.)
        }
    }

    private String buildEmailVerificationHtml(String username, String otpCode) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; background: #f4f4f4; margin: 0; padding: 0; }
                        .container { max-width: 600px; margin: 40px auto; background: #fff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.1); }
                        .header { background: linear-gradient(135deg, #1a73e8, #0d47a1); padding: 30px; text-align: center; }
                        .header h1 { color: #fff; margin: 0; font-size: 24px; }
                        .body { padding: 40px 30px; }
                        .otp-box { background: #f0f7ff; border: 2px dashed #1a73e8; border-radius: 12px; text-align: center; padding: 24px; margin: 24px 0; }
                        .otp-code { font-size: 42px; font-weight: bold; color: #1a73e8; letter-spacing: 10px; }
                        .note { color: #666; font-size: 14px; margin-top: 10px; }
                        .footer { background: #f4f4f4; padding: 20px; text-align: center; color: #999; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🏦 Banking App</h1>
                        </div>
                        <div class="body">
                            <p>Xin chào <strong>%s</strong>,</p>
                            <p>Cảm ơn bạn đã đăng ký tài khoản tại <strong>Banking App</strong>. Vui lòng sử dụng mã OTP bên dưới để xác thực tài khoản của bạn:</p>
                            <div class="otp-box">
                                <div class="otp-code">%s</div>
                                <div class="note">⏱️ Mã có hiệu lực trong <strong>10 phút</strong></div>
                            </div>
                            <p>Nếu bạn không thực hiện đăng ký này, vui lòng bỏ qua email này.</p>
                            <p>Trân trọng,<br><strong>Banking App Team</strong></p>
                        </div>
                        <div class="footer">
                            <p>© 2025 Banking App. All rights reserved.</p>
                            <p>Email này được gửi tự động, vui lòng không reply.</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(username, otpCode);
    }

    private String buildPasswordResetHtml(String username, String otpCode) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; background: #f4f4f4; margin: 0; padding: 0; }
                        .container { max-width: 600px; margin: 40px auto; background: #fff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.1); }
                        .header { background: linear-gradient(135deg, #e53935, #b71c1c); padding: 30px; text-align: center; }
                        .header h1 { color: #fff; margin: 0; font-size: 24px; }
                        .body { padding: 40px 30px; }
                        .otp-box { background: #fff5f5; border: 2px dashed #e53935; border-radius: 12px; text-align: center; padding: 24px; margin: 24px 0; }
                        .otp-code { font-size: 42px; font-weight: bold; color: #e53935; letter-spacing: 10px; }
                        .note { color: #666; font-size: 14px; margin-top: 10px; }
                        .warning { background: #fff3cd; border-left: 4px solid #ffc107; padding: 12px; margin: 16px 0; border-radius: 4px; }
                        .footer { background: #f4f4f4; padding: 20px; text-align: center; color: #999; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🔒 Đặt Lại Mật Khẩu</h1>
                        </div>
                        <div class="body">
                            <p>Xin chào <strong>%s</strong>,</p>
                            <p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn. Sử dụng mã OTP bên dưới:</p>
                            <div class="otp-box">
                                <div class="otp-code">%s</div>
                                <div class="note">⏱️ Mã có hiệu lực trong <strong>10 phút</strong></div>
                            </div>
                            <div class="warning">
                                ⚠️ <strong>Lưu ý bảo mật:</strong> Không chia sẻ mã OTP này với bất kỳ ai. Banking App sẽ không bao giờ yêu cầu mã OTP qua điện thoại.
                            </div>
                            <p>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này và đảm bảo tài khoản của bạn vẫn an toàn.</p>
                            <p>Trân trọng,<br><strong>Banking App Team</strong></p>
                        </div>
                        <div class="footer">
                            <p>© 2025 Banking App. All rights reserved.</p>
                            <p>Email này được gửi tự động, vui lòng không reply.</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(username, otpCode);
    }
}
