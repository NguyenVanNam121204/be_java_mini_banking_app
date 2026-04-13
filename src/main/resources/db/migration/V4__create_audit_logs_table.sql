-- V4: Create audit_logs table for system audit logging
CREATE TABLE IF NOT EXISTS audit_logs
(
    id         BIGSERIAL    NOT NULL,
    username   VARCHAR(255) NOT NULL,
    action     VARCHAR(255) NOT NULL,
    details    VARCHAR(1000),
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_audit_logs PRIMARY KEY (id)
);
