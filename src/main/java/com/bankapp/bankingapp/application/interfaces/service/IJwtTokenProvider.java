package com.bankapp.bankingapp.application.interfaces.service;

import org.springframework.security.core.Authentication;

/**
 * Interface cho JWT Token Provider — nam o Application layer.
 * Tuon thu Clean Architecture: Application Service chi phu thuoc vao interface nay,
 * khong biet den implementation cu the (JwtTokenProvider) trong Infrastructure layer.
 */
public interface IJwtTokenProvider {

    /**
     * Tao Access Token tu Authentication object (sau khi dang nhap thanh cong).
     */
    String generateAccessToken(Authentication authentication);

    /**
     * Tao Access Token tu username va roles (dung cho Token Rotation).
     */
    String generateAccessTokenFromUsername(String username, String roles);

    /**
     * Tao Refresh Token cho mot username.
     */
    String generateRefreshToken(String username);

    /**
     * Kiem tra tinh hop le cua token (chu ky va han su dung).
     */
    boolean validateToken(String token);

    /**
     * Lay username tu token.
     */
    String getUsernameFromToken(String token);

    /**
     * Lay thoi gian song cua Access Token (ms).
     */
    long getJwtExpiration();

    /**
     * Lay thoi gian song cua Refresh Token (ms).
     */
    long getRefreshExpiration();
}
