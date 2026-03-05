package com.bankapp.bankingapp.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Request xác thực email bằng OTP sau đăng ký")
public class VerifyEmailRequestDto {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Schema(description = "Email đăng ký", example = "user@example.com")
    private String email;

    @NotBlank(message = "Mã OTP không được để trống")
    @Schema(description = "Mã OTP 6 số nhận qua email", example = "123456")
    private String otp;
}
