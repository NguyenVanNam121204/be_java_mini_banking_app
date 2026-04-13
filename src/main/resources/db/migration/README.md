# Flyway Database Migrations

## Overview
This directory contains Flyway migration scripts for database schema versioning.

## Naming Convention
- Format: `V{version}__{description}.sql`
- Example: `V1__create_users_table.sql`, `V2__add_email_verification.sql`

## Creating New Migrations

### 1. Create SQL File
```bash
# Example: Add a new column
V3__add_phone_number_to_users.sql
```

### 2. Write Migration
```sql
-- V3__add_phone_number_to_users.sql
ALTER TABLE users ADD COLUMN phone_number VARCHAR(20);
CREATE INDEX idx_users_phone ON users(phone_number);
```

### 3. Test Migration
```bash
# Run application - Flyway will automatically apply
./mvnw spring-boot:run
```

## Best Practices

### DO:
✅ Always test migrations on dev database first
✅ Use descriptive names for clarity
✅ Keep migrations small and focused
✅ Add indexes in the same migration as tables
✅ Use transactions for data migrations

### DON'T:
❌ Never edit already-applied migrations
❌ Don't mix DDL and DML in same migration (separate them)
❌ Don't use DROP operations without backup
❌ Avoid complex logic - keep SQL simple

## Migration Types

### Schema Changes (DDL)
```sql
-- V4__create_audit_log_table.sql
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    action VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_created_at ON audit_logs(created_at);
```

### Data Changes (DML)
```sql
-- V5__populate_default_roles.sql
INSERT INTO roles (name, description) VALUES 
('ADMIN', 'Administrator role with full access'),
('USER', 'Standard user role');
```

### Rollback Strategy
```sql
-- Create separate down migration if needed
-- R__rollback_v3.sql (Repeatable migration)
ALTER TABLE users DROP COLUMN IF EXISTS phone_number;
```

## Current Schema Status

### Baseline
The application uses `spring.flyway.baseline-on-migrate=true` which means:
- Existing schema is treated as baseline (version 0)
- New migrations start from V1
- All future changes MUST use Flyway migrations

### Required Indexes (Already exist from initial schema)
- users(email) - UNIQUE
- users(username) - UNIQUE  
- refresh_tokens(token_hash)
- otp_codes(user_id, type, created_at)

## Troubleshooting

### Migration Failed
```bash
# Check Flyway schema history
SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC;

# Repair failed migration (use with caution!)
./mvnw flyway:repair
```

### Rollback Migration
1. Create new migration to undo changes
2. Example: If V3 added column, V4 removes it
```sql
-- V4__remove_phone_number_from_users.sql
ALTER TABLE users DROP COLUMN phone_number;
```

## References
- [Flyway Documentation](https://flywaydb.org/documentation/)
- [Migration Best Practices](https://flywaydb.org/documentation/concepts/migrations#best-practices)
