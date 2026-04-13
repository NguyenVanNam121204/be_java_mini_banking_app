package com.bankapp.bankingapp.application.interfaces.repository;

import com.bankapp.bankingapp.domain.model.Account;
import java.util.List;
import java.util.Optional;

public interface IAccountRepository {

    Account save(Account account);
    
    Optional<Account> findById(Long id);
    
    Optional<Account> findByAccountNumber(String accountNumber);
    
    List<Account> findByUserId(Long userId);
    
    boolean existsByAccountNumber(String accountNumber);
    
    List<Account> findAll();
}
