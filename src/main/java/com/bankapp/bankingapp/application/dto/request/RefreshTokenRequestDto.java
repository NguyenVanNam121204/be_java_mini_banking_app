package com.bankapp.bankingapp.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Request làm mới access token bằng refresh token")
public class RefreshTokenRequestDto {

    @NotBlank(message = "Refresh token không được để trống")
    @Schema(description = "Refresh token nhận được khi đăng nhập/đăng ký")
    private String refreshToken;
}
