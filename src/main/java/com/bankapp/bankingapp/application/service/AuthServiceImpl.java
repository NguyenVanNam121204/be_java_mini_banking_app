package com.bankapp.bankingapp.application.service;

import com.bankapp.bankingapp.application.dto.request.*;
import com.bankapp.bankingapp.application.dto.response.AuthResponseDto;
import com.bankapp.bankingapp.application.interfaces.repository.IOtpCodeRepository;
import com.bankapp.bankingapp.application.interfaces.repository.IRefreshTokenRepository;
import com.bankapp.bankingapp.application.interfaces.repository.IRoleRepository;
import com.bankapp.bankingapp.application.interfaces.repository.IUserRepository;
import com.bankapp.bankingapp.application.interfaces.service.IAuditService;
import com.bankapp.bankingapp.domain.model.enums.AuditAction;
import com.bankapp.bankingapp.application.interfaces.service.IAuthService;
import com.bankapp.bankingapp.application.interfaces.service.IEmailService;
import com.bankapp.bankingapp.application.interfaces.service.IJwtTokenProvider;
import com.bankapp.bankingapp.application.mapper.AuthDtoMapper;
import com.bankapp.bankingapp.application.mapper.UserDtoMapper;
import com.bankapp.bankingapp.application.validator.UserValidator;
import com.bankapp.bankingapp.application.validator.ValidationException;
import com.bankapp.bankingapp.domain.model.OtpCode;
import com.bankapp.bankingapp.domain.model.RefreshToken;
import com.bankapp.bankingapp.domain.model.Role;
import com.bankapp.bankingapp.domain.model.User;
import com.bankapp.bankingapp.domain.model.enums.OtpType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuthServiceImpl implements IAuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final IUserRepository userRepository;
    private final IRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final IJwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserValidator userValidator;
    private final UserDtoMapper userDtoMapper;
    private final AuthDtoMapper authDtoMapper;
    private final IRefreshTokenRepository refreshTokenRepository;
    private final IOtpCodeRepository otpCodeRepository;
    private final IEmailService emailService;
    private final IAuditService auditService;

    @Value("${app.otp.expiration-minutes:10}")
    private int otpExpirationMinutes;

    public AuthServiceImpl(IUserRepository userRepository,
            IRoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            com.bankapp.bankingapp.infrastructure.security.jwt.JwtTokenProvider jwtTokenProvider,
            AuthenticationManager authenticationManager,
            UserValidator userValidator,
            UserDtoMapper userDtoMapper,
            AuthDtoMapper authDtoMapper,
            IRefreshTokenRepository refreshTokenRepository,
            IOtpCodeRepository otpCodeRepository,
            IEmailService emailService,
            IAuditService auditService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
        this.userValidator = userValidator;
        this.userDtoMapper = userDtoMapper;
        this.authDtoMapper = authDtoMapper;
        this.refreshTokenRepository = refreshTokenRepository;
        this.otpCodeRepository = otpCodeRepository;
        this.emailService = emailService;
        this.auditService = auditService;
    }

    // =========================================================
    // REGISTER
    // =========================================================
    @Override
    @Transactional
    public AuthResponseDto register(RegisterRequestDto request) {
        // Validate input
        List<String> errors = userValidator.validateForCreation(
                request.getUsername(), request.getEmail(), request.getPassword(), null);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        // Check duplicates
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ValidationException("Username đã tồn tại");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email đã tồn tại");
        }

        // Create user (status = PENDING)
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User user = userDtoMapper.toUserFromRegisterDto(request, hashedPassword);

        // Assign default USER role
        Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> roleRepository.save(new Role(null, "USER")));
        user.addRole(userRole);

        User savedUser = userRepository.save(user);

        // Tạo và gửi OTP xác thực email
        sendOtp(savedUser, OtpType.EMAIL_VERIFICATION);

        // Ghi Audit Log
        auditService.logAction(savedUser.getUsername(), AuditAction.REGISTER,
            String.format("Người dùng [%s] đăng ký tài khoản mới thành công qua Email: %s", savedUser.getUsername(), savedUser.getEmail()));

        // Không tạo JWT khi đăng ký (user chưa ACTIVE)
        // Chỉ trả về thông tin user, token là null
        return authDtoMapper.toAuthResponseDto(null, null, null, savedUser);
    }

    // =========================================================
    // LOGIN
    // =========================================================
    @Override
    @Transactional
    public AuthResponseDto login(LoginRequestDto request) {
        // Spring Security authenticate
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Lấy user từ DB
        User user = userRepository.findByUsername(request.getUsername())
                .or(() -> userRepository.findByEmail(request.getUsername()))
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Kiểm tra trạng thái
        if (user.getStatus() == com.bankapp.bankingapp.domain.model.enums.UserStatus.PENDING) {
            throw new ValidationException("Tài khoản chưa được xác thực. Vui lòng kiểm tra email để lấy mã OTP.");
        }

        if (!user.isActive()) {
            throw new RuntimeException("Tài khoản đã bị khóa hoặc vô hiệu hóa");
        }

        // Tạo Access Token
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);

        // Tạo Refresh Token và lưu vào DB (xóa token cũ trước)
        String rawRefreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());
        saveRefreshTokenToDb(user, rawRefreshToken);

        // Ghi Audit Log
        auditService.logAction(user.getUsername(), AuditAction.LOGIN,
            String.format("Người dùng [%s] đã đăng nhập thành công vào hệ thống", user.getUsername()));

        return authDtoMapper.toAuthResponseDto(
                accessToken,
                rawRefreshToken,
                jwtTokenProvider.getJwtExpiration(),
                user);
    }

    // =========================================================
    // REFRESH TOKEN (Token Rotation - enterprise pattern)
    // =========================================================
    @Override
    @Transactional
    public AuthResponseDto refreshToken(RefreshTokenRequestDto request) {
        String rawRefreshToken = request.getRefreshToken();

        // 1. Validate JWT signature & expiry
        if (!jwtTokenProvider.validateToken(rawRefreshToken)) {
            throw new ValidationException("Refresh token không hợp lệ hoặc đã hết hạn");
        }

        // 2. Hash token để tra cứu trong DB
        String tokenHash = hashToken(rawRefreshToken);

        // 3. Tìm trong DB và kiểm tra trạng thái domain
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new ValidationException("Refresh token không tồn tại"));

        if (!storedToken.isValid()) {
            throw new ValidationException("Refresh token đã bị thu hồi hoặc hết hạn");
        }

        // 4. Lấy username từ token
        String username = jwtTokenProvider.getUsernameFromToken(rawRefreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        if (!user.isActive()) {
            throw new RuntimeException("Tài khoản không còn hoạt động");
        }

        // 5. Revoke token cũ (Token Rotation - bảo mật cao)
        storedToken.revoke();
        refreshTokenRepository.save(storedToken);

        // 6. Tạo access token và refresh token mới
        String newAccessToken = jwtTokenProvider.generateAccessTokenFromUsername(
                user.getUsername(), buildRolesString(user));

        String newRawRefreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());
        saveRefreshTokenToDb(user, newRawRefreshToken);

        logger.debug("Token rotation successful for user: {}", username);

        return authDtoMapper.toAuthResponseDto(
                newAccessToken,
                newRawRefreshToken,
                jwtTokenProvider.getJwtExpiration(),
                user);
    }

    // =========================================================
    // VERIFY EMAIL
    // =========================================================
    @Override
    @Transactional
    public void verifyEmail(VerifyEmailRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ValidationException("Không tìm thấy tài khoản với email này"));

        if (user.isActive()) {
            throw new ValidationException("Tài khoản đã được xác thực rồi");
        }

        if (user.getStatus() != com.bankapp.bankingapp.domain.model.enums.UserStatus.PENDING) {
            throw new ValidationException("Trạng thái tài khoản không hợp lệ");
        }

        // Validate OTP
        OtpCode otp = otpCodeRepository
                .findLatestValidByUserIdAndType(user.getId(), OtpType.EMAIL_VERIFICATION)
                .orElseThrow(() -> new ValidationException(
                        "Mã OTP không tồn tại hoặc đã hết hạn. Vui lòng yêu cầu gửi lại."));

        if (!otp.isValid()) {
            throw new ValidationException(buildOtpErrorMessage(otp));
        }

        if (!passwordEncoder.matches(request.getOtp(), otp.getCodeHash())) {
            otp.incrementAttempt();
            otpCodeRepository.save(otp);
            throw new ValidationException("Mã OTP không đúng. Còn " + otp.getRemainingAttempts() + " lần thử.");
        }

        // OTP hợp lệ → kích hoạt user
        otp.markAsUsed();
        otpCodeRepository.save(otp);

        // Cập nhật status → ACTIVE
        user.setStatus(com.bankapp.bankingapp.domain.model.enums.UserStatus.ACTIVE);
        userRepository.save(user);

        // Ghi Audit Log
        auditService.logAction(user.getUsername(), AuditAction.EMAIL_VERIFIED,
            String.format("Người dùng [%s] đã xác thực Email thành công thông qua mã OTP", user.getUsername()));

        logger.info("✅ Email verified successfully for user: {}", user.getUsername());
    }

    // =========================================================
    // FORGOT PASSWORD
    // =========================================================
    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequestDto request) {
        // Security: không tiết lộ email có tồn tại hay không
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            if (user.isActive()) { // Chỉ gửi OTP cho tài khoản đang ACTIVE
                sendOtp(user, OtpType.PASSWORD_RESET);
            }
        });
        // Luôn trả về thành công (không leak info)
        logger.info("Forgot password requested for email: {}", request.getEmail());
    }

    // =========================================================
    // RESET PASSWORD
    // =========================================================
    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ValidationException("Không tìm thấy tài khoản với email này"));

        if (!user.isActive()) {
            throw new ValidationException("Tài khoản không hợp lệ");
        }

        // Validate OTP
        OtpCode otp = otpCodeRepository
                .findLatestValidByUserIdAndType(user.getId(), OtpType.PASSWORD_RESET)
                .orElseThrow(() -> new ValidationException(
                        "Mã OTP không tồn tại hoặc đã hết hạn. Vui lòng yêu cầu gửi lại."));

        if (!otp.isValid()) {
            throw new ValidationException(buildOtpErrorMessage(otp));
        }

        if (!passwordEncoder.matches(request.getOtp(), otp.getCodeHash())) {
            otp.incrementAttempt();
            otpCodeRepository.save(otp);
            throw new ValidationException("Mã OTP không đúng. Còn " + otp.getRemainingAttempts() + " lần thử.");
        }

        // Validate mật khẩu mới
        List<String> errors = userValidator.validateNewPassword(request.getNewPassword());
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        // OTP hợp lệ → đặt lại mật khẩu đăng nhập
        otp.markAsUsed();
        otpCodeRepository.save(otp);

        user.forceChangePassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Revoke tất cả refresh token (bảo mật: logout khỏi mọi device)
        refreshTokenRepository.deleteByUserId(user.getId());

        logger.info("✅ Password reset successfully for user: {}", user.getUsername());
    }

    // =========================================================
    // RESEND VERIFICATION OTP
    // =========================================================
    @Override
    @Transactional
    public void resendVerificationOtp(ForgotPasswordRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ValidationException("Không tìm thấy tài khoản với email này"));

        if (user.isActive()) {
            throw new ValidationException("Tài khoản đã được xác thực rồi");
        }

        if (user.getStatus() != com.bankapp.bankingapp.domain.model.enums.UserStatus.PENDING) {
            throw new ValidationException("Trạng thái tài khoản không hợp lệ để gửi lại OTP");
        }

        sendOtp(user, OtpType.EMAIL_VERIFICATION);
        logger.info("Resend verification OTP for email: {}", request.getEmail());
    }

    // =========================================================
    // PRIVATE HELPERS
    // =========================================================

    /**
     * Tạo OTP ngẫu nhiên 6 số, hash và lưu vào DB, gửi email
     */
    private void sendOtp(User user, OtpType type) {
        // Xóa OTP cũ của cùng loại
        otpCodeRepository.deleteByUserIdAndType(user.getId(), type);

        // Tạo mã 6 số
        String rawOtp = generateRawOtp();
        String hashedOtp = passwordEncoder.encode(rawOtp);

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(otpExpirationMinutes);
        OtpCode otpCode = new OtpCode(user.getId(), hashedOtp, type, expiresAt);
        otpCodeRepository.save(otpCode);

        // Gửi email (@Async - không block)
        if (type == OtpType.EMAIL_VERIFICATION) {
            emailService.sendEmailVerificationOtp(user.getEmail(), user.getUsername(), rawOtp);
        } else {
            emailService.sendPasswordResetOtp(user.getEmail(), user.getUsername(), rawOtp);
        }
    }

    /**
     * Tạo mã OTP 6 số an toàn bằng SecureRandom
     */
    private String generateRawOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000); // 100000 → 999999
        return String.valueOf(otp);
    }

    /**
     * Lưu refresh token vào DB (xóa token cũ trước để giới hạn 1 session)
     * Có thể điều chỉnh để hỗ trợ multi-device bằng cách không xóa
     */
    private void saveRefreshTokenToDb(User user, String rawToken) {
        refreshTokenRepository.deleteByUserId(user.getId());

        String tokenHash = hashToken(rawToken);
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(
                jwtTokenProvider.getRefreshExpiration() / 1000);

        RefreshToken refreshToken = new RefreshToken(user.getId(), tokenHash, expiresAt);
        refreshTokenRepository.save(refreshToken);
    }

    /**
     * Hash token bằng SHA-256 để lưu vào DB an toàn
     */
    private String hashToken(String rawToken) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /**
     * Build roles string cho JWT claim
     */
    private String buildRolesString(User user) {
        return user.getRoles().stream()
                .map(role -> "ROLE_" + role.getName())
                .collect(java.util.stream.Collectors.joining(","));
    }

    private String buildOtpErrorMessage(OtpCode otp) {
        if (otp.isExpired())
            return "Mã OTP đã hết hạn. Vui lòng yêu cầu gửi lại.";
        if (otp.isLocked())
            return "Đã vượt quá số lần thử. Vui lòng yêu cầu gửi lại mã OTP.";
        if (otp.isUsed())
            return "Mã OTP đã được sử dụng.";
        return "Mã OTP không hợp lệ.";
    }
}
