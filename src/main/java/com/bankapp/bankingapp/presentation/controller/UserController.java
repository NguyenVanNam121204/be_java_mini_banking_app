package com.bankapp.bankingapp.presentation.controller;

import com.bankapp.bankingapp.application.dto.response.ApiResponseDto;
import com.bankapp.bankingapp.application.dto.response.UserResponseDto;
import com.bankapp.bankingapp.application.interfaces.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@Tag(name = "User Management", description = "APIs quản lý người dùng")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final IUserService userService;

    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Lấy danh sách users (paginated)", description = "Lấy danh sách users trong hệ thống với pagination (Chỉ dành cho ADMIN)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập (cần role ADMIN)")
    })
    @GetMapping("/api/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<Page<UserResponseDto>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponseDto> users = userService.getAllUsersPaginated(pageable);
        return ResponseEntity.ok(ApiResponseDto.success("Lấy danh sách users thành công", users));
    }

    @Operation(summary = "Lấy thông tin profile", description = "Lấy thông tin profile của user hiện tại")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công", content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @GetMapping("/api/users/profile")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> getCurrentUserProfile() {
        UserResponseDto user = userService.getCurrentUserProfile();
        return ResponseEntity.ok(ApiResponseDto.success("Lấy thông tin profile thành công", user));
    }

    @Operation(summary = "Đổi mật khẩu", description = "Đổi mật khẩu cho user đang đăng nhập")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đổi mật khẩu thành công"),
            @ApiResponse(responseCode = "400", description = "Mật khẩu cũ không đúng hoặc mật khẩu mới không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @PostMapping("/api/users/change-password")
    public ResponseEntity<ApiResponseDto<Void>> changePassword(
            @Valid @RequestBody com.bankapp.bankingapp.application.dto.request.ChangePasswordRequestDto request) {
        userService.changePassword(request);
        return ResponseEntity.ok(ApiResponseDto.success("Đổi mật khẩu thành công", null));
    }

    @Operation(summary = "Thiết lập mã PIN", description = "Thiết lập mã PIN giao dịch lần đầu cho user đang đăng nhập")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thiết lập mã PIN thành công"),
            @ApiResponse(responseCode = "400", description = "Đã có mã PIN hoặc mã PIN không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @PostMapping("/api/users/setup-pin")
    public ResponseEntity<ApiResponseDto<Void>> setupPin(
            @Valid @RequestBody com.bankapp.bankingapp.application.dto.request.SetupPinRequestDto request) {
        userService.setupPin(request);
        return ResponseEntity.ok(ApiResponseDto.success("Thiết lập mã PIN thành công", null));
    }

    @Operation(summary = "Đổi mã PIN", description = "Đổi mã PIN giao dịch cho user đang đăng nhập")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đổi mã PIN thành công"),
            @ApiResponse(responseCode = "400", description = "Mã PIN cũ không đúng hoặc mã PIN mới không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @PostMapping("/api/users/change-pin")
    public ResponseEntity<ApiResponseDto<Void>> changePin(
            @Valid @RequestBody com.bankapp.bankingapp.application.dto.request.ChangePinRequestDto request) {
        userService.changePin(request);
        return ResponseEntity.ok(ApiResponseDto.success("Đổi mã PIN thành công", null));
    }
}
