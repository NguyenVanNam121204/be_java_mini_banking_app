# Security Guide

## ⚠️ CRITICAL: Before Production Deployment

### 1. Change All Credentials in .env

**NEVER use default or weak credentials in production!**

```bash
# Generate strong JWT secret (256-bit)
openssl rand -base64 64

# Use the output to replace JWT_SECRET in .env
```

Update these values in `.env`:
- `DB_PASSWORD` - Strong database password (16+ characters)
- `JWT_SECRET` - Generated with openssl (as above)
- `MAIL_PASSWORD` - Gmail App Password (16 characters)
- `ADMIN_PASSWORD` - Strong admin password (16+ characters with complexity)

### 2. Verify .env is NOT in Git

```bash
# Check .gitignore includes .env
grep "^\.env$" .gitignore

# If .env was committed, remove from history:
git filter-branch --force --index-filter \
  "git rm --cached --ignore-unmatch .env" \
  --prune-empty --tag-name-filter cat -- --all
```

### 3. Password Requirements

**Minimum requirements for production:**
- Length: 12+ characters
- Must include: uppercase, lowercase, numbers, special characters
- Not in common password lists
- Not contain user information (name, email, etc.)

### 4. Environment-Specific Configuration

**Development:**
- `spring.jpa.hibernate.ddl-auto=update` (OK)
- Swagger UI enabled (OK)
- Debug logging (OK)

**Production:**
- `spring.jpa.hibernate.ddl-auto=validate` (REQUIRED)
- Use Flyway/Liquibase for migrations
- Swagger UI disabled or authenticated
- Info/Warn logging only
- Enable HTTPS
- Configure CORS properly

### 5. Rate Limiting (Implemented)

The application now has rate limiting for:
- Login: 5 attempts per 15 minutes
- OTP Generation: 3 per hour per user
- OTP Verification: 5 attempts per 15 minutes
- API calls: 100 requests per minute per IP

### 6. Database Indexes (Implemented)

Critical indexes have been added:
- `users(email)`
- `users(username)`
- `refresh_tokens(token_hash)`
- `otp_codes(user_id, type, created_at)`

### 7. Security Headers

The application sets these security headers:
- X-Content-Type-Options: nosniff
- X-Frame-Options: DENY
- X-XSS-Protection: 1; mode=block
- Strict-Transport-Security: max-age=31536000

### 8. Monitoring & Alerts

**Set up monitoring for:**
- Failed login attempts (potential brute force)
- Unusual API traffic patterns
- Database connection errors
- JWT validation failures
- Rate limit violations

### 9. Regular Security Maintenance

**Monthly:**
- Review access logs
- Update dependencies (check for CVEs)
- Rotate secrets if needed

**Quarterly:**
- Security audit
- Penetration testing
- Review and update security policies

### 10. Incident Response

**If credentials are compromised:**
1. Immediately rotate all secrets
2. Force logout all users (clear refresh tokens)
3. Review access logs for suspicious activity
4. Notify users if personal data was accessed
5. Document incident for compliance

## Security Contacts

For security issues, contact: [your-security-email@example.com]

**DO NOT** open public GitHub issues for security vulnerabilities.
