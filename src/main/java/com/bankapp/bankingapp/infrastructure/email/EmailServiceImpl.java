package com.bankapp.bankingapp.infrastructure.email;

import com.bankapp.bankingapp.application.interfaces.service.IEmailService;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

@Service
public class EmailServiceImpl implements IEmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    private static final String BRAND_NAME = "CoreBank";

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${spring.mail.port:}")
    private String mailPort;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @PostConstruct
    void logMailConfiguration() {
        logger.info(
                "Mail service configured: host={}, port={}, username={}",
                blankToNotConfigured(mailHost),
                blankToNotConfigured(mailPort),
                maskEmail(fromEmail));
    }

    @Override
    @Async
    public void sendEmailVerificationOtp(String toEmail, String username, String otpCode) {
        sendHtmlEmail(toEmail, "CoreBank - Xác thực tài khoản", buildOtpTemplate(
                "Xác thực tài khoản",
                username,
                "Vui lòng sử dụng mã OTP bên dưới để hoàn tất xác thực tài khoản CoreBank của bạn.",
                otpCode,
                "Mã OTP có hiệu lực trong 3 phút. Không chia sẻ mã này với bất kỳ ai."));
    }

    @Override
    @Async
    public void sendPasswordResetOtp(String toEmail, String username, String otpCode) {
        sendHtmlEmail(toEmail, "CoreBank - Đặt lại mật khẩu", buildOtpTemplate(
                "Đặt lại mật khẩu",
                username,
                "Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn. Vui lòng nhập mã OTP bên dưới để tiếp tục.",
                otpCode,
                "Nếu bạn không yêu cầu thao tác này, hãy bỏ qua email và kiểm tra lại bảo mật tài khoản."));
    }

    @Override
    @Async
    public void sendPasswordChangedAlert(String toEmail, String username) {
        sendHtmlEmail(toEmail, "CoreBank - Mật khẩu vừa được thay đổi", buildSecurityAlertTemplate(
                "Mật khẩu vừa được thay đổi",
                username,
                "Mật khẩu đăng nhập của bạn đã được cập nhật thành công.",
                "Nếu đây không phải là thao tác của bạn, vui lòng đăng nhập lại, đổi mật khẩu ngay và liên hệ bộ phận hỗ trợ CoreBank.",
                "Kiểm tra tài khoản"));
    }

    @Override
    @Async
    public void sendPinChangedAlert(String toEmail, String username) {
        sendHtmlEmail(toEmail, "CoreBank - Mã PIN giao dịch vừa được thay đổi", buildSecurityAlertTemplate(
                "Mã PIN giao dịch vừa được thay đổi",
                username,
                "Mã PIN dùng để xác nhận giao dịch của bạn đã được cập nhật thành công.",
                "Nếu bạn không thực hiện thao tác này, vui lòng khóa các giao dịch nhạy cảm và liên hệ hỗ trợ CoreBank ngay lập tức.",
                "Xem lịch sử bảo mật"));
    }

    private void sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        String normalizedToEmail = normalizeEmail(toEmail);
        String normalizedFromEmail = normalizeEmail(fromEmail);

        if (normalizedToEmail == null) {
            logger.error("Cannot send email '{}': recipient email is blank or invalid", subject);
            return;
        }

        if (normalizedFromEmail == null) {
            logger.error("Cannot send email '{}': spring.mail.username is blank or invalid", subject);
            return;
        }

        try {
            logger.info("Sending email '{}' to {}", subject, maskEmail(normalizedToEmail));

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());

            helper.setFrom(new InternetAddress(normalizedFromEmail, BRAND_NAME, StandardCharsets.UTF_8.name()));
            helper.setTo(normalizedToEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Email sent successfully to {}", maskEmail(normalizedToEmail));
        } catch (MessagingException | UnsupportedEncodingException e) {
            logger.error("Failed to build email '{}' for {}: {}", subject, maskEmail(normalizedToEmail), e.getMessage(), e);
        } catch (MailException e) {
            logger.error("SMTP failed while sending email '{}' to {}: {}", subject, maskEmail(normalizedToEmail), e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected email error while sending '{}' to {}: {}", subject, maskEmail(normalizedToEmail), e.getMessage(), e);
        }
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        String trimmed = email.trim();
        if (!trimmed.contains("@")) {
            return null;
        }

        return trimmed;
    }

    private String maskEmail(String email) {
        String normalized = normalizeEmail(email);
        if (normalized == null) {
            return "not-configured";
        }

        String[] parts = normalized.split("@", 2);
        String local = parts[0];
        String domain = parts[1];
        if (local.length() <= 2) {
            return local.charAt(0) + "***@" + domain;
        }
        return local.substring(0, 2) + "***@" + domain;
    }

    private String blankToNotConfigured(String value) {
        return value == null || value.isBlank() ? "not-configured" : value.trim();
    }

    private String buildOtpTemplate(String heading, String username, String intro, String otpCode, String note) {
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { margin: 0; padding: 0; background: #eef4ff; font-family: Arial, Helvetica, sans-serif; color: #0f172a; }
                        .wrap { padding: 32px 16px; }
                        .card { max-width: 640px; margin: 0 auto; background: #ffffff; border-radius: 24px; overflow: hidden; box-shadow: 0 24px 70px rgba(15, 23, 42, 0.12); }
                        .hero { padding: 28px 32px; background: linear-gradient(135deg, #0f172a, #1d4ed8 55%%, #06b6d4); color: #ffffff; }
                        .brand { font-size: 22px; font-weight: 800; letter-spacing: 0.2px; }
                        .tag { display: inline-block; margin-top: 14px; padding: 6px 12px; border-radius: 999px; background: rgba(255,255,255,0.14); font-size: 12px; font-weight: 700; }
                        .body { padding: 32px; }
                        h1 { margin: 0 0 16px; font-size: 26px; line-height: 1.25; color: #0f172a; }
                        p { margin: 0 0 14px; font-size: 15px; line-height: 1.7; color: #334155; }
                        .otp { margin: 26px 0; padding: 22px; border-radius: 18px; background: #eff6ff; border: 1px solid #bfdbfe; text-align: center; font-size: 38px; letter-spacing: 10px; font-weight: 800; color: #1d4ed8; }
                        .note { padding: 16px; border-radius: 14px; background: #f8fafc; border: 1px solid #e2e8f0; color: #64748b; font-size: 14px; }
                        .footer { padding: 20px 32px 28px; color: #94a3b8; font-size: 12px; line-height: 1.6; }
                    </style>
                </head>
                <body>
                    <div class="wrap">
                        <div class="card">
                            <div class="hero">
                                <div class="brand">CoreBank</div>
                                <div class="tag">Xác thực bảo mật</div>
                            </div>
                            <div class="body">
                                <h1>%s</h1>
                                <p>Xin chào <strong>%s</strong>,</p>
                                <p>%s</p>
                                <div class="otp">%s</div>
                                <div class="note">%s</div>
                            </div>
                            <div class="footer">
                                Email này được gửi tự động từ CoreBank. Vui lòng không trả lời trực tiếp email này.
                            </div>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(escapeHtml(heading), escapeHtml(username), escapeHtml(intro), escapeHtml(otpCode), escapeHtml(note));
    }

    private String buildSecurityAlertTemplate(String heading, String username, String intro, String warning, String actionText) {
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { margin: 0; padding: 0; background: #0f172a; font-family: Arial, Helvetica, sans-serif; color: #e2e8f0; }
                        .wrap { padding: 32px 16px; }
                        .card { max-width: 640px; margin: 0 auto; background: #ffffff; border-radius: 24px; overflow: hidden; box-shadow: 0 24px 90px rgba(0, 0, 0, 0.35); }
                        .hero { padding: 30px 32px; background: linear-gradient(135deg, #111827, #1d4ed8 55%%, #06b6d4); color: #ffffff; }
                        .brand { font-size: 22px; font-weight: 800; letter-spacing: 0.2px; }
                        .badge { display: inline-block; margin-top: 14px; padding: 6px 12px; border-radius: 999px; background: rgba(34, 211, 238, 0.16); color: #cffafe; font-size: 12px; font-weight: 700; }
                        .body { padding: 32px; color: #0f172a; }
                        h1 { margin: 0 0 16px; font-size: 26px; line-height: 1.25; color: #0f172a; }
                        p { margin: 0 0 14px; font-size: 15px; line-height: 1.7; color: #334155; }
                        .status { margin: 24px 0; padding: 18px; border-radius: 18px; background: #ecfdf5; border: 1px solid #bbf7d0; color: #047857; font-weight: 700; }
                        .alert { margin: 20px 0 24px; padding: 18px; border-radius: 18px; background: #fff7ed; border: 1px solid #fed7aa; color: #9a3412; line-height: 1.6; }
                        .button { display: inline-block; padding: 14px 20px; border-radius: 14px; background: #2563eb; color: #ffffff !important; text-decoration: none; font-weight: 800; }
                        .meta { margin-top: 24px; padding: 16px; border-radius: 14px; background: #f8fafc; border: 1px solid #e2e8f0; color: #64748b; font-size: 13px; }
                        .footer { padding: 20px 32px 28px; color: #94a3b8; font-size: 12px; line-height: 1.6; background: #ffffff; }
                    </style>
                </head>
                <body>
                    <div class="wrap">
                        <div class="card">
                            <div class="hero">
                                <div class="brand">CoreBank</div>
                                <div class="badge">Cảnh báo bảo mật tài khoản</div>
                            </div>
                            <div class="body">
                                <h1>%s</h1>
                                <p>Xin chào <strong>%s</strong>,</p>
                                <div class="status">%s</div>
                                <div class="alert">%s</div>
                                <a class="button" href="#">%s</a>
                                <div class="meta">
                                    Vì lý do bảo mật, CoreBank không bao giờ yêu cầu bạn cung cấp mật khẩu, mã PIN hoặc OTP qua email, điện thoại hay tin nhắn.
                                </div>
                            </div>
                            <div class="footer">
                                Email này được gửi tự động để bảo vệ tài khoản của bạn. Nếu cần hỗ trợ, vui lòng liên hệ tổng đài hoặc truy cập ứng dụng CoreBank.
                            </div>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(escapeHtml(heading), escapeHtml(username), escapeHtml(intro), escapeHtml(warning), escapeHtml(actionText));
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
