package com.bankapp.bankingapp.infrastructure.repository;

import com.bankapp.bankingapp.application.interfaces.repository.PermissionRepository;
import com.bankapp.bankingapp.domain.model.Permission;
import com.bankapp.bankingapp.infrastructure.persistence.entity.PermissionEntity;
import com.bankapp.bankingapp.infrastructure.persistence.mapper.PermissionMapper;
import com.bankapp.bankingapp.infrastructure.persistence.repository.PermissionJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@SuppressWarnings("null")
public class PermissionRepositoryImpl implements PermissionRepository {

    private final PermissionJpaRepository jpaRepository;
    private final PermissionMapper mapper;

    public PermissionRepositoryImpl(PermissionJpaRepository jpaRepository, PermissionMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Permission save(Permission permission) {
        PermissionEntity entity = mapper.toEntity(permission);
        PermissionEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Permission> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Permission> findByName(String name) {
        return jpaRepository.findByName(name)
                .map(mapper::toDomain);
    }
}
