package com.bankapp.bankingapp.infrastructure.persistence.jpaRepository;

import com.bankapp.bankingapp.infrastructure.persistence.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountJpaRepository extends JpaRepository<AccountEntity, Long> {
    
    Optional<AccountEntity> findByAccountNumber(String accountNumber);
    
    List<AccountEntity> findByUserId(Long userId);

    boolean existsByAccountNumber(String accountNumber);
}
