package com.bankapp.bankingapp.domain.model.enums;

public enum AuditAction {
    // Authentication
    LOGIN,
    LOGOUT,
    REGISTER,
    EMAIL_VERIFIED,
    PASSWORD_CHANGE,
    PASSWORD_RESET,
    PIN_CHANGE,

    // User Management
    USER_CREATED,
    USER_UPDATED,
    ACCOUNT_LOCKED,
    ACCOUNT_UNLOCKED,
    ROLE_ASSIGNED,

    // Account Management
    ACCOUNT_CREATED,
    ACCOUNT_LOCKED_ADMIN,
    ACCOUNT_UNLOCKED_ADMIN,
    ACCOUNT_CLOSED,

    // Transactions
    DEPOSIT,
    WITHDRAW,
    TRANSFER,
    TRANSFER_PENDING,       // Giao dich gia tri lon dang cho duyet
    TRANSFER_SUCCESS,       // Chuyen tien thanh cong (hoan tat)

    // Admin Actions
    ADMIN_APPROVE_TRANSACTION,  // Admin duyet giao dich pending
    ADMIN_REJECT_TRANSACTION,   // Admin tu choi giao dich pending
    ADMIN_FORCE_RESET_PASSWORD, // Admin ep doi mat khau nguoi dung

    // Security
    PIN_FAILED_ATTEMPT,
    PIN_LOCKED,
    UNAUTHORIZED_ACCESS
}
