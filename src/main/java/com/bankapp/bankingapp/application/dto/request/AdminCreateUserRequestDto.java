package com.bankapp.bankingapp.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.Set;

@Data
public class AdminCreateUserRequestDto {
    @NotBlank(message = "Username không được để trống")
    private String username;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
    
    private Set<String> roles;
}
