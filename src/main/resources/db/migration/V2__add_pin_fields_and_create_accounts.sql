-- V2__add_pin_fields_and_create_accounts.sql

-- Bổ sung thêm các cột mới cho UserEntity nếu chưa có ở bảng users (pin failed and locked out)
ALTER TABLE users ADD COLUMN IF NOT EXISTS pin_failed_attempts INT DEFAULT 0 NOT NULL;
ALTER TABLE users ADD COLUMN IF NOT EXISTS pin_locked_until TIMESTAMP;

-- Khởi tạo bảng tài khoản
CREATE TABLE accounts (
    id BIGSERIAL PRIMARY KEY,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL REFERENCES users(id),
    balance DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
    status VARCHAR(20) NOT NULL,
    type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE INDEX idx_accounts_account_number ON accounts(account_number);
CREATE INDEX idx_accounts_user_id ON accounts(user_id);
