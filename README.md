# 🏦 Banking Application - Spring Boot

> Enterprise-grade Banking System with Layered Architecture, SOLID Principles, and Clean Architecture

[![Java](https://img.shields.io/badge/Java-17-red)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.3-brightgreen)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14%2B-blue)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Quick Start](#-quick-start)
- [Project Structure](#-project-structure)
- [API Endpoints](#-api-endpoints)
- [Learning Resources](#-learning-resources)
- [Contributing](#-contributing)

---

## 🎯 Overview

Modern banking application built with **Spring Boot** following **Clean Architecture** principles. The system provides user authentication, account management, transaction processing, and admin functionalities with enterprise-level security.

### Key Highlights:
- ✅ **Layered Architecture** (Presentation → Application → Domain → Infrastructure)
- ✅ **SOLID Principles** throughout the codebase
- ✅ **JWT Authentication** with refresh tokens
- ✅ **Role-Based Access Control** (RBAC)
- ✅ **Repository Pattern** with clean abstractions
- ✅ **Auto-initialization** of admin account and permissions

---

## ✨ Features

### 🔐 Authentication & Authorization
- [x] User Registration with validation
- [x] User Login with JWT tokens
- [x] Role-based permissions (USER, ADMIN)
- [x] Password encryption (BCrypt)
- [x] Refresh Token mechanism (with Token Rotation)
- [x] Password Reset flow (via Email OTP)
- [x] Email verification with OTP for new accounts

### 👨‍💼 User Management
- [x] User entity with status management
- [x] Multiple roles per user
- [x] PIN management with lockout
- [ ] User profile update
- [ ] Account deactivation

### 💰 Account Management
- [ ] Create bank accounts (Savings, Checking, Investment)
- [ ] Account balance tracking
- [ ] Account status management (Active, Frozen, Closed)
- [ ] Multiple accounts per user

### 💸 Transaction Processing
- [ ] Deposit funds
- [ ] Withdraw funds
- [ ] Transfer between accounts
- [ ] Transaction history
- [ ] Double-entry bookkeeping
- [ ] Transaction approval workflow

### 📊 Admin Features
- [x] Auto-created admin account
- [ ] User management APIs
- [ ] Transaction approval
- [ ] System audit logs
- [ ] Reports and analytics

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────┐
│          API/Presentation Layer                      │
│  - REST Controllers                                  │
│  - Exception Handlers                                │
└──────────────────┬──────────────────────────────────┘
                   │ DTOs
┌──────────────────▼──────────────────────────────────┐
│          Application Layer                           │
│  - Use Cases / Services                             │
│  - DTOs (Request/Response)                          │
│  - Validators                                       │
└──────────────────┬──────────────────────────────────┘
                   │ Domain Models
┌──────────────────▼──────────────────────────────────┐
│          Domain Layer (Pure Business Logic)          │
│  - Entities (User, Account, Transaction)            │
│  - Business Rules                                   │
│  - Enums                                            │
└──────────────────┬──────────────────────────────────┘
                   │ Repository Interfaces
┌──────────────────▼──────────────────────────────────┐
│          Infrastructure Layer                        │
│  - JPA Entities & Repositories                      │
│  - Security (JWT, Spring Security)                  │
│  - Database Configuration                           │
│  - Mappers (Domain ↔ Entity)                        │
└──────────────────┬──────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────┐
│          Database (PostgreSQL)                       │
└─────────────────────────────────────────────────────┘
```

---

## 🛠️ Tech Stack

### Backend
- **Java 17** - Programming language
- **Spring Boot 4.0.3** - Application framework
- **Spring Security** - Authentication & authorization
- **Spring Data JPA** - Data access layer
- **Hibernate** - ORM framework

### Database
- **PostgreSQL 14+** - Primary database

### Security
- **JWT (JJWT 0.12.3)** - Token-based authentication
- **BCrypt** - Password hashing

### Utilities
- **Lombok** - Reduce boilerplate code
- **Maven** - Dependency management

---

## 🚀 Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.8+
- PostgreSQL 14+

### 1. Clone the repository
```bash
git clone https://github.com/NguyenVanNam121204/be_java_mini_banking_app.git
cd banking-app
```

### 2. Setup PostgreSQL
```sql
CREATE DATABASE yourdb;
```

### 3. Configure application.properties
Update `src/main/resources/application.properties` if needed:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/yourdb
spring.datasource.username=yourusername
spring.datasource.password=yourpassword
```

### 4. Build & Run
```bash
# Clean and build
mvn clean install

# Run application
mvn spring-boot:run
```

### 5. Test the API
```bash
# Health check
curl http://localhost:8080/api/auth/test
```

Application will start on **http://localhost:8080**

### 6. Login with Admin Account
```json
POST http://localhost:8080/api/auth/login

{
  "username": "admin",
  "password": "12122004"
}
```

📧 **Admin Email**: `nambo@gmail.com`

---

## 📁 Project Structure

```
banking-app/
├── src/main/java/com/bankapp/bankingapp/
│   ├── Api_presentation/         # REST Controllers
│   ├── application/              # Services, DTOs, Validators, Mappers
│   ├── domain/                   # Business Entities & Logic
│   ├── infrastructure/           # JPA, Security, Config
│   └── common/                   # Shared utilities
├── src/main/resources/
│   └── application.properties    # Configuration
├── pom.xml                       # Maven dependencies
```

**Total**: 62 Java files across 26 packages

---

## 🌐 API Endpoints

### Authentication
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/register` | Đăng ký tài khoản (gửi OTP) | ❌ |
| POST | `/api/auth/verify-email` | Xác thực email bằng OTP | ❌ |
| POST | `/api/auth/resend-verification`| Gửi lại mã OTP xác thực email| ❌ |
| POST | `/api/auth/login` | Đăng nhập | ❌ |
| POST | `/api/auth/refresh-token`| Làm mới Access Token (Rotation) | ❌ |
| POST | `/api/auth/forgot-password`| Quên mật khẩu (gửi OTP) | ❌ |
| POST | `/api/auth/reset-password` | Đặt lại mật khẩu bằng OTP | ❌ |
| GET | `/api/auth/test` | Health check | ❌ |

### Coming Soon
- Account Management APIs
- Transaction APIs
- Admin APIs

---

## 🔐 Security

### Implemented
- ✅ JWT token-based authentication
- ✅ BCrypt password hashing
- ✅ Role-based access control (RBAC)
- ✅ Stateless session management
- ✅ CORS configuration
- ✅ SQL injection prevention (JPA)

### Planned
- ⏳ Multi-factor authentication (MFA)
- ⏳ Rate limiting
- ⏳ IP whitelisting for admin
- ⏳ Audit logging

---

## 🧪 Testing

### Run Tests
```bash
mvn test
```

### Test Coverage
- Unit tests for validators
- Integration tests (planned)
- E2E API tests (planned)

---

## 📖 Learning Resources

Dành cho các bạn mới muốn tham khảo và học hỏi từ dự án này, dưới đây là các khái niệm và tài liệu tham khảo chính (Tech Stack) định hình nên kiến trúc và logic code của hệ thống:

### 1. Hệ sinh thái Spring Boot & Java
- **Spring Boot Core**: [Spring Boot Reference Guide](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- **Lombok**: [Project Lombok Features](https://projectlombok.org/features/) - Thư viện giúp giảm thiểu đáng kể các đoạn code khuôn mẫu (getters, setters, constructors,...).
- **Spring Boot Mail**: Cách tích hợp JavaMailSender để gửi mã OTP tự động qua thư điện tử.

### 2. Kiến trúc Server & Design Pattern
- **Clean Architecture**: [The Clean Architecture - Uncle Bob](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html) - Đây là lý do cốt lõi tạo sao dự án được chia rõ rệt thành: Presentation, Application, Domain và Infrastructure.
- **SOLID Principles**: [Các nguyên lý SOLID trong Java](https://www.baeldung.com/solid-principles) - Chìa khóa để code dễ bảo trì và dễ scale mở rộng.
- **Repository Pattern**: [Spring Data JPA Core Concepts](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories) - Tách biệt logic truy cập dữ liệu để các tầng nghiệp vụ phía trên không cần lo việc kết nối CSDL thao tác ra sao.

### 3. Xác thực bảo mật (Security & JWT)
- **Spring Security Architecture**: [Spring Security Documentation](https://docs.spring.io/spring-security/reference/index.html) - Trái tim của việc cấp quyền, chặn filter, mã hóa password bằng BCrypt.
- **JSON Web Tokens (JWT)**: [Giới thiệu về JWT (jwt.io)](https://jwt.io/introduction) - Token Authorization mang theo thông tin user dưới khối mã hoá để đăng nhập không trạng thái (stateless).
- **Refresh Token Pattern**: Nắm bắt luồng lấy lại Access Token mới tự động dựa trên Refresh Token lưu lâu dài hơn, đảm bảo trải nghiệm người dùng không phải login lại.

### 4. Database & ORM (Quản trị Dữ liệu)
- **PostgreSQL**: [PostgreSQL Official](https://www.postgresql.org/docs/) - Hệ quản trị cơ sở dữ liệu Relation Database lưu các Table người dùng, OTP Code và sau này là Giao dịch...
- **Hibernate / JPA**: [Baeldung - Spring Data JPA Guide](https://www.baeldung.com/the-persistence-layer-with-spring-data-jpa) - Cách map các thực thể (Entity) trong Code dính liền xuống cấu trúc Cột bảng trong Database tự động. Thích hợp cho CRUD.

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Coding Standards
- Follow SOLID principles
- Write clean, readable code
- Add unit tests for new features
- Update documentation

---

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- Spring Boot team for the awesome framework
- PostgreSQL community
- Open source contributors

---

## 🗺️ Roadmap

### Phase 1: Authentication (✅ Completed)
- [x] User registration
- [x] User login
- [x] JWT authentication
- [x] Admin account initialization

### Phase 2: Account Management (🚧 In Progress)
- [ ] Create accounts
- [ ] View account details
- [ ] Account status management

### Phase 3: Transactions
- [ ] Deposit
- [ ] Withdraw
- [ ] Transfer
- [ ] Transaction history

### Phase 4: Admin Features
- [ ] User management
- [ ] Transaction approval
- [ ] Audit logs
- [ ] Reports

### Phase 5: Advanced Features
- [x] Password reset (OTP)
- [x] Email notifications (OTP)
- [ ] MFA
- [ ] Rate limiting

---

**Status**: ✅ Phase 1 Complete - Ready for Development

**Last Updated**: March 6, 2026

---

## 📞 Support
For support, email: nguyenvannam121204@gmail.com

---

## 👨‍💻 Author

**Nguyễn Văn Nam**

---

**Made with ❤️ using Spring Boot**
