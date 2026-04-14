package com.bankapp.bankingapp.infrastructure.repository;

import com.bankapp.bankingapp.application.interfaces.repository.IPermissionRepository;
import com.bankapp.bankingapp.domain.model.Permission;
import com.bankapp.bankingapp.infrastructure.persistence.entity.PermissionEntity;
import com.bankapp.bankingapp.infrastructure.persistence.mapper.PermissionEntityMapper;
import com.bankapp.bankingapp.infrastructure.persistence.jpaRepository.PermissionJpaRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Repository
public class PermissionRepositoryImpl implements IPermissionRepository {

    private final PermissionJpaRepository jpaRepository;
    private final PermissionEntityMapper mapper;

    public PermissionRepositoryImpl(PermissionJpaRepository jpaRepository, PermissionEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Permission save(@NotNull Permission permission) {
        PermissionEntity entity = mapper.toEntity(permission);
        PermissionEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(Objects.requireNonNull(saved));
    }

    @Override
    public Optional<Permission> findById(@NotNull Long id) {
        return jpaRepository.findById(Objects.requireNonNull(id))
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Permission> findByName(@NotNull String name) {
        return jpaRepository.findByName(name)
                .map(mapper::toDomain);
    }
}
