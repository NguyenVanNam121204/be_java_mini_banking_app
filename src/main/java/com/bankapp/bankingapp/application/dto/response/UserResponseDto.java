package com.bankapp.bankingapp.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response DTO cho thông tin user")
public class UserResponseDto {

    @Schema(description = "ID của user", example = "1")
    private Long id;

    @Schema(description = "Username", example = "admin")
    private String username;

    @Schema(description = "Email", example = "nambo@gmail.com")
    private String email;

    @Schema(description = "Trạng thái tài khoản", example = "ACTIVE")
    private String status;

    @Schema(description = "Danh sách roles của user", example = "[\"ADMIN\", \"USER\"]")
    private Set<String> roles;

    @Schema(description = "Thời gian tạo tài khoản")
    private LocalDateTime createdAt;

    @Schema(description = "Thời gian cập nhật cuối")
    private LocalDateTime updatedAt;
}
