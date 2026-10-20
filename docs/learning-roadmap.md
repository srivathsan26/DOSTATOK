# Practical Learning Roadmap

Work through these lessons in order. Each exercise refers to the current source.

## 1. Java OOP foundations

Read `backend/src/com/dostatok/model/BankAccount.java` and `OopDemo.java`. A class groups state and behavior; an object is one concrete account. **Exercise:** explain why balance changes go through methods. **Viva:** how does a constructor establish a valid object?

## 2. Packages and exceptions

Read `backend/src/com/dostatok/exception/` and compare model, service, and DAO packages. Exceptions carry failures upward to the HTTP boundary. **Exercise:** trace `InsufficientBalanceException`. **Viva:** when should a checked exception be used?

## 3. Domain models and services

Read `BankAccount.java`, `SavingsAccount.java`, and `AccountService.java`. Models express account behavior; services coordinate workflows. **Exercise:** trace a withdrawal from service validation to model behavior. **Viva:** why should a service not parse an HTTP request?

## 4. JDBC and SQL

Read `backend/src/com/dostatok/dao/AccountRepositoryImpl.java` and `database/schema.sql`. JDBC runs prepared SQL; SQLite persists rows and enforces foreign keys. **Exercise:** identify the account primary key and its user foreign key. **Viva:** why use a prepared statement?

## 5. HTTP API architecture

Read `App.java`, `ApiController.java`, and `HttpHelper.java`. **Exercise:** follow `GET /api/accounts` from route to JSON. **Viva:** what distinguishes a 401 from a 404?

## 6. React state, forms, and API calls

Read `frontend/src/App.jsx` and `frontend/src/services/api.js`. React owns form interaction; Java remains the source of truth. **Exercise:** locate a deposit form and the API call it triggers. **Viva:** what causes a React component to render again?

## 7. Authentication and user isolation

Read `AuthService.java`, `PasswordHasher.java`, `SessionStore.java`, and the controller's ownership checks. **Exercise:** trace the cookie from login to account query. **Viva:** why is a client-provided user ID not authorization?

## 8. Financial calculations

Read `FinancialTransaction.java`, `Loan.java`, and `DashboardSummary.java`. Monetary calculations use `BigDecimal`. **Exercise:** explain transfer-in and transfer-out ledger rows. **Viva:** why avoid binary floating-point for currency?

## 9. Testing and debugging

Read `backend/src/com/dostatok/main/SecurityTestSuite.java` and `docs/testing.md`. **Exercise:** add a test for an expired session. **Viva:** why should tests use a temporary database copy?

Try each viva question before looking up the answer in the source and this guide.
