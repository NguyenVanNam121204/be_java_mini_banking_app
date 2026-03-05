package com.bankapp.bankingapp.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request đăng nhập")
public class LoginRequestDto {

    @NotBlank(message = "Username không được để trống")
    @Schema(description = "Username hoặc email", example = "nambo@gmail.com")
    private String username;

    @NotBlank(message = "Password không được để trống")
    @Schema(description = "Mật khẩu", example = "12122004")
    private String password;
}
