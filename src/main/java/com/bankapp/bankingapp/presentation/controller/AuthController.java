package com.bankapp.bankingapp.presentation.controller;

import com.bankapp.bankingapp.application.dto.request.*;
import com.bankapp.bankingapp.application.dto.response.ApiResponseDto;
import com.bankapp.bankingapp.application.dto.response.AuthResponseDto;
import com.bankapp.bankingapp.application.dto.response.ErrorResponseDto;
import com.bankapp.bankingapp.application.interfaces.service.IAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "APIs xác thực người dùng (Đăng ký, Đăng nhập, OTP, Quên mật khẩu)")
public class AuthController {

        private final IAuthService authService;

        public AuthController(IAuthService authService) {
                this.authService = authService;
        }

        // =========================================================
        // REGISTER
        // =========================================================
        @Operation(summary = "Đăng ký tài khoản mới", description = "Tạo tài khoản mới với status PENDING. Hệ thống gửi mã OTP 6 số về email để xác thực.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Đăng ký thành công, OTP đã gửi về email", content = @Content(schema = @Schema(implementation = AuthResponseDto.class))),
                        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc email/username đã tồn tại", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
        })
        @PostMapping("/register")
        public ResponseEntity<ApiResponseDto<AuthResponseDto>> register(
                        @Valid @RequestBody RegisterRequestDto request) {
                AuthResponseDto response = authService.register(request);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponseDto.created(
                                                "Đăng ký thành công. Vui lòng kiểm tra email để nhận mã OTP xác thực tài khoản.",
                                                response));
        }

        // =========================================================
        // LOGIN
        // =========================================================
        @Operation(summary = "Đăng nhập", description = "Đăng nhập bằng username/email và password. Tài khoản phải đã xác thực email (ACTIVE).")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Đăng nhập thành công", content = @Content(schema = @Schema(implementation = AuthResponseDto.class))),
                        @ApiResponse(responseCode = "401", description = "Username/password không đúng", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))),
                        @ApiResponse(responseCode = "400", description = "Tài khoản chưa xác thực email", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
        })
        @PostMapping("/login")
        public ResponseEntity<ApiResponseDto<AuthResponseDto>> login(
                        @Valid @RequestBody LoginRequestDto request) {
                AuthResponseDto response = authService.login(request);
                return ResponseEntity.ok(ApiResponseDto.success("Đăng nhập thành công", response));
        }

        // =========================================================
        // REFRESH TOKEN
        // =========================================================
        @Operation(summary = "Làm mới Access Token", description = "Dùng refresh token để lấy access token mới và refresh token mới (Token Rotation). Refresh token cũ bị vô hiệu hóa.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Token mới được tạo thành công", content = @Content(schema = @Schema(implementation = AuthResponseDto.class))),
                        @ApiResponse(responseCode = "400", description = "Refresh token không hợp lệ hoặc đã hết hạn", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
        })
        @PostMapping("/refresh-token")
        public ResponseEntity<ApiResponseDto<AuthResponseDto>> refreshToken(
                        @Valid @RequestBody RefreshTokenRequestDto request) {
                AuthResponseDto response = authService.refreshToken(request);
                return ResponseEntity.ok(ApiResponseDto.success("Token đã được làm mới thành công", response));
        }

        // =========================================================
        // VERIFY EMAIL
        // =========================================================
        @Operation(summary = "Xác thực email bằng OTP", description = "Nhập mã OTP 6 số đã gửi qua email để kích hoạt tài khoản (PENDING → ACTIVE).")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Xác thực thành công, tài khoản đã được kích hoạt"),
                        @ApiResponse(responseCode = "400", description = "OTP sai, hết hạn hoặc vượt quá số lần thử", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
        })
        @PostMapping("/verify-email")
        public ResponseEntity<ApiResponseDto<Void>> verifyEmail(
                        @Valid @RequestBody VerifyEmailRequestDto request) {
                authService.verifyEmail(request);
                return ResponseEntity
                                .ok(ApiResponseDto.success("Xác thực email thành công! Bạn có thể đăng nhập.", null));
        }

        // =========================================================
        // RESEND VERIFICATION OTP
        // =========================================================
        @Operation(summary = "Gửi lại OTP xác thực email", description = "Gửi lại mã OTP mới khi OTP cũ đã hết hạn hoặc bị lock. Mã OTP cũ bị hủy.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "OTP mới đã được gửi về email"),
                        @ApiResponse(responseCode = "400", description = "Tài khoản không ở trạng thái PENDING", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
        })
        @PostMapping("/resend-verification")
        public ResponseEntity<ApiResponseDto<Void>> resendVerification(
                        @Valid @RequestBody ForgotPasswordRequestDto request) {
                authService.resendVerificationOtp(request);
                return ResponseEntity.ok(ApiResponseDto.success("Mã OTP xác thực mới đã được gửi về email.", null));
        }

        // =========================================================
        // FORGOT PASSWORD
        // =========================================================
        @Operation(summary = "Quên mật khẩu - gửi OTP", description = "Nhập email tài khoản. Hệ thống gửi mã OTP 6 số về email để đặt lại mật khẩu. "
                        +
                        "API luôn trả về thành công để không lộ thông tin email tồn tại.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Yêu cầu đã được xử lý (OTP gửi nếu email tồn tại)")
        })
        @PostMapping("/forgot-password")
        public ResponseEntity<ApiResponseDto<Void>> forgotPassword(
                        @Valid @RequestBody ForgotPasswordRequestDto request) {
                authService.forgotPassword(request);
                return ResponseEntity.ok(ApiResponseDto.success(
                                "Nếu email tồn tại trong hệ thống, mã OTP đã được gửi về email của bạn.", null));
        }

        // =========================================================
        // RESET PASSWORD
        // =========================================================
        @Operation(summary = "Đặt lại mật khẩu bằng OTP", description = "Xác minh OTP và đặt mật khẩu mới. Tất cả phiên đăng nhập sẽ bị đăng xuất.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Mật khẩu đổi thành công"),
                        @ApiResponse(responseCode = "400", description = "OTP sai, hết hạn hoặc mật khẩu mới không hợp lệ", content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
        })
        @PostMapping("/reset-password")
        public ResponseEntity<ApiResponseDto<Void>> resetPassword(
                        @Valid @RequestBody ResetPasswordRequestDto request) {
                authService.resetPassword(request);
                return ResponseEntity.ok(ApiResponseDto.success(
                                "Mật khẩu đã được đặt lại thành công. Vui lòng đăng nhập lại.", null));
        }
}
