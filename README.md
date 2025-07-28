 Payment Transfer Service

A Spring Boot-based microservice for handling secure money transfers between user accounts with comprehensive audit trails and transaction management.

 Overview

The Payment Transfer Service provides RESTful APIs for:

- 👤 User registration and management
- 🏦 Account creation and management
- 💸 Secure money transfers between accounts
- 📜 Transaction history and audit trails
- ⚡ Real-time balance updates

 Architecture

 Technology Stack
- Framework: Spring Boot 3.x
- Database: JPA/Hibernate with PostgreSQL
- Security: Spring Security with JWT authentication
- Documentation: OpenAPI 3.0 (Swagger)
- Testing: JUnit 5, Mockito
- Build Tool: Maven

 Key Components

 Services Layer
- PaymentTransferService: Core business logic for money transfers
- UserService: User registration and management
- AccountService: Account creation and management
- AuditService: Transaction audit logging with separate transaction boundaries

 Controllers Layer
- PaymentController: Transfer operations with comprehensive API documentation
- TransactionController: Transaction history and retrieval
- AuditController: Audit trail queries and reporting

 API Endpoints

 Payment Operations
```http
POST /api/v1/payments/transfer
GET  /api/v1/payments/transactions/{transactionId}
```

 Transaction Management
```http
GET /api/v1/transactions/{transactionId}
GET /api/v1/transactions/user/{userId}
GET /api/v1/transactions/account/{accountId}
```

 Audit Operations
```http
GET /api/v1/audit/transaction/{transactionId}
GET /api/v1/audit/user/{userId}
GET /api/v1/audit/account/{accountId}
GET /api/v1/audit/failed
GET /api/v1/audit/daily?date={date}
```

 Key Features

 🔒 Security
- JWT-based authentication
- Role-based access control
- Account ownership validation
- Secure password encoding

 💰 Transfer Processing
- Atomic Transactions: All transfers are fully ACID-compliant
- Deadlock Prevention: Consistent account locking order
- Balance Validation: Real-time insufficient funds checking
- Currency Support: Multi-currency account handling
- Duplicate Prevention: Reference-based deduplication

 📊 Audit & Compliance
- Complete Audit Trail: Every transfer attempt is logged
- Separate Transaction Boundaries: Audit records persist even if main transaction fails
- Balance Tracking: Before/after balances for all successful transfers
- Failed Transaction Logging: Detailed error messages and context

 🔄 Transaction Types
- Internal Transfers: Between user's own accounts
- External Transfers: Legacy support for external transfers
- Status Tracking: PENDING → COMPLETED/FAILED workflow

 Data Models

 Core Entities
- User: User account information and authentication
- Account: Financial accounts with balances and metadata
- Transaction: Transfer records with full transaction details
- TransactionAudit: Immutable audit logs with balance history

 Account Types
- Savings
- Checking
- Business
- Investment

 Transaction Statuses
- `PENDING`: Transfer initiated but not completed
- `COMPLETED`: Transfer successfully processed
- `FAILED`: Transfer failed due to validation or system errors

Security Considerations

 Authentication
- JWT token-based authentication
- Token expiration and refresh handling
- Role-based access control

 Data Protection
- Password hashing with BCrypt
- Sensitive data encryption
- Account ownership validation
- Audit log immutability

 Transfer Security
- Atomic transaction processing
- Duplicate transaction prevention
- Real-time balance validation
- Comprehensive audit trails


 Getting Started

 Prerequisites
- Java 17+
- Maven 3.6+
- Database (PostgreSQL)

 Running the Application

1. Clone and build:
   ```bash
   git clone <repository-url>
   cd payment-transfer-service
   mvn clean install
   ```

2. Configure database in `application.yml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/payment_db
       username: your_username
       password: your_password
   ```

3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

4. Access Swagger UI:
   ```
   http://localhost:8080/swagger-ui.html
   ```


 Error Handling

 Common Error Codes
- `USER_NOT_FOUND`: Invalid user ID
- `ACCOUNT_ACCESS_DENIED`: Insufficient permissions
- `INSUFFICIENT_FUNDS`: Account balance too low
- `SAME_ACCOUNT`: Source and destination cannot be identical
- `CURRENCY_MISMATCH`: Different currencies between accounts
- `ACCOUNT_INACTIVE`: Account is not in active status

 Error Response Format
```json
{
  "success": false,
  "message": "Insufficient funds",
  "errorCode": "INSUFFICIENT_FUNDS",
  "timestamp": "2025-01-28T10:30:00Z"
}
```


 
