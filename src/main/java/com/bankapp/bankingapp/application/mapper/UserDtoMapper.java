package com.bankapp.bankingapp.application.mapper;

import com.bankapp.bankingapp.application.dto.request.RegisterRequestDto;
import com.bankapp.bankingapp.application.dto.response.AuthResponseDto;
import com.bankapp.bankingapp.application.dto.response.UserResponseDto;
import com.bankapp.bankingapp.domain.model.Role;
import com.bankapp.bankingapp.domain.model.User;
import com.bankapp.bankingapp.domain.model.enums.UserStatus;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper for converting between User domain model and DTOs
 * This mapper belongs to Application Layer
 */
@Component
public class UserDtoMapper {

    /**
     * Map User domain to UserInfoDto for response
     */
    public AuthResponseDto.UserInfoDto toUserInfoDto(User user) {
        if (user == null) {
            return null;
        }

        return AuthResponseDto.UserInfoDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .status(user.getStatus().name())
                .roles(extractRoleNames(user.getRoles()))
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * Create User domain from RegisterRequestDto
     * Note: Password should be encoded before calling this method
     */
    public User toUserFromRegisterDto(RegisterRequestDto dto, String encodedPassword) {
        if (dto == null) {
            return null;
        }

        return new User(
                null, // id will be generated
                dto.getUsername(),
                dto.getEmail(),
                encodedPassword,
                null, // transactionPinHash
                UserStatus.PENDING // Chờ xác thực email OTP
        );
    }

    /**
     * Extract role names from Set of Role domain objects
     */
    private Set<String> extractRoleNames(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return Set.of();
        }

        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

    /**
     * Map User domain to UserResponseDto
     */
    public UserResponseDto toUserResponseDto(User user) {
        if (user == null) {
            return null;
        }

        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .status(user.getStatus().name())
                .roles(extractRoleNames(user.getRoles()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
