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

    // Dung JPQL voi kiem tra null rieng le - tranh COALESCE voi List param
    @Query("SELECT a FROM AuditLogEntity a " +
           "WHERE (:username = '' OR LOWER(a.username) LIKE LOWER(CONCAT('%', :username, '%'))) " +
           "AND (:date = '' OR function('to_char', a.createdAt, 'YYYY-MM-DD') = :date)")
    Page<AuditLogEntity> findByUsernameAndDate(
            @Param("username") String username,
            @Param("date") String date,
            Pageable pageable);

    // Query rieng khi co action filter
    @Query("SELECT a FROM AuditLogEntity a " +
           "WHERE (:username = '' OR LOWER(a.username) LIKE LOWER(CONCAT('%', :username, '%'))) " +
           "AND a.action IN :actions " +
           "AND (:date = '' OR function('to_char', a.createdAt, 'YYYY-MM-DD') = :date)")
    Page<AuditLogEntity> findByUsernameActionsAndDate(
            @Param("username") String username,
            @Param("actions") List<String> actions,
            @Param("date") String date,
            Pageable pageable);

    @Query("SELECT a FROM AuditLogEntity a " +
           "WHERE (:username = '' OR LOWER(a.username) LIKE LOWER(CONCAT('%', :username, '%'))) " +
           "AND (:date = '' OR function('to_char', a.createdAt, 'YYYY-MM-DD') = :date)")
    List<AuditLogEntity> findByUsernameAndDateForExport(
            @Param("username") String username,
            @Param("date") String date,
            org.springframework.data.domain.Sort sort);

    @Query("SELECT a FROM AuditLogEntity a " +
           "WHERE (:username = '' OR LOWER(a.username) LIKE LOWER(CONCAT('%', :username, '%'))) " +
           "AND a.action IN :actions " +
           "AND (:date = '' OR function('to_char', a.createdAt, 'YYYY-MM-DD') = :date)")
    List<AuditLogEntity> findByUsernameActionsAndDateForExport(
            @Param("username") String username,
            @Param("actions") List<String> actions,
            @Param("date") String date,
            org.springframework.data.domain.Sort sort);
}
