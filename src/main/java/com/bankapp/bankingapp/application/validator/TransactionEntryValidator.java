package com.bankapp.bankingapp.application.validator;

import com.bankapp.bankingapp.domain.model.TransactionEntry;
import com.bankapp.bankingapp.domain.model.enums.EntryType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Validator cho TransactionEntry domain
 * Validate double-entry ledger integrity
 */
@Component
public class TransactionEntryValidator {

    public List<String> validateForCreation(
            Long transactionId,
            Long accountId,
            EntryType entryType,
            BigDecimal amount,
            BigDecimal balanceBefore,
            BigDecimal balanceAfter) {
        
        List<String> errors = new ArrayList<>();

        if (transactionId == null) {
            errors.add("Transaction ID không được để trống");
        }

        if (accountId == null) {
            errors.add("Account ID không được để trống");
        }

        if (entryType == null) {
            errors.add("Loại entry không được để trống");
        }

        if (amount == null) {
            errors.add("Số tiền không được để trống");
        } else if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Số tiền phải lớn hơn 0");
        }

        if (balanceBefore == null) {
            errors.add("Số dư trước không được để trống");
        } else if (balanceBefore.compareTo(BigDecimal.ZERO) < 0) {
            errors.add("Số dư trước không được âm");
        }

        if (balanceAfter == null) {
            errors.add("Số dư sau không được để trống");
        } else if (balanceAfter.compareTo(BigDecimal.ZERO) < 0) {
            errors.add("Số dư sau không được âm");
        }

        // Validate balance calculation
        if (amount != null && balanceBefore != null && balanceAfter != null && entryType != null) {
            BigDecimal expectedBalance;
            if (entryType == EntryType.CREDIT) {
                expectedBalance = balanceBefore.add(amount);
            } else {
                expectedBalance = balanceBefore.subtract(amount);
            }

            if (balanceAfter.compareTo(expectedBalance) != 0) {
                errors.add("Số dư sau giao dịch không khớp với tính toán");
            }
        }

        return errors;
    }

    public List<String> validateDoubleEntry(TransactionEntry debitEntry, TransactionEntry creditEntry) {
        List<String> errors = new ArrayList<>();

        if (debitEntry == null) {
            errors.add("Entry ghi nợ không được để trống");
        }

        if (creditEntry == null) {
            errors.add("Entry ghi có không được để trống");
        }

        if (debitEntry != null && creditEntry != null) {
            // Kiểm tra cùng transaction
            if (!debitEntry.getTransactionId().equals(creditEntry.getTransactionId())) {
                errors.add("Hai entry phải thuộc cùng một transaction");
            }

            // Kiểm tra số tiền bằng nhau
            if (debitEntry.getAmount().compareTo(creditEntry.getAmount()) != 0) {
                errors.add("Số tiền của debit và credit phải bằng nhau");
            }

            // Kiểm tra entry type
            if (debitEntry.getEntryType() != EntryType.DEBIT) {
                errors.add("Entry đầu tiên phải là DEBIT");
            }
            if (creditEntry.getEntryType() != EntryType.CREDIT) {
                errors.add("Entry thứ hai phải là CREDIT");
            }
        }

        return errors;
    }
}
