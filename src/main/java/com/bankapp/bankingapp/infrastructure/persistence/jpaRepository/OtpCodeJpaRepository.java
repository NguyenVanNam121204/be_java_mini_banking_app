package com.bankapp.bankingapp.infrastructure.persistence.jpaRepository;

import com.bankapp.bankingapp.domain.model.enums.OtpType;
import com.bankapp.bankingapp.infrastructure.persistence.entity.OtpCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpCodeJpaRepository extends JpaRepository<OtpCodeEntity, Long> {

        /**
         * Lấy OTP mới nhất của user theo loại (chưa dùng, chưa hết hạn)
         */
        @Query("SELECT o FROM OtpCodeEntity o " +
                        "WHERE o.userId = :userId AND o.type = :type AND o.used = false " +
                        "ORDER BY o.createdAt DESC LIMIT 1")
        Optional<OtpCodeEntity> findLatestValidByUserIdAndType(
                        @Param("userId") Long userId,
                        @Param("type") OtpType type);

        /**
         * Xóa tất cả OTP cũ của user theo loại (dọn dẹp trước khi tạo OTP mới)
         */
        @Modifying
        @Query("DELETE FROM OtpCodeEntity o WHERE o.userId = :userId AND o.type = :type")
        void deleteByUserIdAndType(@Param("userId") Long userId, @Param("type") OtpType type);
}
