-- DOSTATOK - Personal Financial Management System
-- Seed Demo Data

-- 1. Demo User
INSERT OR IGNORE INTO users (id, username, full_name, email, currency)
VALUES (1, 'demo_user', 'Aditya Sharma', 'aditya.sharma@dostatok.in', 'INR');

-- 2. Bank Accounts
-- HDFC Savings Account (with ₹2,500 minimum balance policy)
INSERT OR IGNORE INTO bank_accounts (id, user_id, account_number, account_name, account_type, bank_name, balance, minimum_balance, overdraft_limit, interest_rate, status)
VALUES (1, 1, 'HDFC00129841', 'Primary Savings Account', 'SAVINGS', 'HDFC Bank', 145000.00, 2500.00, 0.00, 3.50, 'ACTIVE');

-- ICICI Current Account (with ₹50,000 overdraft limit policy)
INSERT OR IGNORE INTO bank_accounts (id, user_id, account_number, account_name, account_type, bank_name, balance, minimum_balance, overdraft_limit, interest_rate, status)
VALUES (2, 1, 'ICIC99402315', 'Freelance & Business Current', 'CURRENT', 'ICICI Bank', 82500.00, 0.00, 50000.00, 0.00, 'ACTIVE');

-- SBI Emergency Savings Account
INSERT OR IGNORE INTO bank_accounts (id, user_id, account_number, account_name, account_type, bank_name, balance, minimum_balance, overdraft_limit, interest_rate, status)
VALUES (3, 1, 'SBIN00561234', 'Emergency Liquid Reserve', 'SAVINGS', 'State Bank of India', 50000.00, 1000.00, 0.00, 3.00, 'ACTIVE');

-- 3. Initial Transactions
INSERT OR IGNORE INTO transactions (id, user_id, account_id, transaction_type, amount, balance_after, category, description, transaction_date)
VALUES 
(1, 1, 1, 'DEPOSIT', 120000.00, 120000.00, 'Salary', 'Monthly Tech Lead Salary credit', '2026-10-01 10:00:00'),
(2, 1, 1, 'WITHDRAWAL', 15000.00, 105000.00, 'Housing', 'Apartment maintenance & utility advance', '2026-10-03 14:30:00'),
(3, 1, 1, 'DEPOSIT', 40000.00, 145000.00, 'Dividends', 'Quarterly equity & fund dividend payout', '2026-10-07 11:15:00'),
(4, 1, 2, 'DEPOSIT', 95000.00, 95000.00, 'Consulting', 'UI/UX Design retainer project milestone', '2026-10-05 16:45:00'),
(5, 1, 2, 'WITHDRAWAL', 12500.00, 82500.00, 'Software', 'Cloud hosting & software subscriptions', '2026-10-08 09:20:00'),
(6, 1, 3, 'DEPOSIT', 50000.00, 50000.00, 'Savings', 'Emergency fund allocation deposit', '2026-10-02 18:00:00');

-- 4. Income Records
INSERT OR IGNORE INTO income (id, user_id, account_id, title, amount, category, source, income_date, notes)
VALUES
(1, 1, 1, 'Monthly Tech Salary', 120000.00, 'Salary', 'Apex Technologies Corp', '2026-10-01', 'Direct payroll deposit'),
(2, 1, 2, 'Design Consulting Milestone', 95000.00, 'Consulting', 'Finovate Digital Client', '2026-10-05', 'Milestone 2 deliverable invoice cleared'),
(3, 1, 1, 'Stock Portfolio Dividend', 40000.00, 'Investments', 'Nifty 50 Index Holdings', '2026-10-07', 'Interim dividend payout');

-- 5. Expense Records
INSERT OR IGNORE INTO expenses (id, user_id, account_id, title, amount, category, payment_method, expense_date, notes)
VALUES
(1, 1, 1, 'Apartment Maintenance', 15000.00, 'Housing', 'NET_BANKING', '2026-10-03', 'Monthly maintenance and society dues'),
(2, 1, 2, 'Cloud Server Infrastructure', 12500.00, 'Business', 'DEBIT_CARD', '2026-10-08', 'AWS & Vercel server bills'),
(3, 1, 1, 'Gourmet Dining & Groceries', 8200.00, 'Food', 'UPI', '2026-10-09', 'Weekly pantry restocking and weekend meal'),
(4, 1, 1, 'Electric Vehicle Charging', 2400.00, 'Transportation', 'UPI', '2026-10-10', 'Monthly fast charging credits');

-- 6. Investments (Mutual funds, Stocks, Digital Gold, Fixed Deposit)
INSERT OR IGNORE INTO investments (id, user_id, name, investment_type, invested_amount, current_value, purchase_date, notes)
VALUES
(1, 1, 'Mirae Asset Large Cap Fund', 'Mutual Fund', 150000.00, 182400.00, '2024-03-15', 'Systematic Investment Plan (SIP) units'),
(2, 1, 'Parag Parikh Flexi Cap Fund', 'Mutual Fund', 200000.00, 248600.00, '2023-11-20', 'Long term capital appreciation'),
(3, 1, 'Tata Motors & L&T Equities', 'Stocks', 120000.00, 142800.00, '2024-06-10', 'Direct bluechip equity portfolio'),
(4, 1, 'Sovereign Gold Bonds (SGB)', 'Gold', 100000.00, 126500.00, '2023-08-01', 'RBI Sovereign Gold Bond 2.5% p.a.');

-- 7. Assets (Movable & Immovable)
INSERT OR IGNORE INTO assets (id, user_id, name, asset_category, purchase_value, current_value, acquisition_date, notes)
VALUES
(1, 1, 'Tata Nexon EV Max', 'MOVABLE', 1850000.00, 1580000.00, '2024-01-15', 'Electric vehicle daily commuter'),
(2, 1, 'Bangalore Urban Residential Plot (1200 sqft)', 'IMMOVABLE', 3200000.00, 4100000.00, '2022-09-01', 'North Bangalore appreciating sub-urban zone');

-- 8. Loans (Liabilities)
-- Vehicle Loan
INSERT OR IGNORE INTO loans (id, user_id, loan_name, lender, principal_amount, interest_rate, tenure_months, start_date, outstanding_balance, monthly_emi, status)
VALUES
(1, 1, 'Electric Car Financing', 'HDFC Bank Auto Loans', 1200000.00, 8.50, 60, '2024-01-20', 890000.00, 24628.00, 'ACTIVE');

-- 9. Loan Repayments
INSERT OR IGNORE INTO loan_repayments (id, loan_id, account_id, amount, principal_component, interest_component, payment_date, notes)
VALUES
(1, 1, 1, 24628.00, 18320.00, 6308.00, '2026-10-05', 'Automated monthly ECS debit');

-- 10. Financial Goals
INSERT OR IGNORE INTO financial_goals (id, user_id, title, target_amount, current_amount, target_date, category, status)
VALUES
(1, 1, 'Emergency 6-Month Reserve Fund', 300000.00, 180000.00, '2026-12-31', 'Safety Net', 'IN_PROGRESS'),
(2, 1, 'Europe Study & Cultural Tour', 250000.00, 145000.00, '2027-05-15', 'Travel & Education', 'IN_PROGRESS'),
(3, 1, 'New Workstation Setup (M4 Max)', 180000.00, 180000.00, '2026-10-15', 'Equipment', 'COMPLETED');

