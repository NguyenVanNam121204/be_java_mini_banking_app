package com.bankapp.bankingapp.application.interfaces.service;

import com.bankapp.bankingapp.application.dto.response.UserResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

import java.util.List;

public interface IUserService {

    /**
     * Lấy danh sách tất cả users (ADMIN only) - deprecated, use getAllUsersPaginated instead
     */
    @Deprecated
    List<UserResponseDto> getAllUsers();

    /**
     * Lấy danh sách users với pagination (ADMIN only)
     */
    Page<UserResponseDto> getAllUsersPaginated(@NonNull Pageable pageable, String keyword);

    /**
     * Lấy thông tin profile của user hiện tại
     */
    UserResponseDto getCurrentUserProfile();

    /**
     * Đổi mật khẩu cho user hiện tại
     */
    void changePassword(com.bankapp.bankingapp.application.dto.request.ChangePasswordRequestDto request);

    /**
     * Cài đặt mã PIN (nếu chưa có)
     */
    void setupPin(com.bankapp.bankingapp.application.dto.request.SetupPinRequestDto request);

    /**
     * Đổi mã PIN
     */
    void changePin(com.bankapp.bankingapp.application.dto.request.ChangePinRequestDto request);

    // ADMIN APIs
    UserResponseDto createUserByAdmin(com.bankapp.bankingapp.application.dto.request.AdminCreateUserRequestDto request);
    void lockUser(Long userId);
    void unlockUser(Long userId);
    void assignRole(Long userId, String roleName);
    void forceResetPassword(Long userId, String newPassword);
}
