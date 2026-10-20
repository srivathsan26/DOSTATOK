-- DOSTATOK - Personal Financial Management System
-- SQLite Schema Definition

PRAGMA foreign_keys = ON;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    full_name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    age INTEGER,
    occupation TEXT,
    address TEXT,
    annual_income DECIMAL(15,2),
    currency TEXT NOT NULL DEFAULT 'INR',
    created_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
);

-- 2. Bank Accounts Table (Supports Polymorphic Account Types: SAVINGS & CURRENT)
CREATE TABLE IF NOT EXISTS bank_accounts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    account_number TEXT NOT NULL UNIQUE,
    account_name TEXT NOT NULL,
    account_type TEXT NOT NULL CHECK(account_type IN ('SAVINGS', 'CURRENT')),
    bank_name TEXT NOT NULL,
    balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    minimum_balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    overdraft_limit DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    interest_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    status TEXT NOT NULL DEFAULT 'ACTIVE' CHECK(status IN ('ACTIVE', 'FROZEN', 'CLOSED')),
    created_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
);

-- 3. Transactions Table (Atomic ledger of all deposits, withdrawals, and transfers)
CREATE TABLE IF NOT EXISTS transactions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    account_id INTEGER NOT NULL REFERENCES bank_accounts(id) ON DELETE CASCADE,
    transaction_type TEXT NOT NULL CHECK(transaction_type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER_IN', 'TRANSFER_OUT')),
    amount DECIMAL(15,2) NOT NULL CHECK(amount > 0),
    balance_after DECIMAL(15,2) NOT NULL,
    category TEXT NOT NULL,
    description TEXT,
    transaction_date TEXT NOT NULL DEFAULT (datetime('now', 'localtime')),
    created_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
);

-- 4. Income Table
CREATE TABLE IF NOT EXISTS income (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    account_id INTEGER REFERENCES bank_accounts(id) ON DELETE SET NULL,
    title TEXT NOT NULL,
    amount DECIMAL(15,2) NOT NULL CHECK(amount > 0),
    category TEXT NOT NULL,
    source TEXT NOT NULL,
    income_date TEXT NOT NULL,
    notes TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
);

-- 5. Expenses Table
CREATE TABLE IF NOT EXISTS expenses (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    account_id INTEGER REFERENCES bank_accounts(id) ON DELETE SET NULL,
    title TEXT NOT NULL,
    amount DECIMAL(15,2) NOT NULL CHECK(amount > 0),
    category TEXT NOT NULL,
    payment_method TEXT NOT NULL DEFAULT 'BANK_TRANSFER',
    expense_date TEXT NOT NULL,
    notes TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
);

-- 6. Investments Table
CREATE TABLE IF NOT EXISTS investments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    investment_type TEXT NOT NULL,
    invested_amount DECIMAL(15,2) NOT NULL CHECK(invested_amount >= 0),
    current_value DECIMAL(15,2) NOT NULL CHECK(current_value >= 0),
    purchase_date TEXT NOT NULL,
    notes TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
);

-- 7. Assets Table (Movable and Immovable physical assets)
CREATE TABLE IF NOT EXISTS assets (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    asset_category TEXT NOT NULL CHECK(asset_category IN ('MOVABLE', 'IMMOVABLE')),
    purchase_value DECIMAL(15,2) NOT NULL CHECK(purchase_value >= 0),
    current_value DECIMAL(15,2) NOT NULL CHECK(current_value >= 0),
    acquisition_date TEXT NOT NULL,
    notes TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
);

-- 8. Loans Table (Liabilities)
CREATE TABLE IF NOT EXISTS loans (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    loan_name TEXT NOT NULL,
    lender TEXT NOT NULL,
    principal_amount DECIMAL(15,2) NOT NULL CHECK(principal_amount > 0),
    interest_rate DECIMAL(5,2) NOT NULL CHECK(interest_rate >= 0),
    tenure_months INTEGER NOT NULL CHECK(tenure_months > 0),
    start_date TEXT NOT NULL,
    outstanding_balance DECIMAL(15,2) NOT NULL CHECK(outstanding_balance >= 0),
    monthly_emi DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    status TEXT NOT NULL DEFAULT 'ACTIVE' CHECK(status IN ('ACTIVE', 'CLOSED')),
    created_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
);

-- 9. Loan Repayments Table
CREATE TABLE IF NOT EXISTS loan_repayments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    loan_id INTEGER NOT NULL REFERENCES loans(id) ON DELETE CASCADE,
    account_id INTEGER REFERENCES bank_accounts(id) ON DELETE SET NULL,
    amount DECIMAL(15,2) NOT NULL CHECK(amount > 0),
    principal_component DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    interest_component DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    payment_date TEXT NOT NULL,
    notes TEXT,
    created_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
);

-- 10. Financial Goals Table
CREATE TABLE IF NOT EXISTS financial_goals (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    target_amount DECIMAL(15,2) NOT NULL CHECK(target_amount > 0),
    current_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00 CHECK(current_amount >= 0),
    target_date TEXT NOT NULL,
    category TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'IN_PROGRESS' CHECK(status IN ('IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    created_at TEXT NOT NULL DEFAULT (datetime('now', 'localtime'))
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_accounts_user ON bank_accounts(user_id);
CREATE INDEX IF NOT EXISTS idx_transactions_account ON transactions(account_id);
CREATE INDEX IF NOT EXISTS idx_transactions_user ON transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_income_user ON income(user_id);
CREATE INDEX IF NOT EXISTS idx_expenses_user ON expenses(user_id);
CREATE INDEX IF NOT EXISTS idx_investments_user ON investments(user_id);
CREATE INDEX IF NOT EXISTS idx_assets_user ON assets(user_id);
CREATE INDEX IF NOT EXISTS idx_loans_user ON loans(user_id);
CREATE INDEX IF NOT EXISTS idx_goals_user ON financial_goals(user_id);

