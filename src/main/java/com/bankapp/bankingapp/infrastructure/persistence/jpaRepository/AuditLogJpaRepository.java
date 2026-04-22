package com.bankapp.bankingapp.infrastructure.persistence.jpaRepository;

import com.bankapp.bankingapp.infrastructure.persistence.entity.AuditLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogJpaRepository extends JpaRepository<AuditLogEntity, Long> {
    List<AuditLogEntity> findTop5ByOrderByCreatedAtDesc();

    @Query("SELECT a FROM AuditLogEntity a " +
           "WHERE (:username IS NULL OR LOWER(a.username) LIKE LOWER(CONCAT('%', :username, '%'))) " +
           "AND (COALESCE(:actions, NULL) IS NULL OR a.action IN :actions) " +
           "AND (:date IS NULL OR function('to_char', a.createdAt, 'YYYY-MM-DD') = :date)")
    Page<AuditLogEntity> findAllFiltered(
            @Param("username") String username,
            @Param("actions") java.util.List<String> actions,
            @Param("date") String date,
            Pageable pageable);
}
