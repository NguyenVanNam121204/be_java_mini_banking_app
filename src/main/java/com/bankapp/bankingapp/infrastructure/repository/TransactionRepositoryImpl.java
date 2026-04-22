package com.bankapp.bankingapp.infrastructure.repository;

import com.bankapp.bankingapp.application.interfaces.repository.ITransactionRepository;
import com.bankapp.bankingapp.domain.model.Transaction;
import com.bankapp.bankingapp.domain.model.TransactionEntry;
import com.bankapp.bankingapp.infrastructure.persistence.entity.TransactionEntity;
import com.bankapp.bankingapp.infrastructure.persistence.entity.TransactionEntryEntity;
import com.bankapp.bankingapp.infrastructure.persistence.jpaRepository.TransactionEntryJpaRepository;
import com.bankapp.bankingapp.infrastructure.persistence.jpaRepository.TransactionJpaRepository;
import com.bankapp.bankingapp.infrastructure.persistence.mapper.TransactionEntityMapper;
import com.bankapp.bankingapp.infrastructure.persistence.mapper.TransactionEntryEntityMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public class TransactionRepositoryImpl implements ITransactionRepository {

    private final TransactionJpaRepository transactionJpaRepository;
    private final TransactionEntryJpaRepository transactionEntryJpaRepository;
    private final TransactionEntityMapper transactionEntityMapper;
    private final TransactionEntryEntityMapper transactionEntryEntityMapper;

    public TransactionRepositoryImpl(TransactionJpaRepository transactionJpaRepository, 
                                     TransactionEntryJpaRepository transactionEntryJpaRepository, 
                                     TransactionEntityMapper transactionEntityMapper, 
                                     TransactionEntryEntityMapper transactionEntryEntityMapper) {
        this.transactionJpaRepository = transactionJpaRepository;
        this.transactionEntryJpaRepository = transactionEntryJpaRepository;
        this.transactionEntityMapper = transactionEntityMapper;
        this.transactionEntryEntityMapper = transactionEntryEntityMapper;
    }

    @Override
    public Transaction save(Transaction transaction) {
        TransactionEntity entity = transactionEntityMapper.toEntity(transaction);
        java.util.Objects.requireNonNull(entity, "TransactionEntity must not be null");
        entity = transactionJpaRepository.save(entity);
        return transactionEntityMapper.toDomain(entity);
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        java.util.Objects.requireNonNull(id, "id must not be null");
        return transactionJpaRepository.findById(id).map(transactionEntityMapper::toDomain);
    }

    @Override
    public Optional<Transaction> findByReferenceNumber(String referenceNumber) {
        java.util.Objects.requireNonNull(referenceNumber, "referenceNumber must not be null");
        return transactionJpaRepository.findByReferenceNumber(referenceNumber)
                .map(transactionEntityMapper::toDomain);
    }

    @Override
    public Page<Transaction> findTransactionsByAccountId(Long accountId, Pageable pageable) {
        java.util.Objects.requireNonNull(accountId, "accountId must not be null");
        return transactionJpaRepository.findTransactionsByAccountId(accountId, pageable)
                .map(transactionEntityMapper::toDomain);
    }

    @Override
    public Page<Transaction> findAll(Pageable pageable) {
        return transactionJpaRepository.findAll(pageable)
                .map(transactionEntityMapper::toDomain);
    }

    @Override
    public TransactionEntry saveEntry(TransactionEntry entry) {
        TransactionEntryEntity entity = transactionEntryEntityMapper.toEntity(entry);
        java.util.Objects.requireNonNull(entity, "TransactionEntryEntity must not be null");
        entity = transactionEntryJpaRepository.save(entity);
        return transactionEntryEntityMapper.toDomain(entity);
    }

    @Override
    public List<TransactionEntry> findEntriesByTransactionId(Long transactionId) {
        java.util.Objects.requireNonNull(transactionId, "transactionId must not be null");
        return transactionEntryJpaRepository.findByTransactionId(transactionId).stream()
                .map(transactionEntryEntityMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionEntry> findEntriesByAccountId(Long accountId) {
        java.util.Objects.requireNonNull(accountId, "accountId must not be null");
        return transactionEntryJpaRepository.findByAccountId(accountId).stream()
                .map(transactionEntryEntityMapper::toDomain)
                .collect(Collectors.toList());
    }
}
