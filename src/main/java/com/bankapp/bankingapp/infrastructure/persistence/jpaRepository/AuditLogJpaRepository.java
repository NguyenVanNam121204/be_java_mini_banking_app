package com.bankapp.bankingapp.infrastructure.persistence.jpaRepository;

import com.bankapp.bankingapp.infrastructure.persistence.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogJpaRepository extends JpaRepository<AuditLogEntity, Long> {
}
