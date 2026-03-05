package com.bankapp.bankingapp.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request đăng ký tài khoản mới")
public class RegisterRequestDto {

    @NotBlank(message = "Username không được để trống")
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,20}$", message = "Username phải từ 3-20 ký tự, chỉ chứa chữ, số và dấu gạch dưới")
    @Schema(description = "Tên đăng nhập (3-20 ký tự, chỉ chữ, số, gạch dưới)", example = "john_doe")
    private String username;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Schema(description = "Địa chỉ email", example = "john@example.com")
    private String email;

    @NotBlank(message = "Password không được để trống")
    @Size(min = 8, message = "Password phải có ít nhất 8 ký tự")
    @Schema(description = "Mật khẩu (ít nhất 8 ký tự)", example = "password123")
    private String password;
}
