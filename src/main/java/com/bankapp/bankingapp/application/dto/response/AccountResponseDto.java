package com.bankapp.bankingapp.application.dto.response;

import com.bankapp.bankingapp.domain.model.enums.AccountStatus;
import com.bankapp.bankingapp.domain.model.enums.AccountType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class AccountResponseDto {
    private Long id;
    private String accountNumber;
    private Long userId;
    private BigDecimal balance;
    private AccountStatus status;
    private AccountType type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
