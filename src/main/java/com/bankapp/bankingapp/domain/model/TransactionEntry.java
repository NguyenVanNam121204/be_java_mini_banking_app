package com.bankapp.bankingapp.domain.model;

import com.bankapp.bankingapp.domain.model.enums.EntryType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Double-Entry Ledger
 * Mỗi transaction tạo ra 2 entries:
 * - DEBIT: Rút tiền từ account (-)
 * - CREDIT: Nạp tiền vào account (+)
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TransactionEntry {

    @Setter
    @EqualsAndHashCode.Include
    private Long id;

    private Long transactionId;
    private Long accountId;
    private EntryType entryType;
    private BigDecimal amount;

    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;

    @Setter
    private LocalDateTime createdAt;

    public TransactionEntry(Long id,
                            Long transactionId,
                            Long accountId,
                            EntryType entryType,
                            BigDecimal amount,
                            BigDecimal balanceBefore,
                            BigDecimal balanceAfter) {
        this.id = id;
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.entryType = entryType;
        this.amount = amount;
        this.balanceBefore = balanceBefore;
        this.balanceAfter = balanceAfter;
        this.createdAt = LocalDateTime.now();
    }

    // BUSINESS LOGIC

    public BigDecimal calculateExpectedBalance() {
        if (entryType == EntryType.CREDIT) {
            return balanceBefore.add(amount);
        } else {
            return balanceBefore.subtract(amount);
        }
    }

    public boolean hasValidBalance() {
        return balanceAfter.compareTo(calculateExpectedBalance()) == 0;
    }

    public boolean isCredit() {
        return entryType == EntryType.CREDIT;
    }

    public boolean isDebit() {
        return entryType == EntryType.DEBIT;
    }
}
