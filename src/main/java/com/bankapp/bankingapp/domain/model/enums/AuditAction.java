package com.bankapp.bankingapp.domain.model.enums;

public enum AuditAction {
    // Authentication
    LOGIN,
    LOGOUT,
    REGISTER,
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

    // Security
    PIN_FAILED_ATTEMPT,
    PIN_LOCKED,
    UNAUTHORIZED_ACCESS
}
