package com.bankapp.bankingapp.infrastructure.persistence.jpaRepository;

import com.bankapp.bankingapp.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

    @EntityGraph(attributePaths = "roles")
    Optional<UserEntity> findByUsername(String username);

    @EntityGraph(attributePaths = "roles")
    Optional<UserEntity> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
