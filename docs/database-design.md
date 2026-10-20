# DOSTATOK — Database Design & Schema Architecture

**Database Engine**: SQLite 3.53  
**Persistence Layer**: JDBC Driver (`org.sqlite.JDBC`) with `PRAGMA foreign_keys = ON;`  
**Storage Location**: `database/dostatok.db` (auto-created on startup)

---

## 1. Entity-Relationship (ER) Diagram

```mermaid
erDiagram
    users ||--o{ bank_accounts : owns
    users ||--o{ transactions : records
    users ||--o{ income : receives
    users ||--o{ expenses : incurs
    users ||--o{ investments : holds
    users ||--o{ assets : possesses
    users ||--o{ loans : owes
    users ||--o{ financial_goals : tracks
    bank_accounts ||--o{ transactions : logs
    bank_accounts ||--o{ income : credits
    bank_accounts ||--o{ expenses : debits
    loans ||--o{ loan_repayments : services
    bank_accounts ||--o{ loan_repayments : funds

    users {
        int id PK
        string username UK
        string full_name
        string email UK
        string currency
        datetime created_at
    }

    bank_accounts {
        int id PK
        int user_id FK
        string account_number UK
        string account_name
        string account_type "SAVINGS | CURRENT"
        string bank_name
        decimal balance
        decimal minimum_balance
        decimal overdraft_limit
        decimal interest_rate
        string status
        datetime created_at
    }

    transactions {
        int id PK
        int user_id FK
        int account_id FK
        string transaction_type "DEPOSIT | WITHDRAWAL | TRANSFER_IN | TRANSFER_OUT"
        decimal amount
        decimal balance_after
        string category
        string description
        datetime transaction_date
        datetime created_at
    }

    income {
        int id PK
        int user_id FK
        int account_id FK
        string title
        decimal amount
        string category
        string source
        string income_date
        string notes
        datetime created_at
    }

    expenses {
        int id PK
        int user_id FK
        int account_id FK
        string title
        decimal amount
        string category
        string payment_method
        string expense_date
        string notes
        datetime created_at
    }

    investments {
        int id PK
        int user_id FK
        string name
        string investment_type
        decimal invested_amount
        decimal current_value
        string purchase_date
        string notes
        datetime created_at
    }

    assets {
        int id PK
        int user_id FK
        string name
        string asset_category "MOVABLE | IMMOVABLE"
        decimal purchase_value
        decimal current_value
        string acquisition_date
        string notes
        datetime created_at
    }

    loans {
        int id PK
        int user_id FK
        string loan_name
        string lender
        decimal principal_amount
        decimal interest_rate
        int tenure_months
        string start_date
        decimal outstanding_balance
        decimal monthly_emi
        string status "ACTIVE | CLOSED"
        datetime created_at
    }

    loan_repayments {
        int id PK
        int loan_id FK
        int account_id FK
        decimal amount
        decimal principal_component
        decimal interest_component
        string payment_date
        string notes
        datetime created_at
    }

    financial_goals {
        int id PK
        int user_id FK
        string title
        decimal target_amount
        decimal current_amount
        string target_date
        string category
        string status "IN_PROGRESS | COMPLETED | CANCELLED"
        datetime created_at
    }
```

---

## 2. Table Specifications and Business Invariants

### 1. `users`
- Stores primary user identity and currency preferences.
- Default demo user seeded as `Aditya Sharma` (`INR`).

### 2. `bank_accounts`
- Core table backing the polymorphic `BankAccount` domain hierarchy.
- `account_type` column constrained to `SAVINGS` or `CURRENT`.
- `minimum_balance`: Enforced for `SAVINGS` accounts during withdrawals.
- `overdraft_limit`: Enforced for `CURRENT` accounts permitting negative balance down to `-overdraft_limit`.
- Atomic balance updates ensure no race conditions.

### 3. `transactions`
- Immutable ledger recording all monetary debits, credits, and transfers.
- Stores `balance_after` to preserve full audit trails.
- Linked to `bank_accounts(id)` with `ON DELETE CASCADE`.

### 4. `income` & `expenses`
- Operating cash flow records with categorization (Salary, Consulting, Food, Rent, Business).
- Optionally linked to a `bank_accounts(id)` for automated balance adjustments.

### 5. `investments`
- Capital market instruments (Mutual Funds, Equities, Gold, Fixed Deposits).
- Stores `invested_amount` and `current_value` to derive real-time capital gains and ROI.

### 6. `assets`
- Tangible physical capital divided into `MOVABLE` (Vehicles, Machinery, Electronics) and `IMMOVABLE` (Real Estate, Land).
- Enables calculation of gross physical wealth.

### 7. `loans` & `loan_repayments`
- Liabilities tracking borrowing, tenure, and amortization.
- `monthly_emi` pre-calculated using standard banking amortization formulas.
- Repayments reduce `outstanding_balance` atomically.

### 8. `financial_goals`
- Target-based savings milestones.
- Tracks `target_amount` and accumulated `current_amount` with safe division-by-zero protection.

---

## 3. Monetary Representation Standard

All monetary amounts are stored in SQLite using `DECIMAL(15,2)` and mapped into Java `BigDecimal` with 2 decimal places and `RoundingMode.HALF_UP`.
- **Zero Floating-Point Imprecision**: Binary `double` and `float` are strictly forbidden for financial calculations to prevent IEEE-754 rounding errors.
- **Indian Financial Localization**: Displayed across the UI using the `en-IN` numbering grouping convention (`₹1,50,000.00`).

