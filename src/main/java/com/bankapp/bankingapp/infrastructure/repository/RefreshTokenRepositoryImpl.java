package com.bankapp.bankingapp.infrastructure.repository;

import com.bankapp.bankingapp.application.interfaces.repository.IRefreshTokenRepository;
import com.bankapp.bankingapp.domain.model.RefreshToken;
import com.bankapp.bankingapp.infrastructure.persistence.entity.RefreshTokenEntity;
import com.bankapp.bankingapp.infrastructure.persistence.mapper.RefreshTokenEntityMapper;
import com.bankapp.bankingapp.infrastructure.persistence.jpaRepository.RefreshTokenJpaRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Repository
public class RefreshTokenRepositoryImpl implements IRefreshTokenRepository {

    private final RefreshTokenJpaRepository jpaRepository;
    private final RefreshTokenEntityMapper mapper;

    public RefreshTokenRepositoryImpl(RefreshTokenJpaRepository jpaRepository, RefreshTokenEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public RefreshToken save(@NotNull RefreshToken token) {
        RefreshTokenEntity entity = mapper.toEntity(token);
        RefreshTokenEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(Objects.requireNonNull(saved));
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(@NotNull String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional
    public void deleteByUserId(@NotNull Long userId) {
        jpaRepository.deleteByUserId(userId);
    }
}
