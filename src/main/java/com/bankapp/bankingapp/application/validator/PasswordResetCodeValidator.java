package com.bankapp.bankingapp.application.validator;

import com.bankapp.bankingapp.domain.model.PasswordResetCode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Validator cho PasswordResetCode domain
 * Validate password reset flow
 */
@Component
public class PasswordResetCodeValidator {

    public List<String> validateForCreation(Long userId, String code) {
        List<String> errors = new ArrayList<>();

        if (userId == null) {
            errors.add("User ID không được để trống");
        }

        if (code == null || code.isBlank()) {
            errors.add("Mã xác nhận không được để trống");
        } else if (!code.matches("^\\d{6}$")) {
            errors.add("Mã xác nhận phải là 6 chữ số");
        }

        return errors;
    }

    public List<String> validateForVerification(PasswordResetCode resetCode, String code) {
        List<String> errors = new ArrayList<>();

        if (resetCode == null) {
            errors.add("Mã xác nhận không tồn tại hoặc đã hết hạn");
            return errors;
        }

        if (code == null || code.isBlank()) {
            errors.add("Vui lòng nhập mã xác nhận");
        } else if (!code.matches("^\\d{6}$")) {
            errors.add("Mã xác nhận không hợp lệ");
        }

        if (resetCode.isUsed()) {
            errors.add("Mã xác nhận đã được sử dụng");
        }

        if (resetCode.isExpired()) {
            errors.add("Mã xác nhận đã hết hạn");
        }

        if (resetCode.isLocked()) {
            errors.add("Mã xác nhận đã bị khóa do nhập sai quá " + resetCode.getAttemptCount() + " lần");
        }

        return errors;
    }

    public List<String> validateEmail(String email) {
        List<String> errors = new ArrayList<>();

        if (email == null || email.isBlank()) {
            errors.add("Email không được để trống");
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            errors.add("Email không hợp lệ");
        }

        return errors;
    }
}
