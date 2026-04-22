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

    @Override
    public org.springframework.data.domain.Page<AuditLog> findAll(org.springframework.data.domain.Pageable pageable, String username, String action, String date) {
        // Chuan hoa du lieu loc
        String usernameFilter = (username != null && !username.trim().isEmpty()) ? username.trim() : null;
        String dateFilter = (date != null && !date.trim().isEmpty()) ? date.trim() : null;

        java.util.List<String> actions = null;
        if (action != null && !action.trim().isEmpty() && !action.equalsIgnoreCase("ALL")) {
            actions = new java.util.ArrayList<>();
            switch (action.toUpperCase()) {
                case "AUTH":
                    actions.addAll(java.util.Arrays.asList("LOGIN", "LOGOUT", "REGISTER", "PASSWORD_CHANGE", "PASSWORD_RESET", "PIN_CHANGE"));
                    break;
                case "TRANSACTION":
                    actions.addAll(java.util.Arrays.asList("DEPOSIT", "WITHDRAW", "TRANSFER"));
                    break;
                case "ADMIN":
                    actions.addAll(java.util.Arrays.asList("USER_CREATED", "USER_UPDATED", "ACCOUNT_LOCKED", "ACCOUNT_UNLOCKED", "ROLE_ASSIGNED", "ACCOUNT_CREATED", "ACCOUNT_LOCKED_ADMIN", "ACCOUNT_UNLOCKED_ADMIN", "ACCOUNT_CLOSED", "UNAUTHORIZED_ACCESS"));
                    break;
                default:
                    actions.add(action); // Neu la action cu the
                    break;
            }
        }

        // Neu khong co bo loc nao, dung findAll mac dinh cua JPA de dam bao luon co du lieu
        if (usernameFilter == null && actions == null && dateFilter == null) {
            return auditLogJpaRepository.findAll(pageable)
                    .map(auditLogEntityMapper::toDomain);
        }

        return auditLogJpaRepository.findAllFiltered(usernameFilter, actions, dateFilter, pageable)
                .map(auditLogEntityMapper::toDomain);
    }
}
