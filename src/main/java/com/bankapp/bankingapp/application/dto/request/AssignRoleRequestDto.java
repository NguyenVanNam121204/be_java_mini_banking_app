package com.bankapp.bankingapp.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AssignRoleRequestDto {
    @NotBlank(message = "Role name không được để trống")
    private String roleName;
}
