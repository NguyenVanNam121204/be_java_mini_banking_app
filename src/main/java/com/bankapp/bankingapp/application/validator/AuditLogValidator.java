package com.bankapp.bankingapp.application.validator;

import com.bankapp.bankingapp.domain.model.enums.AuditAction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Validator cho AuditLog domain
 * Validate audit log data
 */
@Component
public class AuditLogValidator {

    public List<String> validateForCreation(
            AuditAction action,
            String entityType,
            String performedBy) {
        
        List<String> errors = new ArrayList<>();

        if (action == null) {
            errors.add("Action không được để trống");
        }

        if (entityType == null || entityType.isBlank()) {
            errors.add("Entity type không được để trống");
        }

        if (performedBy == null || performedBy.isBlank()) {
            errors.add("Người thực hiện không được để trống");
        }

        return errors;
    }

    public List<String> validateIpAddress(String ipAddress) {
        List<String> errors = new ArrayList<>();

        if (ipAddress != null && !ipAddress.isBlank()) {
            // IPv4 validation
            if (!ipAddress.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
                // IPv6 basic validation
                if (!ipAddress.matches("^([0-9a-fA-F]{0,4}:){2,7}[0-9a-fA-F]{0,4}$")) {
                    errors.add("IP address không hợp lệ");
                }
            }
        }

        return errors;
    }
}
