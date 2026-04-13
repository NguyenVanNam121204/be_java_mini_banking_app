package com.bankapp.bankingapp.infrastructure.repository;

import com.bankapp.bankingapp.application.interfaces.repository.IAccountRepository;
import com.bankapp.bankingapp.domain.model.Account;
import com.bankapp.bankingapp.infrastructure.persistence.entity.AccountEntity;
import com.bankapp.bankingapp.infrastructure.persistence.jpaRepository.AccountJpaRepository;
import com.bankapp.bankingapp.infrastructure.persistence.mapper.AccountEntityMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class AccountRepositoryImpl implements IAccountRepository {

    private final AccountJpaRepository accountJpaRepository;
    private final AccountEntityMapper accountEntityMapper;

    public AccountRepositoryImpl(AccountJpaRepository accountJpaRepository, AccountEntityMapper accountEntityMapper) {
        this.accountJpaRepository = accountJpaRepository;
        this.accountEntityMapper = accountEntityMapper;
    }

    @Override
    public Account save(Account account) {
        AccountEntity entity = accountEntityMapper.toEntity(account);
        java.util.Objects.requireNonNull(entity, "AccountEntity must not be null");
        entity = accountJpaRepository.save(entity);
        return accountEntityMapper.toDomain(entity);
    }

    @Override
    public Optional<Account> findById(Long id) {
        java.util.Objects.requireNonNull(id, "id must not be null");
        return accountJpaRepository.findById(id)
                .map(accountEntityMapper::toDomain);
    }

    @Override
    public Optional<Account> findByAccountNumber(String accountNumber) {
        return accountJpaRepository.findByAccountNumber(accountNumber)
                .map(accountEntityMapper::toDomain);
    }

    @Override
    public List<Account> findByUserId(Long userId) {
        return accountJpaRepository.findByUserId(userId).stream()
                .map(accountEntityMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByAccountNumber(String accountNumber) {
        return accountJpaRepository.existsByAccountNumber(accountNumber);
    }

    @Override
    public List<Account> findAll() {
        return accountJpaRepository.findAll().stream()
                .map(accountEntityMapper::toDomain)
                .collect(Collectors.toList());
    }
}
