package com.bankapp.bankingapp.infrastructure.persistence.mapper;

import com.bankapp.bankingapp.domain.model.Account;
import com.bankapp.bankingapp.infrastructure.persistence.entity.AccountEntity;
import com.bankapp.bankingapp.infrastructure.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class AccountEntityMapper {

    public Account toDomain(AccountEntity entity) {
        if (entity == null) return null;
        
        Account account = new Account(
                entity.getId(),
                entity.getAccountNumber(),
                entity.getUser().getId(),
                entity.getBalance(),
                entity.getStatus(),
                entity.getType()
        );
        
        // Preserve timestamps & version
        account.setVersion(entity.getVersion());
        account.setCreatedAt(entity.getCreatedAt());
        account.setUpdatedAt(entity.getUpdatedAt());
        
        return account;
    }

    public AccountEntity toEntity(Account domain) {
        if (domain == null) return null;
        
        AccountEntity entity = new AccountEntity();
        entity.setId(domain.getId());
        entity.setAccountNumber(domain.getAccountNumber());
        
        UserEntity userEntity = new UserEntity();
        userEntity.setId(domain.getUserId());
        entity.setUser(userEntity);
        
        entity.setBalance(domain.getBalance());
        entity.setStatus(domain.getStatus());
        entity.setType(domain.getType());
        
        if (domain.getVersion() != null) {
            entity.setVersion(domain.getVersion());
        }
        
        // Let entity retain native timestamps via Hibernate, or sync if needed
        return entity;
    }
}
