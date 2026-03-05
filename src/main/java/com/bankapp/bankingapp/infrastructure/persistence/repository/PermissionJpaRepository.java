package com.bankapp.bankingapp.infrastructure.persistence.repository;

import com.bankapp.bankingapp.infrastructure.persistence.entity.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionJpaRepository extends JpaRepository<PermissionEntity, Long> {
    
    Optional<PermissionEntity> findByName(String name);
}
