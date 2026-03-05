package com.bankapp.bankingapp.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Request đặt lại mật khẩu bằng OTP")
public class ResetPasswordRequestDto {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Schema(description = "Email tài khoản", example = "user@example.com")
    private String email;

    @NotBlank(message = "Mã OTP không được để trống")
    @Schema(description = "Mã OTP 6 số nhận qua email", example = "654321")
    private String otp;

    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Size(min = 8, message = "Mật khẩu mới phải có ít nhất 8 ký tự")
    @Schema(description = "Mật khẩu mới (tối thiểu 8 ký tự)", example = "NewPass@2025")
    private String newPassword;
}
