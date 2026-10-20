# Java OOP Viva Guide

## Questions and concise answers

1. **What is a class and an object?** A class defines state and behavior; an object is one instance. `BankAccount` is the class and each row loaded by `AccountRepositoryImpl` becomes an account object.
2. **How is encapsulation used?** `BankAccount` keeps its fields private and exposes methods such as `deposit` and `withdraw`, which apply balance rules.
3. **Where is inheritance demonstrated?** `SavingsAccount` and `CurrentAccount` extend `BankAccount` and implement account-specific withdrawal rules.
4. **What is abstraction here?** `AccountOperations` describes account actions without tying callers to a particular account subtype.
5. **What is runtime polymorphism?** `AccountService` invokes account behavior through the common `BankAccount` type; the runtime subtype supplies the overridden behavior.
6. **Overloading versus overriding?** Overloading uses the same method name with different parameter lists. Overriding replaces inherited behavior in a subtype; the account classes override withdrawal behavior.
7. **Why use interfaces for repositories?** Services depend on `AccountRepository` and `FinanceRepository` contracts, so SQL implementation details stay in the DAO layer.
8. **What is a checked exception?** Java requires callers to handle or declare it. JDBC operations use checked `SQLException`; domain failures use the project's `DostatokException` hierarchy.
9. **How does the project demonstrate a transaction?** `AccountRepositoryImpl` and `TransactionRepositoryImpl` coordinate balance and ledger changes so a failed operation can be rolled back.
10. **How does authentication identify an owner?** The backend resolves an HttpOnly session cookie to a server-side user ID, then scopes reads and verifies record ownership before access.

## Five-minute demonstration

1. Open `BankAccount.java`; show private state, validation, and controlled operations.
2. Open `SavingsAccount.java` and `CurrentAccount.java`; compare overridden rules.
3. Open `AccountOperations.java`; show the abstraction used by the domain.
4. Open `AccountService.java`; follow validation into repository calls.
5. Open `AccountRepositoryImpl.java`; show prepared SQL and transaction boundaries.
6. Run `OopDemo` using the command in the root README.
7. Explain that `ApiController` reads HTTP input, while services and models own business behavior.

## Source references

- `backend/src/com/dostatok/model/BankAccount.java`
- `backend/src/com/dostatok/model/SavingsAccount.java`
- `backend/src/com/dostatok/model/CurrentAccount.java`
- `backend/src/com/dostatok/model/AccountOperations.java`
- `backend/src/com/dostatok/service/AccountService.java`
- `backend/src/com/dostatok/dao/AccountRepository.java`
- `backend/src/com/dostatok/dao/AccountRepositoryImpl.java`
- `backend/src/com/dostatok/main/OopDemo.java`
