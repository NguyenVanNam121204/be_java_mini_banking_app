package com.bankapp.bankingapp.application.validator;

import com.bankapp.bankingapp.domain.model.Transaction;
import com.bankapp.bankingapp.domain.model.enums.TransactionType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Validator cho Transaction domain
 * Validate transaction data trước khi xử lý
 */
@Component
public class TransactionValidator {

    public List<String> validateForCreation(
            TransactionType type,
            BigDecimal amount,
            Long fromAccountId,
            Long toAccountId,
            String initiatedBy) {
        
        List<String> errors = new ArrayList<>();

        if (type == null) {
            errors.add("Loại giao dịch không được để trống");
        }

        if (amount == null) {
            errors.add("Số tiền không được để trống");
        } else if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Số tiền phải lớn hơn 0");
        }

        if (initiatedBy == null || initiatedBy.isBlank()) {
            errors.add("Người thực hiện không được để trống");
        }

        // Validate theo từng loại transaction
        if (type != null) {
            switch (type) {
                case DEPOSIT:
                    if (toAccountId == null) {
                        errors.add("Tài khoản nhận không được để trống cho giao dịch nạp tiền");
                    }
                    break;

                case WITHDRAW:
                    if (fromAccountId == null) {
                        errors.add("Tài khoản rút không được để trống cho giao dịch rút tiền");
                    }
                    break;

                case TRANSFER:
                    if (fromAccountId == null) {
                        errors.add("Tài khoản nguồn không được để trống cho giao dịch chuyển tiền");
                    }
                    if (toAccountId == null) {
                        errors.add("Tài khoản đích không được để trống cho giao dịch chuyển tiền");
                    }
                    if (fromAccountId != null && toAccountId != null && fromAccountId.equals(toAccountId)) {
                        errors.add("Không thể chuyển tiền cho chính mình");
                    }
                    break;
            }
        }

        return errors;
    }

    public List<String> validateForCompletion(Transaction transaction) {
        List<String> errors = new ArrayList<>();

        if (transaction == null) {
            errors.add("Giao dịch không tồn tại");
            return errors;
        }

        if (!transaction.isPending()) {
            errors.add("Chỉ có thể hoàn thành giao dịch đang pending");
        }

        return errors;
    }

    public List<String> validateReferenceNumber(String referenceNumber) {
        List<String> errors = new ArrayList<>();

        if (referenceNumber == null || referenceNumber.isBlank()) {
            errors.add("Mã tham chiếu không được để trống");
        } else if (referenceNumber.length() < 10 || referenceNumber.length() > 50) {
            errors.add("Mã tham chiếu không hợp lệ");
        }

        return errors;
    }
}
