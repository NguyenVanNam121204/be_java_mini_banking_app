package com.bankapp.bankingapp.infrastructure.persistence.jpaRepository;

import com.bankapp.bankingapp.infrastructure.persistence.entity.TransactionEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionEntryJpaRepository extends JpaRepository<TransactionEntryEntity, Long> {
    List<TransactionEntryEntity> findByTransactionId(Long transactionId);
    List<TransactionEntryEntity> findByAccountId(Long accountId);
}
