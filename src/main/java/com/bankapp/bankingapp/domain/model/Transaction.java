package com.bankapp.bankingapp.domain.model;

import com.bankapp.bankingapp.domain.model.enums.TransactionStatus;
import com.bankapp.bankingapp.domain.model.enums.TransactionType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Transaction {

    @Setter
    @EqualsAndHashCode.Include
    private Long id;

    private String referenceNumber;
    private TransactionType type;
    private BigDecimal amount;
    @Setter
    private TransactionStatus status;

    private Long fromAccountId;
    private Long toAccountId;

    private String description;
    private String initiatedBy;  // Username hoặc userId

    @Setter
    private LocalDateTime createdAt;
    
    @Setter
    private LocalDateTime completedAt;

    public Transaction(Long id, String referenceNumber, TransactionType type, BigDecimal amount, Long fromAccountId, Long toAccountId, String description, String initiatedBy) {
        this.id = id;
        this.referenceNumber = referenceNumber;
        this.type = type;
        this.amount = amount;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.description = description;
        this.initiatedBy = initiatedBy;
        this.status = TransactionStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    // BUSINESS LOGIC

    public void complete() {
        if (this.status != TransactionStatus.PENDING) {
            throw new IllegalStateException("Chỉ có thể hoàn thành giao dịch đang pending");
        }
        this.status = TransactionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void fail(String reason) {
        if (this.status != TransactionStatus.PENDING) {
            throw new IllegalStateException("Chỉ có thể fail giao dịch đang pending");
        }
        this.status = TransactionStatus.FAILED;
        this.description = (this.description != null ? this.description + " | " : "") + "Failed: " + reason;
        this.completedAt = LocalDateTime.now();
    }

    public boolean isPending() {
        return status == TransactionStatus.PENDING;
    }

    public boolean isCompleted() {
        return status == TransactionStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == TransactionStatus.FAILED;
    }

    public boolean isTransfer() {
        return type == TransactionType.TRANSFER;
    }

    public boolean isDeposit() {
        return type == TransactionType.DEPOSIT;
    }

    public boolean isWithdraw() {
        return type == TransactionType.WITHDRAW;
    }
}
