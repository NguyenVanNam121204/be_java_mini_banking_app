package com.bankapp.bankingapp.application.interfaces.repository;

import com.bankapp.bankingapp.domain.model.RefreshToken;

import java.util.Optional;

public interface IRefreshTokenRepository {
    
    RefreshToken save(RefreshToken token);
    
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    
    void deleteByUserId(Long userId);
}
