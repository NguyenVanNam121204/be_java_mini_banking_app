package com.bankapp.bankingapp.application.validator;

import java.util.List;

/**
 * Exception được throw khi validation fails
 * Chứa danh sách các lỗi validation
 */
public class ValidationException extends RuntimeException {

    private final List<String> errors;

    public ValidationException(String message) {
        super(message);
        this.errors = List.of(message);
    }

    public ValidationException(List<String> errors) {
        super(formatErrors(errors));
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }

    private static String formatErrors(List<String> errors) {
        if (errors == null || errors.isEmpty()) {
            return "Validation error";
        }
        return String.join("; ", errors);
    }
}
