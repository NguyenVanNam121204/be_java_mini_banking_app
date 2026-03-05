package com.bankapp.bankingapp.domain.model;

import com.bankapp.bankingapp.domain.model.enums.AccountStatus;
import com.bankapp.bankingapp.domain.model.enums.AccountType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Account {

    @Setter
    @EqualsAndHashCode.Include
    private Long id;

    private String accountNumber;
    private Long userId;
    private BigDecimal balance;
    private AccountStatus status;
    private AccountType type;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Account(Long id,
            String accountNumber,
            Long userId,
            BigDecimal balance,
            AccountStatus status,
            AccountType type) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.userId = userId;
        this.balance = balance != null ? balance : BigDecimal.ZERO;
        this.status = status;
        this.type = type;
        this.createdAt = LocalDateTime.now();
    }

    // BUSINESS LOGIC

    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }

    public boolean isClosed() {
        return status == AccountStatus.CLOSED;
    }

    public void lock() {
        if (this.status == AccountStatus.CLOSED) {
            throw new IllegalStateException("Không thể khoá tài khoản đã đóng");
        }
        this.status = AccountStatus.LOCKED;
        updateTimestamp();
    }

    public void unlock() {
        if (this.status == AccountStatus.CLOSED) {
            throw new IllegalStateException("Không thể mở khoá tài khoản đã đóng");
        }
        this.status = AccountStatus.ACTIVE;
        updateTimestamp();
    }

    public void close() {
        if (!this.balance.equals(BigDecimal.ZERO)) {
            throw new IllegalStateException("Không thể đóng tài khoản còn số dư");
        }
        this.status = AccountStatus.CLOSED;
        updateTimestamp();
    }

    public void deposit(BigDecimal amount) {
        if (this.status != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Tài khoản không hoạt động");
        }
        this.balance = this.balance.add(amount);
        updateTimestamp();
    }

    public void withdraw(BigDecimal amount) {
        if (this.status != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Tài khoản không hoạt động");
        }
        if (!hasSufficientBalance(amount)) {
            throw new IllegalStateException("Số dư không đủ");
        }
        this.balance = this.balance.subtract(amount);
        updateTimestamp();
    }

    public boolean hasSufficientBalance(BigDecimal amount) {
        return this.balance.compareTo(amount) >= 0;
    }

    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
}
