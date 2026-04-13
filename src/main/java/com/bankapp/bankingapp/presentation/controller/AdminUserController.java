package com.bankapp.bankingapp.presentation.controller;

import com.bankapp.bankingapp.application.dto.request.AdminCreateUserRequestDto;
import com.bankapp.bankingapp.application.dto.request.AdminResetPasswordRequestDto;
import com.bankapp.bankingapp.application.dto.request.AssignRoleRequestDto;
import com.bankapp.bankingapp.application.dto.response.ApiResponseDto;
import com.bankapp.bankingapp.application.dto.response.UserResponseDto;
import com.bankapp.bankingapp.application.interfaces.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "Admin User Management", description = "Các API dành cho Admin quản lý tài khoản người dùng")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final IUserService userService;

    public AdminUserController(IUserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Tạo user mới thủ công", description = "Admin tạo tài khoản user trực tiếp không cần đăng ký kích hoạt email")
    @PostMapping("/")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> createUser(@Valid @RequestBody AdminCreateUserRequestDto request) {
        UserResponseDto response = userService.createUserByAdmin(request);
        return ResponseEntity.ok(ApiResponseDto.success("Tạo user thành công", response));
    }

    @Operation(summary = "Khóa User", description = "Khóa quyền truy cập của một người dùng (Status = LOCKED)")
    @PostMapping("/{userId}/lock")
    public ResponseEntity<ApiResponseDto<Void>> lockUser(@PathVariable Long userId) {
        userService.lockUser(userId);
        return ResponseEntity.ok(ApiResponseDto.success("Đã khóa user thành công", null));
    }

    @Operation(summary = "Mở khóa User", description = "Mở khóa cho người dùng (Status = ACTIVE)")
    @PostMapping("/{userId}/unlock")
    public ResponseEntity<ApiResponseDto<Void>> unlockUser(@PathVariable Long userId) {
        userService.unlockUser(userId);
        return ResponseEntity.ok(ApiResponseDto.success("Mở khóa user thành công", null));
    }

    @Operation(summary = "Gán quyền (Role) cho User", description = "Thêm Role cho người dùng hiện tại (Ví dụ: ROLE_ADMIN)")
    @PostMapping("/{userId}/roles")
    public ResponseEntity<ApiResponseDto<Void>> assignRole(
            @PathVariable Long userId,
            @Valid @RequestBody AssignRoleRequestDto request) {
        userService.assignRole(userId, request.getRoleName());
        return ResponseEntity.ok(ApiResponseDto.success("Gán quyền thành công", null));
    }

    @Operation(summary = "Force Reset Password", description = "Bắt buộc đặt lại mật khẩu cho user")
    @PostMapping("/{userId}/force-reset-password")
    public ResponseEntity<ApiResponseDto<Void>> forceResetPassword(
            @PathVariable Long userId,
            @Valid @RequestBody AdminResetPasswordRequestDto request) {
        userService.forceResetPassword(userId, request.getNewPassword());
        return ResponseEntity.ok(ApiResponseDto.success("Đặt lại mật khẩu thành công", null));
    }
}
