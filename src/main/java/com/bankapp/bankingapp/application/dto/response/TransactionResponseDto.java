package com.bankapp.bankingapp.application.dto.response;

import com.bankapp.bankingapp.domain.model.enums.TransactionStatus;
import com.bankapp.bankingapp.domain.model.enums.TransactionType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class TransactionResponseDto {
    private Long id;
    private String referenceNumber;
    private TransactionType type;
    private BigDecimal amount;
    private TransactionStatus status;
    private Long fromAccountId;
    private String fromAccountNumber;
    private String fromAccountOwner;
    
    private Long toAccountId;
    private String toAccountNumber;
    private String toAccountOwner;
    
    private String description;
    private String initiatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
