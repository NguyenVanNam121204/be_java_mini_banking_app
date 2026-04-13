package com.bankapp.bankingapp.infrastructure.persistence.mapper;

import com.bankapp.bankingapp.domain.model.Transaction;
import com.bankapp.bankingapp.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.stereotype.Component;

@Component
public class TransactionEntityMapper {

    public Transaction toDomain(TransactionEntity entity) {
        if (entity == null) return null;

        Transaction transaction = new Transaction(
                entity.getId(),
                entity.getReferenceNumber(),
                entity.getType(),
                entity.getAmount(),
                entity.getFromAccountId(),
                entity.getToAccountId(),
                entity.getDescription(),
                entity.getInitiatedBy()
        );
        
        transaction.setStatus(entity.getStatus());
        transaction.setCreatedAt(entity.getCreatedAt());
        transaction.setCompletedAt(entity.getCompletedAt());
        
        return transaction;
    }

    public TransactionEntity toEntity(Transaction domain) {
        if (domain == null) return null;

        TransactionEntity entity = new TransactionEntity();
        entity.setId(domain.getId());
        entity.setReferenceNumber(domain.getReferenceNumber());
        entity.setType(domain.getType());
        entity.setAmount(domain.getAmount());
        entity.setStatus(domain.getStatus());
        entity.setFromAccountId(domain.getFromAccountId());
        entity.setToAccountId(domain.getToAccountId());
        entity.setDescription(domain.getDescription());
        entity.setInitiatedBy(domain.getInitiatedBy());
        entity.setCompletedAt(domain.getCompletedAt());
        
        return entity;
    }
}
