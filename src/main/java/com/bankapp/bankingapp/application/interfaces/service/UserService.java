package com.bankapp.bankingapp.application.interfaces.service;

import com.bankapp.bankingapp.application.dto.response.UserResponseDto;

import java.util.List;

public interface UserService {
    
    /**
     * Lấy danh sách tất cả users (ADMIN only)
     */
    List<UserResponseDto> getAllUsers();
    
    /**
     * Lấy thông tin profile của user hiện tại
     */
    UserResponseDto getCurrentUserProfile();
}
