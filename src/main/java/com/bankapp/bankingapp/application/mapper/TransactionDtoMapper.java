package com.bankapp.bankingapp.application.mapper;

import com.bankapp.bankingapp.application.dto.response.TransactionResponseDto;
import com.bankapp.bankingapp.domain.model.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionDtoMapper {

    public TransactionResponseDto toTransactionResponseDto(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        return TransactionResponseDto.builder()
                .id(transaction.getId())
                .referenceNumber(transaction.getReferenceNumber())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .status(transaction.getStatus())
                .fromAccountId(transaction.getFromAccountId())
                .toAccountId(transaction.getToAccountId())
                .description(transaction.getDescription())
                .initiatedBy(transaction.getInitiatedBy())
                .createdAt(transaction.getCreatedAt())
                .completedAt(transaction.getCompletedAt())
                .build();
    }
}
