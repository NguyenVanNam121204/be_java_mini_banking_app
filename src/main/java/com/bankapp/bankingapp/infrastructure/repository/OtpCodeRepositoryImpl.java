package com.bankapp.bankingapp.infrastructure.repository;

import com.bankapp.bankingapp.application.interfaces.repository.OtpCodeRepository;
import com.bankapp.bankingapp.domain.model.OtpCode;
import com.bankapp.bankingapp.domain.model.enums.OtpType;
import com.bankapp.bankingapp.infrastructure.persistence.entity.OtpCodeEntity;
import com.bankapp.bankingapp.infrastructure.persistence.mapper.OtpCodeMapper;
import com.bankapp.bankingapp.infrastructure.persistence.repository.OtpCodeJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@SuppressWarnings("null")
public class OtpCodeRepositoryImpl implements OtpCodeRepository {

    private final OtpCodeJpaRepository jpaRepository;
    private final OtpCodeMapper mapper;

    public OtpCodeRepositoryImpl(OtpCodeJpaRepository jpaRepository, OtpCodeMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public OtpCode save(OtpCode otpCode) {
        OtpCodeEntity entity;

        if (otpCode.getId() != null) {
            // Update
            entity = jpaRepository.findById(otpCode.getId())
                    .orElseThrow(() -> new RuntimeException("OtpCode not found with id: " + otpCode.getId()));
            mapper.updateEntity(otpCode, entity);
        } else {
            // Create
            entity = mapper.toEntity(otpCode);
        }

        OtpCodeEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<OtpCode> findLatestValidByUserIdAndType(Long userId, OtpType type) {
        return jpaRepository.findLatestValidByUserIdAndType(userId, type)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional
    public void deleteByUserIdAndType(Long userId, OtpType type) {
        jpaRepository.deleteByUserIdAndType(userId, type);
    }
}
