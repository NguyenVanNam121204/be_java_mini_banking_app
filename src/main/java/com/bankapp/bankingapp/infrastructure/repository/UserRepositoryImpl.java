package com.bankapp.bankingapp.infrastructure.repository;

import com.bankapp.bankingapp.application.interfaces.repository.UserRepository;
import com.bankapp.bankingapp.domain.model.User;
import com.bankapp.bankingapp.infrastructure.persistence.entity.UserEntity;
import com.bankapp.bankingapp.infrastructure.persistence.mapper.UserMapper;
import com.bankapp.bankingapp.infrastructure.persistence.repository.UserJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@SuppressWarnings("null")
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository jpaRepository;
    private final UserMapper mapper;

    public UserRepositoryImpl(UserJpaRepository jpaRepository, UserMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public User save(User user) {
        UserEntity entity;
        
        if (user.getId() != null) {
            // Update existing
            entity = jpaRepository.findById(user.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            mapper.updateEntity(user, entity);
        } else {
            // Create new
            entity = mapper.toEntity(user);
        }

        UserEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public List<User> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(User user) {
        if (user.getId() != null) {
            jpaRepository.deleteById(user.getId());
        }
    }
}
