package com.bankapp.bankingapp.infrastructure.repository;

import com.bankapp.bankingapp.application.interfaces.repository.IUserRepository;
import com.bankapp.bankingapp.domain.model.User;
import com.bankapp.bankingapp.infrastructure.persistence.entity.UserEntity;
import com.bankapp.bankingapp.infrastructure.persistence.mapper.UserEntityMapper;
import com.bankapp.bankingapp.infrastructure.persistence.jpaRepository.UserJpaRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class UserRepositoryImpl implements IUserRepository {

    private final UserJpaRepository jpaRepository;
    private final UserEntityMapper mapper;

    public UserRepositoryImpl(UserJpaRepository jpaRepository, UserEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public User save(@NotNull User user) {
        UserEntity entity;

        if (user.getId() != null) {
            // Update existing
            Long userId = user.getId();
            entity = jpaRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            mapper.updateEntity(user, entity);
        } else {
            // Create new
            entity = mapper.toEntity(user);
        }

        UserEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(Objects.requireNonNull(saved));
    }

    @Override
    public Optional<User> findById(@NotNull Long id) {
        return jpaRepository.findById(Objects.requireNonNull(id))
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(@NotNull String username) {
        return jpaRepository.findByUsername(username)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(@NotNull String email) {
        return jpaRepository.findByEmail(email)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByUsername(@NotNull String username) {
        return jpaRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(@NotNull String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public List<User> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public Page<User> findAllPaginated(@NonNull Pageable pageable, String keyword) {
        String safeKeyword = (keyword != null) ? keyword.trim() : "";
        return jpaRepository.searchByKeyword(safeKeyword, pageable)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional
    public void delete(@NotNull User user) {
        if (user.getId() != null) {
            @NotNull Long userId = Objects.requireNonNull(user.getId());
            jpaRepository.deleteById(userId);
        }
    }
}
