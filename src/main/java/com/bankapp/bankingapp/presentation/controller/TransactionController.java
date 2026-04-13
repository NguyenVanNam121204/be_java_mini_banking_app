package com.bankapp.bankingapp.presentation.controller;

import com.bankapp.bankingapp.application.dto.request.DepositRequestDto;
import com.bankapp.bankingapp.application.dto.request.TransferRequestDto;
import com.bankapp.bankingapp.application.dto.request.WithdrawRequestDto;
import com.bankapp.bankingapp.application.dto.response.ApiResponseDto;
import com.bankapp.bankingapp.application.dto.response.PageResponseDto;
import com.bankapp.bankingapp.application.dto.response.TransactionResponseDto;
import com.bankapp.bankingapp.application.interfaces.service.ITransactionService;
import com.bankapp.bankingapp.infrastructure.security.idempotency.Idempotent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transaction", description = "Các tính năng giao dịch tài chính cốt lõi (Nạp, Rút, Chuyển khoản)")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final ITransactionService transactionService;

    public TransactionController(ITransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Operation(summary = "Nạp tiền (Deposit)", description = "Nạp một số tiền vào tài khoản chỉ định. Yêu cầu Header Idempotency-Key.")
    @Parameter(name = "Idempotency-Key", description = "Khóa chống trùng lặp giao dịch (VD: UUID)", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
    @Idempotent
    @PostMapping("/deposit")
    public ResponseEntity<ApiResponseDto<TransactionResponseDto>> deposit(
            @RequestHeader(value = "Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody DepositRequestDto request) {
        TransactionResponseDto response = transactionService.deposit(request);
        return ResponseEntity.ok(ApiResponseDto.success("Nạp tiền thành công", response));
    }

    @Operation(summary = "Rút tiền (Withdraw)", description = "Rút tiền từ tài khoản kèm xác thực mã PIN. Yêu cầu Header Idempotency-Key.")
    @Parameter(name = "Idempotency-Key", description = "Khóa chống trùng lặp giao dịch (VD: UUID)", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
    @Idempotent
    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponseDto<TransactionResponseDto>> withdraw(
            @RequestHeader(value = "Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody WithdrawRequestDto request) {
        TransactionResponseDto response = transactionService.withdraw(request);
        return ResponseEntity.ok(ApiResponseDto.success("Rút tiền thành công", response));
    }

    @Operation(summary = "Chuyển khoản (Transfer)", description = "Chuyển tiền từ tài khoản nguồn sang tài khoản đích. Yêu cầu Header Idempotency-Key.")
    @Parameter(name = "Idempotency-Key", description = "Khóa chống trùng lặp giao dịch (VD: UUID)", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
    @Idempotent
    @PostMapping("/transfer")
    public ResponseEntity<ApiResponseDto<TransactionResponseDto>> transfer(
            @RequestHeader(value = "Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody TransferRequestDto request) {
        TransactionResponseDto response = transactionService.transfer(request);
        return ResponseEntity.ok(ApiResponseDto.success("Chuyển khoản thành công", response));
    }

    @Operation(summary = "Lịch sử giao dịch", description = "Lấy lịch sử giao dịch của một tài khoản (có phân trang)")
    @GetMapping("/history/{accountId}")
    public ResponseEntity<ApiResponseDto<PageResponseDto<TransactionResponseDto>>> getHistory(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponseDto<TransactionResponseDto> response = transactionService.getTransactionHistory(accountId, page, size);
        return ResponseEntity.ok(ApiResponseDto.success("Lấy lịch sử giao dịch thành công", response));
    }
}
