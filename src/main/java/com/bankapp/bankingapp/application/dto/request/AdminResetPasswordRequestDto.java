package com.bankapp.bankingapp.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminResetPasswordRequestDto {
    @NotBlank(message = "Mật khẩu mới không được để trống")
    private String newPassword;
}
