package com.bankapp.bankingapp.application.interfaces.repository;

import com.bankapp.bankingapp.domain.model.Transaction;
import com.bankapp.bankingapp.domain.model.TransactionEntry;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ITransactionRepository {

    Transaction save(Transaction transaction);
    
    Optional<Transaction> findById(Long id);
    
    Optional<Transaction> findByReferenceNumber(String referenceNumber);
    
    Page<Transaction> findTransactionsByAccountId(Long accountId, Pageable pageable);
    
    // Entries
    TransactionEntry saveEntry(TransactionEntry entry);
    
    List<TransactionEntry> findEntriesByTransactionId(Long transactionId);
    
    List<TransactionEntry> findEntriesByAccountId(Long accountId);
}
