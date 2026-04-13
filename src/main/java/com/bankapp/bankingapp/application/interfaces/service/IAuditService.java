package com.bankapp.bankingapp.application.interfaces.service;

public interface IAuditService {
    void logAction(String username, String action, String details);
}
