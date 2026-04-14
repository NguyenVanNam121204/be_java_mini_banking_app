package com.bankapp.bankingapp.infrastructure.repository;

import com.bankapp.bankingapp.application.interfaces.repository.IRoleRepository;
import com.bankapp.bankingapp.domain.model.Role;
import com.bankapp.bankingapp.infrastructure.persistence.entity.RoleEntity;
import com.bankapp.bankingapp.infrastructure.persistence.mapper.RoleEntityMapper;
import com.bankapp.bankingapp.infrastructure.persistence.jpaRepository.RoleJpaRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Repository
public class RoleRepositoryImpl implements IRoleRepository {

    private final RoleJpaRepository jpaRepository;
    private final RoleEntityMapper mapper;

    public RoleRepositoryImpl(RoleJpaRepository jpaRepository, RoleEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public Role save(@NotNull Role role) {
        RoleEntity entity = mapper.toEntity(role);
        RoleEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(Objects.requireNonNull(saved));
    }

    @Override
    public Optional<Role> findById(@NotNull Long id) {
        return jpaRepository.findById(Objects.requireNonNull(id))
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Role> findByName(@NotNull String name) {
        return jpaRepository.findByName(name)
                .map(mapper::toDomain);
    }
}
