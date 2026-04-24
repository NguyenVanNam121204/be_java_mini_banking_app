package com.bankapp.bankingapp.infrastructure.repository;

import com.bankapp.bankingapp.application.interfaces.repository.IAuditRepository;
import com.bankapp.bankingapp.domain.model.AuditLog;
import com.bankapp.bankingapp.infrastructure.persistence.entity.AuditLogEntity;
import com.bankapp.bankingapp.infrastructure.persistence.jpaRepository.AuditLogJpaRepository;
import com.bankapp.bankingapp.infrastructure.persistence.mapper.AuditLogEntityMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Implementation cua IAuditRepository.
 * Lop nay nam trong infrastructure — chiu trach nhiem giao tiep voi JPA/DB.
 * AuditServiceImpl chi biet den IAuditRepository (interface), khong biet class nay.
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
        Objects.requireNonNull(entity, "AuditLogEntity must not be null");
        AuditLogEntity saved = auditLogJpaRepository.save(entity);
        return auditLogEntityMapper.toDomain(saved);
    }

    @Override
    public Page<AuditLog> findAll(Pageable pageable, String username, String action, String date) {
        String usernameFilter = (username != null && !username.trim().isEmpty()) ? username.trim() : null;
        String dateFilter = (date != null && !date.trim().isEmpty()) ? date.trim() : null;

        List<String> actions = null;
        if (action != null && !action.trim().isEmpty() && !action.equalsIgnoreCase("ALL")) {
            actions = new ArrayList<>();
            switch (action.toUpperCase()) {
                case "AUTH":
                    actions.addAll(Arrays.asList(
                        "LOGIN", "LOGOUT", "REGISTER", "EMAIL_VERIFIED",
                        "PASSWORD_CHANGE", "PASSWORD_RESET", "PIN_CHANGE"));
                    break;
                case "TRANSACTION":
                    actions.addAll(Arrays.asList(
                        "DEPOSIT", "WITHDRAW", "TRANSFER",
                        "TRANSFER_PENDING", "TRANSFER_SUCCESS"));
                    break;
                case "ADMIN":
                    actions.addAll(Arrays.asList(
                        "USER_CREATED", "USER_UPDATED", "ACCOUNT_LOCKED", "ACCOUNT_UNLOCKED",
                        "ROLE_ASSIGNED", "ACCOUNT_CREATED", "ACCOUNT_LOCKED_ADMIN",
                        "ACCOUNT_UNLOCKED_ADMIN", "ACCOUNT_CLOSED",
                        "ADMIN_APPROVE_TRANSACTION", "ADMIN_REJECT_TRANSACTION",
                        "ADMIN_FORCE_RESET_PASSWORD", "UNAUTHORIZED_ACCESS"));
                    break;
                default:
                    actions.add(action.trim().toUpperCase());
                    break;
            }
        }

        // Convert nulls to empty strings to avoid Postgres parameter type inference errors with IS NULL
        String safeUsername = usernameFilter != null ? usernameFilter : "";
        String safeDate = dateFilter != null ? dateFilter : "";

        if (actions != null) {
            return auditLogJpaRepository
                    .findByUsernameActionsAndDate(safeUsername, actions, safeDate, pageable)
                    .map(auditLogEntityMapper::toDomain);
        } else if (usernameFilter != null || dateFilter != null) {
            return auditLogJpaRepository
                    .findByUsernameAndDate(safeUsername, safeDate, pageable)
                    .map(auditLogEntityMapper::toDomain);
        } else {
            return auditLogJpaRepository.findAll(pageable)
                    .map(auditLogEntityMapper::toDomain);
        }
    }

    @Override
    public List<AuditLog> findAllForExport(String username, String action, String date, Sort sort) {
        String usernameFilter = (username != null && !username.trim().isEmpty()) ? username.trim() : null;
        String dateFilter = (date != null && !date.trim().isEmpty()) ? date.trim() : null;

        List<String> actions = null;
        if (action != null && !action.trim().isEmpty() && !action.equalsIgnoreCase("ALL")) {
            actions = new ArrayList<>();
            switch (action.toUpperCase()) {
                case "AUTH":
                    actions.addAll(Arrays.asList(
                        "LOGIN", "LOGOUT", "REGISTER", "EMAIL_VERIFIED",
                        "PASSWORD_CHANGE", "PASSWORD_RESET", "PIN_CHANGE"));
                    break;
                case "TRANSACTION":
                    actions.addAll(Arrays.asList(
                        "DEPOSIT", "WITHDRAW", "TRANSFER",
                        "TRANSFER_PENDING", "TRANSFER_SUCCESS"));
                    break;
                case "ADMIN":
                    actions.addAll(Arrays.asList(
                        "USER_CREATED", "USER_UPDATED", "ACCOUNT_LOCKED", "ACCOUNT_UNLOCKED",
                        "ROLE_ASSIGNED", "ACCOUNT_CREATED", "ACCOUNT_LOCKED_ADMIN",
                        "ACCOUNT_UNLOCKED_ADMIN", "ACCOUNT_CLOSED",
                        "ADMIN_APPROVE_TRANSACTION", "ADMIN_REJECT_TRANSACTION",
                        "ADMIN_FORCE_RESET_PASSWORD", "UNAUTHORIZED_ACCESS"));
                    break;
                default:
                    actions.add(action.trim().toUpperCase());
                    break;
            }
        }

        String safeUsername = usernameFilter != null ? usernameFilter : "";
        String safeDate = dateFilter != null ? dateFilter : "";

        if (actions != null) {
            return auditLogJpaRepository
                    .findByUsernameActionsAndDateForExport(safeUsername, actions, safeDate, sort)
                    .stream()
                    .map(auditLogEntityMapper::toDomain)
                    .toList();
        } else if (usernameFilter != null || dateFilter != null) {
            return auditLogJpaRepository
                    .findByUsernameAndDateForExport(safeUsername, safeDate, sort)
                    .stream()
                    .map(auditLogEntityMapper::toDomain)
                    .toList();
        } else {
            return auditLogJpaRepository.findAll(sort)
                    .stream()
                    .map(auditLogEntityMapper::toDomain)
                    .toList();
        }
    }
}
