# Phan Tich & Danh Gia Du An Backend — banking-app

Stack: Java 17 - Spring Boot 3.5 - PostgreSQL - JPA/Hibernate - Flyway - JWT - Bucket4j
Ngay danh gia: 2026-04-07

---

## Tong Quan Kien Truc

```
banking-app/
├── domain/           <- Domain Models (core business logic, khong phu thuoc framework)
├── application/      <- Use-cases, Services, DTOs, Interfaces, Validators
├── infrastructure/   <- JPA Entities, Repositories, Security, Config, Email
├── presentation/     <- REST Controllers
└── common/           <- Cross-cutting: Exception Handling
```

Du an ap dung Clean Architecture (Layered) voi 4 lop ro rang.
Huong phu thuoc dung: presentation -> application -> domain, infrastructure implement cac interface cua application.

---

## DIEM TONG QUAT: 7.6 / 10

| Hang muc                    | Diem      | Danh gia            |
|-----------------------------|-----------|---------------------|
| Kien truc & Cau truc        | 8.5 / 10  | Rat tot             |
| Bao mat (Security)          | 8.0 / 10  | Tot                 |
| Thiet ke Domain (DDD)       | 7.5 / 10  | Tot                 |
| Chat luong ma nguon         | 7.0 / 10  | Kha tot, con van de |
| Xu ly loi (Error Handling)  | 6.5 / 10  | Can cai thien       |
| Database & Migrations       | 8.0 / 10  | Tot                 |
| Testing                     | 2.0 / 10  | THIEU NGHIEM TRONG  |
| API Design                  | 7.5 / 10  | Tot                 |
| Kha nang mo rong            | 6.0 / 10  | Con han che         |
| Tai lieu & Documentation    | 7.0 / 10  | Kha on              |

---

## DIEM MANH

### 1. Kien Truc Clean & Tach Biet Ro Rang (8.5/10)

Du an tuan thu Clean Architecture nghiem tuc:
- Domain models (Account, User, Transaction...) hoan toan KHONG phu thuoc vao Spring, JPA hay bat ky framework nao.
- Repository interfaces (IAccountRepository, ITransactionRepository) nam trong application/interfaces/repository,
  implementation nam trong infrastructure/repository — dependency inversion dung chuan.
- Phan tach ro DTO <-> Domain Model <-> JPA Entity voi cac mapper rieng biet.

### 2. Security Layer Chac Chan (8.0/10)

- JWT Authentication voi ca Access Token va Refresh Token.
- Token Rotation pattern: Refresh token cu bi revoke ngay khi issue token moi — chong token theft.
- Refresh token duoc hash SHA-256 truoc khi luu DB — neu DB bi lo, token van an toan.
- OTP va PIN BCrypt hashed — khong luu plaintext.
- Rate Limiting voi Bucket4j: ap dung cho login, OTP, forgot-password.
- HSTS headers duoc cau hinh trong SecurityConfig.
- Forgot password khong leak thong tin email ton tai (luon tra 200 OK).
- Swagger chi expose o moi truong dev/local, an o production.

### 3. Domain Model Co Business Logic — Rich Domain (7.5/10)

// Account.java
public void withdraw(BigDecimal amount) {
    if (this.status != AccountStatus.ACTIVE) throw new IllegalStateException("Tai khoan khong hoat dong");
    if (!hasSufficientBalance(amount)) throw new IllegalStateException("So du khong du");
    this.balance = this.balance.subtract(amount);
}

// User.java — PIN lockout tu dong sau 5 lan sai
public void increasePinFailedAttempts() {
    this.pinFailedAttempts++;
    if (this.pinFailedAttempts >= 5) {
        this.pinLockedUntil = LocalDateTime.now().plusMinutes(15);
    }
}

Domain tu bao ve invariant cua minh — Rich Domain Model dung chuan, khong phai Anemic Domain.

### 4. Double-Entry Accounting (8.0/10)

- Moi transaction co TransactionEntry ghi lai DEBIT/CREDIT.
- Luu ca balance_before va balance_after — audit trail hoan chinh.
- @Transactional bao toan bo luong — ACID dam bao.
- Schema: transactions (header) + transaction_entries (double-entry ledger).

### 5. Flyway Migrations (8.0/10)

- Schema version control dung chuan.
- Co 'version' column cho Optimistic Locking.
- spring.jpa.hibernate.ddl-auto=validate — khong de Hibernate tu sua schema production.

### 6. OTP Flow Hoan Chinh (7.5/10)

- OTP co expiry time, gioi han so lan nhap sai.
- Xoa OTP cu truoc khi tao moi — khong trung lap.
- Gui email @Async (khong block request).

---

## DIEM YEU & VAN DE CAN CAI THIEN

==========================================================================
VAN DE 1 (NGHIEM TRONG) — KHONG CO UNIT TESTS / INTEGRATION TESTS (2/10)
==========================================================================

Day la van de nghiem trong nhat cua du an.
- Thu muc src/test/ gan nhu trong.
- Khong co test cho TransactionServiceImpl — he thong xu ly tien KHONG CO BAO LUOI AN TOAN.
- Khong co test cho security, OTP validation, PIN locking.
- Voi banking system, day la rui ro rat cao khong the chap nhan.

Can viet toi thieu:
  [x] Unit tests cho TransactionServiceImpl (deposit, withdraw, transfer, edge cases)
  [x] Unit tests cho AuthServiceImpl (OTP flow, token rotation)
  [x] Integration tests cho Transaction API endpoints
  [x] Security tests (unauthorized access, RBAC)

==========================================================================
VAN DE 2 — AuditServiceImpl VI PHAM CLEAN ARCHITECTURE
==========================================================================

AuditServiceImpl nam trong application/service nhung lai import truc tiep:

  import com.bankapp.bankingapp.infrastructure.persistence.entity.AuditLogEntity;        // SAI
  import com.bankapp.bankingapp.infrastructure.persistence.jpaRepository.AuditLogJpaRepository; // SAI

Application layer KHONG DUOC PHEP import tu infrastructure layer.

Fix:
  1. Tao IAuditRepository trong application/interfaces/repository
  2. AuditServiceImpl chi dung IAuditRepository (interface)
  3. AuditRepositoryImpl o infrastructure moi import JPA

==========================================================================
VAN DE 3 — Optimistic Locking CHUA DUOC KICH HOAT
==========================================================================

Schema co cot 'version' tren accounts, nhung AccountEntity chua co @Version.
Hibernate KHONG TU DONG dung cot version de kiem tra concurrent update.
Race condition van co the xay ra khi nhieu request dong thoi thao tac cung tai khoan.

Fix — them vao AccountEntity:
  @Version
  private Long version;

==========================================================================
VAN DE 4 — GlobalExceptionHandler CHE GIAU ERROR MESSAGE
==========================================================================

@ExceptionHandler(IllegalArgumentException.class)
public ResponseEntity<ErrorResponseDto> handleIllegalArgument(IllegalArgumentException ex, ...) {
    // An message thuc! ("Ma PIN khong chinh xac", "So du khong du", ...)
    ErrorResponseDto error = ErrorResponseDto.of(..., "Invalid request", ...);
    return ResponseEntity.badRequest().body(error);
}

Frontend khong the hien thi loi co nghia cho nguoi dung.

Fix: Thay "Invalid request" bang ex.getMessage() cho IllegalArgumentException.

==========================================================================
VAN DE 5 — BUG TRONG createUserByAdmin (Role Name Sai)
==========================================================================

// UserServiceImpl.java
Role role = roleRepository.findByName("ROLE_USER")  // SAI! Role trong DB la "USER"
        .orElseThrow(() -> new IllegalArgumentException("Default role ROLE_USER khong ton tai"));

Roles duoc seed voi ten "USER", tim "ROLE_USER" se luon throw exception khi admin tao user.

Fix: Doi "ROLE_USER" thanh "USER".

==========================================================================
VAN DE 6 — RateLimitFilter CONSUME REQUEST INPUTSTREAM
==========================================================================

// RateLimitFilter.java
String body = new String(request.getInputStream().readAllBytes()); // Stream bi consume!

Controller sau filter se KHONG DOC DUOC request body (stream da bi dung).

Fix: Wrap request bang ContentCachingRequestWrapper truoc khi doc body.

==========================================================================
VAN DE 7 — Rate Limit IN-MEMORY (Khong Scale)
==========================================================================

RateLimitService dung ConcurrentHashMap in-memory.
Khi deploy nhieu instance (horizontal scaling), rate limit KHONG HOAT DONG cross-instance.

Fix: Dung Redis va Bucket4j Redis extension cho distributed rate limiting.

==========================================================================
VAN DE 8 — THIEU IDEMPOTENCY cho Transaction API
==========================================================================

Neu client retry Deposit do network timeout, tien co the duoc nap HAI LAN.

Fix: Them Idempotency-Key header, luu vao DB kiem tra truoc khi xu ly.

==========================================================================
VAN DE 9 — show-sql=true KHONG PHAN THEO ENVIRONMENT
==========================================================================

spring.jpa.show-sql=true  # Luon bat ke ca production — in SQL ra log

Fix: Chuyen ve spring.jpa.show-sql=false, chi bat ở profile dev/local.

---

## API ENDPOINTS SUMMARY

| Module         | Endpoints                                                      | Tinh trang |
|----------------|----------------------------------------------------------------|------------|
| Auth           | Register, Login, Refresh Token, Verify Email, Forgot/Reset PW | Day du     |
| User           | Profile, Change Password, Setup/Change PIN                     | Day du     |
| Account        | Create, Get Balance, Get Accounts                              | Day du     |
| Transaction    | Deposit, Withdraw, Transfer, History (paginated)               | Day du     |
| Admin - User   | Create, Lock/Unlock, Assign Role, Force Reset Password         | Day du     |
| Admin - Account| Lock/Unlock Account                                            | Day du     |

---

## ENTERPRISE FEATURES CHECKLIST

| Tinh nang                  | Co?         | Ghi chu                         |
|----------------------------|-------------|----------------------------------|
| JWT Authentication         | CO          | Access + Refresh Token           |
| Token Rotation             | CO          | Enterprise pattern               |
| Refresh Token Hashed in DB | CO          | SHA-256                          |
| OTP Email Verification     | CO          | BCrypt hashed                    |
| Transaction PIN + Lockout  | CO          | BCrypt + 5-attempt lockout       |
| Double-Entry Ledger        | CO          | balance_before/after tracking    |
| Flyway Schema Migrations   | CO          | Validated on migrate             |
| Rate Limiting              | CO          | Bucket4j (in-memory)             |
| RBAC (Role + Permission)   | CO          | Granular permissions             |
| Swagger / OpenAPI          | CO          | Environment-aware                |
| Async Email                | CO          | Non-blocking                     |
| HSTS Security Headers      | CO          | 1 nam                            |
| Optimistic Locking         | CHUA XONG   | Schema co, @Version chua active  |
| Unit / Integration Tests   | KHONG CON   | Khong co - RUI RO CAO            |
| Idempotency                | KHONG CO    | Khong co                         |
| Distributed Rate Limit     | KHONG CO    | In-memory only                   |
| Caching Layer              | KHONG CO    | Khong co                         |

---

## ROADMAP CAI THIEN

### Uu tien cao — Can lam ngay
  1. Viet Unit Tests cho Transaction va Auth service
  2. Kich hoat @Version tren AccountEntity
  3. Fix GlobalExceptionHandler: truyen ex.getMessage() cho IllegalArgumentException
  4. Fix bug role name trong createUserByAdmin ("USER" thay "ROLE_USER")
  5. Tai cau truc AuditServiceImpl — tao IAuditRepository

### Uu tien trung binh
  6. Fix RateLimitFilter dung ContentCachingRequestWrapper
  7. Tat show-sql o production profile
  8. Them Idempotency Key cho Deposit/Transfer

### Uu tien thap (Toi uu hoa)
  9. Redis Rate Limiting cho horizontal scaling
  10. Structured Logging (JSON format cho production)
  11. Caching layer cho cac query thuong dung
  12. Transaction amount limits / daily limits

---

## KET LUAN

  Diem tong:    7.6 / 10
  Diem tot:     Token Rotation - Double-Entry Accounting - Clean Architecture - OTP Security
  Diem yeu:     Khong co tests - @Version chua active - Exception message bi an

Day la du an backend chat luong tot voi kien truc ro rang va cac tinh nang bao mat enterprise-grade.
Voi mot vai cai thien trong diem (testing + @Version + exception handling),
du an hoan toan co the dat 8.5+ / 10.
