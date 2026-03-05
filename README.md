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
- [Documentation](#-documentation)
- [Project Structure](#-project-structure)
- [API Endpoints](#-api-endpoints)
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
- [ ] Refresh Token mechanism
- [ ] Password Reset flow

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

👉 See [ARCHITECTURE.md](ARCHITECTURE.md) for detailed architecture explanation.

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
git clone <repository-url>
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

## 📚 Documentation

| Document | Description |
|----------|-------------|
| [SETUP_GUIDE.md](SETUP_GUIDE.md) | Complete setup instructions |
| [API_TESTING_GUIDE.md](API_TESTING_GUIDE.md) | API testing with Postman |
| [ARCHITECTURE.md](ARCHITECTURE.md) | Architecture & design patterns |
| [ADMIN_ACCOUNT.md](ADMIN_ACCOUNT.md) | Admin account details |
| [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) | Project structure overview |

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
└── docs/                         # Documentation files
```

**Total**: 62 Java files across 26 packages

👉 See [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) for detailed structure.

---

## 🌐 API Endpoints

### Authentication
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/register` | Register new user | ❌ |
| POST | `/api/auth/login` | Login user | ❌ |
| GET | `/api/auth/test` | Health check | ❌ |

### Coming Soon
- Account Management APIs
- Transaction APIs
- Admin APIs
- Password Reset
- Refresh Token

👉 See [API_TESTING_GUIDE.md](API_TESTING_GUIDE.md) for detailed API documentation.

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
- ⏳ Token refresh mechanism

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

## 👨‍💻 Author

**Nguyen Van Nam**

---

## 🙏 Acknowledgments

- Spring Boot team for the awesome framework
- PostgreSQL community
- Open source contributors

---

## 📞 Support

For support, email: nguyenvannam121204@gmail.com

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
- [ ] Password reset
- [ ] MFA
- [ ] Rate limiting
- [ ] Email notifications

---

**Status**: ✅ Phase 1 Complete - Ready for Development

**Last Updated**: March 5, 2026

---

**Made with ❤️ using Spring Boot**
