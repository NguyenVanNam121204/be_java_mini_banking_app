package com.bankapp.bankingapp.application.interfaces.repository;

import com.bankapp.bankingapp.domain.model.OtpCode;
import com.bankapp.bankingapp.domain.model.enums.OtpType;

import java.util.Optional;

/**
 * Repository interface cho OtpCode - thuộc Application Layer
 * Giữ đúng kiến trúc: Application layer không biết gì về JPA
 */
public interface OtpCodeRepository {

    OtpCode save(OtpCode otpCode);

    /**
     * Lấy OTP mới nhất còn hiệu lực của user theo loại
     */
    Optional<OtpCode> findLatestValidByUserIdAndType(Long userId, OtpType type);

    /**
     * Xóa tất cả OTP cũ của user theo loại (gọi trước khi tạo OTP mới)
     */
    void deleteByUserIdAndType(Long userId, OtpType type);
}
