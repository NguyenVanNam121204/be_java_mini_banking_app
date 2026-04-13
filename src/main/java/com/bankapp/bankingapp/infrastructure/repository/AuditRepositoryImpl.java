package com.bankapp.bankingapp.infrastructure.repository;

import com.bankapp.bankingapp.application.interfaces.repository.IAuditRepository;
import com.bankapp.bankingapp.domain.model.AuditLog;
import com.bankapp.bankingapp.infrastructure.persistence.entity.AuditLogEntity;
import com.bankapp.bankingapp.infrastructure.persistence.jpaRepository.AuditLogJpaRepository;
import com.bankapp.bankingapp.infrastructure.persistence.mapper.AuditLogEntityMapper;
import org.springframework.stereotype.Repository;

/**
 * Implementation của IAuditRepository.
 * Lớp này nằm trong infrastructure — chịu trách nhiệm giao tiếp với JPA/DB.
 * AuditServiceImpl chỉ biết đến IAuditRepository (interface), không biết class này.
 */
@Repository
public class AuditRepositoryImpl implements IAuditRepository {

    private final AuditLogJpaRepository auditLogJpaRepository;
    private final AuditLogEntityMapper auditLogEntityMapper;

    public AuditRepositoryImpl(AuditLogJpaRepository auditLogJpaRepository,
                                AuditLogEntityMapper auditLogEntityMapper) {
        this.auditLogJpaRepository = auditLogJpaRepository;
        this.auditLogEntityMapper = auditLogEntityMapper;
    }

    @Override
    public AuditLog save(AuditLog auditLog) {
        AuditLogEntity entity = auditLogEntityMapper.toEntity(auditLog);
        java.util.Objects.requireNonNull(entity, "AuditLogEntity must not be null");
        AuditLogEntity saved = auditLogJpaRepository.save(entity);
        return auditLogEntityMapper.toDomain(saved);
    }
}
