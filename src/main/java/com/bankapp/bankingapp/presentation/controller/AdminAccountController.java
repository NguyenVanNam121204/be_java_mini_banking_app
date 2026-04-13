package com.bankapp.bankingapp.presentation.controller;

import com.bankapp.bankingapp.application.dto.response.AccountResponseDto;
import com.bankapp.bankingapp.application.dto.response.ApiResponseDto;
import com.bankapp.bankingapp.application.interfaces.service.IAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/accounts")
@Tag(name = "Admin Account Management", description = "APIs quản lý tài khoản dành cho ADMIN")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAccountController {

    private final IAccountService accountService;

    public AdminAccountController(IAccountService accountService) {
        this.accountService = accountService;
    }

    @Operation(summary = "Xem tất cả tài khoản", description = "Lấy danh sách tất cả tài khoản trong hệ thống")
    @GetMapping
    public ResponseEntity<ApiResponseDto<List<AccountResponseDto>>> getAllAccounts() {
        List<AccountResponseDto> accounts = accountService.getAllAccounts();
        return ResponseEntity.ok(ApiResponseDto.success("Lấy danh sách tất cả tài khoản thành công", accounts));
    }

    @Operation(summary = "Khoá tài khoản")
    @PutMapping("/{id}/lock")
    public ResponseEntity<ApiResponseDto<AccountResponseDto>> lockAccount(@PathVariable Long id) {
        AccountResponseDto account = accountService.lockAccount(id);
        return ResponseEntity.ok(ApiResponseDto.success("Tài khoản đã được khoá", account));
    }

    @Operation(summary = "Mở khoá tài khoản")
    @PutMapping("/{id}/unlock")
    public ResponseEntity<ApiResponseDto<AccountResponseDto>> unlockAccount(@PathVariable Long id) {
        AccountResponseDto account = accountService.unlockAccount(id);
        return ResponseEntity.ok(ApiResponseDto.success("Tài khoản đã được mở khoá", account));
    }

    @Operation(summary = "Đóng tài khoản")
    @PutMapping("/{id}/close")
    public ResponseEntity<ApiResponseDto<AccountResponseDto>> closeAccount(@PathVariable Long id) {
        AccountResponseDto account = accountService.closeAccount(id);
        return ResponseEntity.ok(ApiResponseDto.success("Tài khoản đã được đóng vĩnh viễn", account));
    }
}
