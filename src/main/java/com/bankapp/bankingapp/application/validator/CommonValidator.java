package com.bankapp.bankingapp.application.validator;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Common validator cho các validation chung
 */
@Component
public class CommonValidator {

    public List<String> validateId(Long id, String fieldName) {
        List<String> errors = new ArrayList<>();

        if (id == null) {
            errors.add(fieldName + " không được để trống");
        } else if (id <= 0) {
            errors.add(fieldName + " phải lớn hơn 0");
        }

        return errors;
    }

    public List<String> validateNotBlank(String value, String fieldName) {
        List<String> errors = new ArrayList<>();

        if (value == null || value.isBlank()) {
            errors.add(fieldName + " không được để trống");
        }

        return errors;
    }

    public List<String> validateLength(String value, String fieldName, int min, int max) {
        List<String> errors = new ArrayList<>();

        if (value != null && !value.isBlank()) {
            if (value.length() < min || value.length() > max) {
                errors.add(fieldName + " phải từ " + min + " đến " + max + " ký tự");
            }
        }

        return errors;
    }

    public boolean hasErrors(List<String> errors) {
        return errors != null && !errors.isEmpty();
    }

    public String formatErrors(List<String> errors) {
        if (errors == null || errors.isEmpty()) {
            return "";
        }
        return String.join("; ", errors);
    }
}
