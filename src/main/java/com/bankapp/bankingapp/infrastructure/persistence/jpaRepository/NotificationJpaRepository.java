package com.bankapp.bankingapp.infrastructure.persistence.jpaRepository;

import com.bankapp.bankingapp.infrastructure.persistence.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationJpaRepository extends JpaRepository<NotificationEntity, Long> {

    Page<NotificationEntity> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<NotificationEntity> findByIdAndUser_Id(Long id, Long userId);

    @Modifying
    @Query("update NotificationEntity n set n.read = true where n.user.id = :userId and n.read = false")
    void markAllAsReadByUserId(@Param("userId") Long userId);

    void deleteByIdAndUser_Id(Long id, Long userId);
}
