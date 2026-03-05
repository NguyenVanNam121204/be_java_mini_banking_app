package com.bankapp.bankingapp.infrastructure.persistence.mapper;

import com.bankapp.bankingapp.domain.model.User;
import com.bankapp.bankingapp.infrastructure.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserMapper {

    private final RoleMapper roleMapper;

    public UserMapper(RoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    public User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        User user = new User(
                entity.getId(),
                entity.getUsername(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getTransactionPinHash(),
                entity.getStatus()
        );

        // Set timestamps from entity (important: preserve original timestamps!)
        user.setCreatedAt(entity.getCreatedAt());
        user.setUpdatedAt(entity.getUpdatedAt());

        // Set roles
        if (entity.getRoles() != null) {
            entity.getRoles().forEach(roleEntity -> 
                user.addRole(roleMapper.toDomain(roleEntity))
            );
        }

        return user;
    }

    public UserEntity toEntity(User domain) {
        if (domain == null) {
            return null;
        }

        UserEntity entity = new UserEntity();
        entity.setId(domain.getId());
        entity.setUsername(domain.getUsername());
        entity.setEmail(domain.getEmail());
        entity.setPasswordHash(domain.getPasswordHash());
        entity.setTransactionPinHash(domain.getTransactionPinHash());
        entity.setPinFailedAttempts(domain.getPinFailedAttempts());
        entity.setPinLockedUntil(domain.getPinLockedUntil());
        entity.setStatus(domain.getStatus());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());

        // Set roles
        if (domain.getRoles() != null) {
            entity.setRoles(
                domain.getRoles().stream()
                    .map(roleMapper::toEntity)
                    .collect(Collectors.toSet())
            );
        }

        return entity;
    }

    public void updateEntity(User domain, UserEntity entity) {
        if (domain == null || entity == null) {
            return;
        }

        entity.setUsername(domain.getUsername());
        entity.setEmail(domain.getEmail());
        entity.setPasswordHash(domain.getPasswordHash());
        entity.setTransactionPinHash(domain.getTransactionPinHash());
        entity.setPinFailedAttempts(domain.getPinFailedAttempts());
        entity.setPinLockedUntil(domain.getPinLockedUntil());
        entity.setStatus(domain.getStatus());

        // Update roles
        if (domain.getRoles() != null) {
            entity.getRoles().clear();
            entity.getRoles().addAll(
                domain.getRoles().stream()
                    .map(roleMapper::toEntity)
                    .collect(Collectors.toSet())
            );
        }
    }
}
