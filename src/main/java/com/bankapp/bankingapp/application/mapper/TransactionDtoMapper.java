package com.bankapp.bankingapp.application.mapper;

import com.bankapp.bankingapp.application.dto.response.TransactionResponseDto;
import com.bankapp.bankingapp.application.interfaces.repository.IAccountRepository;
import com.bankapp.bankingapp.application.interfaces.repository.IUserRepository;
import com.bankapp.bankingapp.domain.model.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionDtoMapper {

    private final IAccountRepository accountRepository;
    private final IUserRepository userRepository;

    public TransactionResponseDto toTransactionResponseDto(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        TransactionResponseDto.TransactionResponseDtoBuilder builder = TransactionResponseDto.builder()
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
                .completedAt(transaction.getCompletedAt());

        // Enrich source account details
        if (transaction.getFromAccountId() != null) {
            accountRepository.findById(transaction.getFromAccountId()).ifPresent(acc -> {
                builder.fromAccountNumber(acc.getAccountNumber());
                userRepository.findById(acc.getUserId()).ifPresent(u -> 
                    builder.fromAccountOwner(u.getUsername())
                );
            });
        }

        // Enrich destination account details
        if (transaction.getToAccountId() != null) {
            accountRepository.findById(transaction.getToAccountId()).ifPresent(acc -> {
                builder.toAccountNumber(acc.getAccountNumber());
                userRepository.findById(acc.getUserId()).ifPresent(u -> 
                    builder.toAccountOwner(u.getUsername())
                );
            });
        }

        return builder.build();
    }
}
