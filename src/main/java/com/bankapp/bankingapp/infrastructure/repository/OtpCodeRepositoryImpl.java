package com.bankapp.bankingapp.infrastructure.repository;

import com.bankapp.bankingapp.application.interfaces.repository.IOtpCodeRepository;
import com.bankapp.bankingapp.domain.model.OtpCode;
import com.bankapp.bankingapp.domain.model.enums.OtpType;
import com.bankapp.bankingapp.infrastructure.persistence.entity.OtpCodeEntity;
import com.bankapp.bankingapp.infrastructure.persistence.mapper.OtpCodeEntityMapper;
import com.bankapp.bankingapp.infrastructure.persistence.jpaRepository.OtpCodeJpaRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Repository
public class OtpCodeRepositoryImpl implements IOtpCodeRepository {

    private final OtpCodeJpaRepository jpaRepository;
    private final OtpCodeEntityMapper mapper;

    public OtpCodeRepositoryImpl(OtpCodeJpaRepository jpaRepository, OtpCodeEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    @SuppressWarnings("null")
    public OtpCode save(@NotNull OtpCode otpCode) {
        OtpCodeEntity entity;

        if (otpCode.getId() != null) {
            // Update
            @NotNull Long otpId = Objects.requireNonNull(otpCode.getId());
            entity = jpaRepository.findById(otpId)
                    .orElseThrow(() -> new RuntimeException("OtpCode not found with id: " + otpId));
            mapper.updateEntity(otpCode, entity);
        } else {
            // Create
            entity = mapper.toEntity(otpCode);
        }

        OtpCodeEntity saved = Objects.requireNonNull(jpaRepository.save(entity));
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<OtpCode> findLatestValidByUserIdAndType(@NotNull Long userId, @NotNull OtpType type) {
        return jpaRepository.findLatestValidByUserIdAndType(userId, type)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional
    public void deleteByUserIdAndType(@NotNull Long userId, @NotNull OtpType type) {
        jpaRepository.deleteByUserIdAndType(userId, type);
    }
}
