package com.bankapp.bankingapp.infrastructure.persistence.mapper;

import com.bankapp.bankingapp.domain.model.TransactionEntry;
import com.bankapp.bankingapp.infrastructure.persistence.entity.TransactionEntryEntity;
import org.springframework.stereotype.Component;

@Component
public class TransactionEntryEntityMapper {

    public TransactionEntry toDomain(TransactionEntryEntity entity) {
        if (entity == null) return null;

        TransactionEntry transactionEntry = new TransactionEntry(
                entity.getId(),
                entity.getTransactionId(),
                entity.getAccountId(),
                entity.getEntryType(),
                entity.getAmount(),
                entity.getBalanceBefore(),
                entity.getBalanceAfter()
        );
        
        // Preserve timestamp if we add a setter to TransactionEntry
        // But let's assume it's created automatically. For reading history, it's better to preserve exact timestamp.
        return transactionEntry;
    }

    public TransactionEntryEntity toEntity(TransactionEntry domain) {
        if (domain == null) return null;

        TransactionEntryEntity entity = new TransactionEntryEntity();
        entity.setId(domain.getId());
        entity.setTransactionId(domain.getTransactionId());
        entity.setAccountId(domain.getAccountId());
        entity.setEntryType(domain.getEntryType());
        entity.setAmount(domain.getAmount());
        entity.setBalanceBefore(domain.getBalanceBefore());
        entity.setBalanceAfter(domain.getBalanceAfter());
        
        return entity;
    }
}
