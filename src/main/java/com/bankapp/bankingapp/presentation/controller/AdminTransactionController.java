package com.bankapp.bankingapp.presentation.controller;

import com.bankapp.bankingapp.application.dto.response.ApiResponseDto;
import com.bankapp.bankingapp.application.dto.response.PageResponseDto;
import com.bankapp.bankingapp.application.dto.response.TransactionResponseDto;
import com.bankapp.bankingapp.application.interfaces.service.ITransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/transactions")
@Tag(name = "Admin Transaction Management", description = "Các API giám sát giao dịch dành cho ADMIN")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTransactionController {

    private final ITransactionService transactionService;

    public AdminTransactionController(ITransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Operation(summary = "Xem tất cả giao dịch", description = "Lấy danh sách tất cả các lệnh nạp, rút, chuyển tiền trên toàn hệ thống")
    @GetMapping
    public ResponseEntity<ApiResponseDto<PageResponseDto<TransactionResponseDto>>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponseDto<TransactionResponseDto> response = transactionService.getAllTransactions(page, size);
        return ResponseEntity.ok(ApiResponseDto.success("Lấy danh sách tất cả giao dịch thành công", response));
    }

    @Operation(summary = "Duyệt giao dịch", description = "Duyệt giao dịch PENDING (giá trị lớn)")
    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponseDto<TransactionResponseDto>> approveTransaction(@PathVariable Long id) {
        TransactionResponseDto response = transactionService.approveTransaction(id);
        return ResponseEntity.ok(ApiResponseDto.success("Đã duyệt giao dịch thành công", response));
    }

    @Operation(summary = "Từ chối giao dịch", description = "Từ chối giao dịch PENDING (giá trị lớn)")
    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponseDto<TransactionResponseDto>> rejectTransaction(@PathVariable Long id) {
        TransactionResponseDto response = transactionService.rejectTransaction(id);
        return ResponseEntity.ok(ApiResponseDto.success("Đã từ chối giao dịch", response));
    }
}
