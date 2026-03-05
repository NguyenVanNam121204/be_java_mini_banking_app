package com.bankapp.bankingapp.application.validator;

import com.bankapp.bankingapp.domain.model.Account;
import com.bankapp.bankingapp.domain.model.enums.AccountStatus;
import com.bankapp.bankingapp.domain.model.enums.AccountType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Validator cho Account domain
 * Validate input data và business rules trước khi thực hiện operations
 */
@Component
public class AccountValidator {

    public List<String> validateForCreation(String accountNumber, Long userId, AccountType type) {
        List<String> errors = new ArrayList<>();

        if (accountNumber == null || accountNumber.isBlank()) {
            errors.add("Số tài khoản không được để trống");
        } else if (!accountNumber.matches("^\\d{10,16}$")) {
            errors.add("Số tài khoản phải từ 10-16 chữ số");
        }

        if (userId == null) {
            errors.add("User ID không được để trống");
        }

        if (type == null) {
            errors.add("Loại tài khoản không được để trống");
        }

        return errors;
    }

    public List<String> validateDeposit(Account account, BigDecimal amount) {
        List<String> errors = new ArrayList<>();

        if (account == null) {
            errors.add("Tài khoản không tồn tại");
            return errors;
        }

        if (amount == null) {
            errors.add("Số tiền không được để trống");
        } else if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Số tiền phải lớn hơn 0");
        } else if (amount.compareTo(new BigDecimal("1000000000")) > 0) {
            errors.add("Số tiền nạp không được vượt quá 1 tỷ");
        }

        if (account.getStatus() != AccountStatus.ACTIVE) {
            errors.add("Tài khoản không ở trạng thái hoạt động");
        }

        return errors;
    }

    public List<String> validateWithdraw(Account account, BigDecimal amount) {
        List<String> errors = new ArrayList<>();

        if (account == null) {
            errors.add("Tài khoản không tồn tại");
            return errors;
        }

        if (amount == null) {
            errors.add("Số tiền không được để trống");
        } else if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Số tiền phải lớn hơn 0");
        } else if (amount.compareTo(new BigDecimal("500000000")) > 0) {
            errors.add("Số tiền rút không được vượt quá 500 triệu");
        }

        if (account.getStatus() != AccountStatus.ACTIVE) {
            errors.add("Tài khoản không ở trạng thái hoạt động");
        }

        if (amount != null && !account.hasSufficientBalance(amount)) {
            errors.add("Số dư không đủ");
        }

        return errors;
    }

    public List<String> validateTransfer(Account fromAccount, Account toAccount, BigDecimal amount) {
        List<String> errors = new ArrayList<>();

        if (fromAccount == null) {
            errors.add("Tài khoản nguồn không tồn tại");
        }

        if (toAccount == null) {
            errors.add("Tài khoản đích không tồn tại");
        }

        if (fromAccount != null && toAccount != null) {
            if (fromAccount.getId().equals(toAccount.getId())) {
                errors.add("Không thể chuyển tiền cho chính mình");
            }
        }

        if (amount == null) {
            errors.add("Số tiền không được để trống");
        } else if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Số tiền phải lớn hơn 0");
        } else if (amount.compareTo(new BigDecimal("500000000")) > 0) {
            errors.add("Số tiền chuyển không được vượt quá 500 triệu");
        }

        if (fromAccount != null) {
            if (fromAccount.getStatus() != AccountStatus.ACTIVE) {
                errors.add("Tài khoản nguồn không hoạt động");
            }
            if (amount != null && !fromAccount.hasSufficientBalance(amount)) {
                errors.add("Số dư tài khoản nguồn không đủ");
            }
        }

        if (toAccount != null && toAccount.getStatus() != AccountStatus.ACTIVE) {
            errors.add("Tài khoản đích không hoạt động");
        }

        return errors;
    }

    public List<String> validateForClosure(Account account) {
        List<String> errors = new ArrayList<>();

        if (account == null) {
            errors.add("Tài khoản không tồn tại");
            return errors;
        }

        if (!account.getBalance().equals(BigDecimal.ZERO)) {
            errors.add("Không thể đóng tài khoản còn số dư. Vui lòng rút hết tiền trước");
        }

        if (account.getStatus() == AccountStatus.CLOSED) {
            errors.add("Tài khoản đã được đóng");
        }

        return errors;
    }

    public List<String> validateAccountNumber(String accountNumber) {
        List<String> errors = new ArrayList<>();

        if (accountNumber == null || accountNumber.isBlank()) {
            errors.add("Số tài khoản không được để trống");
        } else if (!accountNumber.matches("^\\d{10,16}$")) {
            errors.add("Số tài khoản không hợp lệ");
        }

        return errors;
    }
}
