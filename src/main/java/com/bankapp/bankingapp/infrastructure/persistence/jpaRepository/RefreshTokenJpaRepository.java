package com.bankapp.bankingapp.infrastructure.persistence.jpaRepository;

import com.bankapp.bankingapp.infrastructure.persistence.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, Long> {
    
    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);
    
    void deleteByUserId(Long userId);
}
