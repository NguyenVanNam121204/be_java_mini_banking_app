package com.bankapp.bankingapp.application.mapper;

import com.bankapp.bankingapp.application.dto.response.AuthResponseDto;
import com.bankapp.bankingapp.domain.model.User;
import org.springframework.stereotype.Component;

/**
 * Mapper for Auth-related DTOs
 * This mapper belongs to Application Layer
 */
@Component
public class AuthDtoMapper {

    private final UserDtoMapper userDtoMapper;

    public AuthDtoMapper(UserDtoMapper userDtoMapper) {
        this.userDtoMapper = userDtoMapper;
    }

    /**
     * Build complete AuthResponseDto with tokens and user info
     */
    public AuthResponseDto toAuthResponseDto(
            String accessToken,
            String refreshToken,
            Long expiresIn,
            User user) {

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .user(userDtoMapper.toUserInfoDto(user))
                .build();
    }

    /**
     * Build AuthResponseDto with user info only (for token refresh scenarios)
     */
    public AuthResponseDto toAuthResponseDto(
            String accessToken,
            String refreshToken,
            Long expiresIn,
            AuthResponseDto.UserInfoDto userInfo) {

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .user(userInfo)
                .build();
    }
}
