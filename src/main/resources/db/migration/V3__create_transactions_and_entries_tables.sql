-- V3__create_transactions_and_entries_tables.sql

-- Bổ sung Version cho Optimistic Locking để chống Concurrent Update mất tiền
ALTER TABLE accounts ADD COLUMN version BIGINT DEFAULT 0 NOT NULL;

-- Bảng Giao Dịch chính (Header)
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    reference_number VARCHAR(50) NOT NULL UNIQUE,
    type VARCHAR(20) NOT NULL,
    amount DECIMAL(19, 4) NOT NULL,
    status VARCHAR(20) NOT NULL,
    from_account_id BIGINT REFERENCES accounts(id),
    to_account_id BIGINT REFERENCES accounts(id),
    description TEXT,
    initiated_by VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    completed_at TIMESTAMP
);

CREATE INDEX idx_txn_from_account ON transactions(from_account_id);
CREATE INDEX idx_txn_to_account ON transactions(to_account_id);
CREATE INDEX idx_txn_reference ON transactions(reference_number);

-- Bảng Sổ Cái Ghi Ghép Cép (Double-entry Ledger)
CREATE TABLE transaction_entries (
    id BIGSERIAL PRIMARY KEY,
    transaction_id BIGINT NOT NULL REFERENCES transactions(id),
    account_id BIGINT NOT NULL REFERENCES accounts(id),
    entry_type VARCHAR(10) NOT NULL, -- DEBIT (Trừ) or CREDIT (Cộng)
    amount DECIMAL(19, 4) NOT NULL,
    balance_before DECIMAL(19, 4) NOT NULL,
    balance_after DECIMAL(19, 4) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_txne_transaction_id ON transaction_entries(transaction_id);
CREATE INDEX idx_txne_account_id ON transaction_entries(account_id);
