package com.bankapp.bankingapp.application.validator;

import com.bankapp.bankingapp.domain.model.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validator cho User domain
 * Validate input data trước khi tạo hoặc cập nhật User
 */
@Component
public class UserValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_]{3,20}$");

    public List<String> validateForCreation(String username, String email, String password, String pin) {
        List<String> errors = new ArrayList<>();

        // Username validation
        if (username == null || username.isBlank()) {
            errors.add("Username không được để trống");
        } else if (!USERNAME_PATTERN.matcher(username).matches()) {
            errors.add("Username phải từ 3-20 ký tự, chỉ chứa chữ, số và dấu gạch dưới");
        }

        // Email validation
        if (email == null || email.isBlank()) {
            errors.add("Email không được để trống");
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            errors.add("Email không hợp lệ");
        }

        // Password validation
        if (password == null || password.isBlank()) {
            errors.add("Password không được để trống");
        } else if (password.length() < 8) {
            errors.add("Password phải có ít nhất 8 ký tự");
        }

        // PIN validation
        if (pin != null && !pin.isBlank()) {
            if (!pin.matches("^\\d{6}$")) {
                errors.add("PIN phải là 6 chữ số");
            }
        }

        return errors;
    }

    public List<String> validateForUpdate(User user, String newEmail) {
        List<String> errors = new ArrayList<>();

        if (user == null) {
            errors.add("User không tồn tại");
            return errors;
        }

        if (newEmail != null && !newEmail.isBlank()) {
            if (!EMAIL_PATTERN.matcher(newEmail).matches()) {
                errors.add("Email không hợp lệ");
            }
        }

        return errors;
    }

    public List<String> validatePasswordChange(String oldPassword, String newPassword) {
        List<String> errors = new ArrayList<>();

        if (oldPassword == null || oldPassword.isBlank()) {
            errors.add("Mật khẩu cũ không được để trống");
        }

        if (newPassword == null || newPassword.isBlank()) {
            errors.add("Mật khẩu mới không được để trống");
        } else if (newPassword.length() < 8) {
            errors.add("Mật khẩu mới phải có ít nhất 8 ký tự");
        } else if (oldPassword != null && oldPassword.equals(newPassword)) {
            errors.add("Mật khẩu mới phải khác mật khẩu cũ");
        }

        return errors;
    }

    /**
     * Validate mật khẩu mới - dùng trong forgot password flow (không cần
     * oldPassword)
     */
    public List<String> validateNewPassword(String newPassword) {
        List<String> errors = new ArrayList<>();

        if (newPassword == null || newPassword.isBlank()) {
            errors.add("Mật khẩu mới không được để trống");
        } else if (newPassword.length() < 8) {
            errors.add("Mật khẩu mới phải có ít nhất 8 ký tự");
        }

        return errors;
    }

    public List<String> validatePinChange(String pin) {
        List<String> errors = new ArrayList<>();

        if (pin == null || pin.isBlank()) {
            errors.add("PIN không được để trống");
        } else if (!pin.matches("^\\d{6}$")) {
            errors.add("PIN phải là 6 chữ số");
        }

        return errors;
    }

    public List<String> validatePin(String pin) {
        List<String> errors = new ArrayList<>();

        if (pin == null || pin.isBlank()) {
            errors.add("PIN không được để trống");
        } else if (!pin.matches("^\\d{6}$")) {
            errors.add("PIN không hợp lệ");
        }

        return errors;
    }
}
