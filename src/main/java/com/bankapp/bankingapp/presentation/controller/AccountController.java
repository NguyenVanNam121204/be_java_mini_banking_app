package com.bankapp.bankingapp.presentation.controller;

import com.bankapp.bankingapp.application.dto.request.CreateAccountRequestDto;
import com.bankapp.bankingapp.application.dto.response.AccountResponseDto;
import com.bankapp.bankingapp.application.dto.response.ApiResponseDto;
import com.bankapp.bankingapp.application.interfaces.service.IAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Account Management", description = "APIs quản lý tài khoản dành cho người dùng")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {

    private final IAccountService accountService;

    public AccountController(IAccountService accountService) {
        this.accountService = accountService;
    }

    @Operation(summary = "Tạo tài khoản ngân hàng mới")
    @PostMapping
    public ResponseEntity<ApiResponseDto<AccountResponseDto>> createAccount(
            @Valid @RequestBody CreateAccountRequestDto request) {
        AccountResponseDto response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.created("Tạo tài khoản ngân hàng thành công", response));
    }

    @Operation(summary = "Xem danh sách tài khoản của tôi")
    @GetMapping
    public ResponseEntity<ApiResponseDto<List<AccountResponseDto>>> getMyAccounts() {
        List<AccountResponseDto> accounts = accountService.getMyAccounts();
        return ResponseEntity.ok(ApiResponseDto.success("Lấy danh sách tài khoản thành công", accounts));
    }

    @Operation(summary = "Xem chi tiết một tài khoản")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<AccountResponseDto>> getMyAccountDetails(@PathVariable Long id) {
        AccountResponseDto account = accountService.getMyAccountDetails(id);
        return ResponseEntity.ok(ApiResponseDto.success("Lấy thông tin chi tiết tài khoản thành công", account));
    }
}
