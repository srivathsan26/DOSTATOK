package com.dostatok.main;

import com.dostatok.dao.*;
import com.dostatok.database.DatabaseConfig;
import com.dostatok.exception.InsufficientBalanceException;
import com.dostatok.exception.InvalidAmountException;
import com.dostatok.model.*;
import com.dostatok.service.AccountService;
import com.dostatok.service.FinanceService;
import com.dostatok.util.CurrencyUtil;
import java.math.BigDecimal;
import java.util.List;

/**
 * Automated Test Suite verifying all backend layers without external testing frameworks.
 */
public final class BackendTestSuite {
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("   DOSTATOK — AUTOMATED BACKEND INTEGRATION TEST SUITE                          ");
        System.out.println("================================================================================");

        try {
            // Setup
            DatabaseConfig.initializeDatabase();
            AccountRepository accountRepo = new AccountRepositoryImpl();
            TransactionRepository txRepo = new TransactionRepositoryImpl();
            FinanceRepository financeRepo = new FinanceRepositoryImpl();
            AccountService accountService = new AccountService(accountRepo, txRepo);
            FinanceService financeService = new FinanceService(financeRepo, accountRepo, txRepo, accountService);

            int testUserId = 1;

            // Test 1: Accounts loaded
            test("Accounts fetch", () -> {
                List<BankAccount> accounts = accountService.getUserAccounts(testUserId);
                assertTrue(!accounts.isEmpty(), "Should have seeded accounts");
            });

            // Test 2: Deposit transaction
            test("Account Deposit and Atomicity", () -> {
                List<BankAccount> accounts = accountService.getUserAccounts(testUserId);
                BankAccount account = accounts.get(0);
                BigDecimal initialBalance = account.getBalance();
                BigDecimal depositAmt = new BigDecimal("5000.00");

                FinancialTransaction tx = accountService.deposit(account.getId(), depositAmt, "Test Deposit", "Automated deposit test");
                assertTrue(tx.getAmount().compareTo(depositAmt) == 0, "Transaction amount should match");

                BankAccount updated = accountService.getAccountById(account.getId());
                assertTrue(updated.getBalance().compareTo(initialBalance.add(depositAmt)) == 0, "Balance must be incremented atomically");
            });

            // Test 3: Valid withdrawal
            test("Valid Account Withdrawal", () -> {
                List<BankAccount> accounts = accountService.getUserAccounts(testUserId);
                BankAccount account = accounts.get(0);
                BigDecimal initialBalance = account.getBalance();
                BigDecimal withdrawAmt = new BigDecimal("1000.00");

                FinancialTransaction tx = accountService.withdraw(account.getId(), withdrawAmt, "Test Withdrawal", "Automated withdrawal test");
                assertTrue(tx.getAmount().compareTo(withdrawAmt) == 0, "Transaction amount should match");

                BankAccount updated = accountService.getAccountById(account.getId());
                assertTrue(updated.getBalance().compareTo(initialBalance.subtract(withdrawAmt)) == 0, "Balance must be decremented atomically");
            });

            // Test 4: Insufficient funds withdrawal rejection
            test("Rejection of Insufficient Funds Withdrawal", () -> {
                List<BankAccount> accounts = accountService.getUserAccounts(testUserId);
                BankAccount account = accounts.get(0);
                BigDecimal excessiveAmount = account.getBalance().add(new BigDecimal("9999999.00"));

                boolean caught = false;
                try {
                    accountService.withdraw(account.getId(), excessiveAmount, "Invalid", "Should fail");
                } catch (InsufficientBalanceException e) {
                    caught = true;
                }
                assertTrue(caught, "InsufficientBalanceException must be thrown when withdrawal exceeds funds");
            });

            // Test 5: Rejection of negative / zero amounts
            test("Rejection of Invalid Financial Amounts", () -> {
                List<BankAccount> accounts = accountService.getUserAccounts(testUserId);
                BankAccount account = accounts.get(0);

                boolean caught = false;
                try {
                    accountService.deposit(account.getId(), new BigDecimal("-500.00"), "Invalid", "Should fail");
                } catch (InvalidAmountException e) {
                    caught = true;
                }
                assertTrue(caught, "InvalidAmountException must be thrown for negative amounts");
            });

            // Test 6: Income & Expenses Recording
            test("Income & Expenses Ledger", () -> {
                Income inc = new Income(0, testUserId, null, "Test Bonus", new BigDecimal("15000.00"), "Bonus", "Company", "2026-10-12", "Test note", null);
                Income savedInc = financeService.addIncome(inc, false);
                assertTrue(savedInc.getId() > 0, "Income must be persisted");

                Expense exp = new Expense(0, testUserId, null, "Test Utility", new BigDecimal("3500.00"), "Utilities", "UPI", "2026-10-12", "Electric bill", null);
                Expense savedExp = financeService.addExpense(exp, false);
                assertTrue(savedExp.getId() > 0, "Expense must be persisted");
            });

            // Test 7: Investment calculations
            test("Investment Calculations (Profit/Loss & ROI)", () -> {
                Investment inv = new Investment(0, testUserId, "Nifty Next 50 ETF", "Mutual Fund",
                        new BigDecimal("100000.00"), new BigDecimal("125000.00"), "2025-01-01", "Index tracker", null);
                assertTrue(inv.getProfitLoss().compareTo(new BigDecimal("25000.00")) == 0, "Profit should be 25,000");
                assertTrue(inv.getReturnPercentage().compareTo(new BigDecimal("25.00")) == 0, "ROI should be 25.00%");
            });

            // Test 8: Loan EMI and Repayment
            test("Loan EMI & Repayment Calculation", () -> {
                Loan loan = new Loan(0, testUserId, "Test Personal Loan", "Axis Bank",
                        new BigDecimal("100000.00"), new BigDecimal("12.00"), 12, "2026-10-01",
                        new BigDecimal("100000.00"), null, "ACTIVE", null);
                assertTrue(loan.getMonthlyEmi().compareTo(BigDecimal.ZERO) > 0, "Monthly EMI should be calculated");
                BigDecimal initialBalance = loan.getOutstandingBalance();
                loan.recordRepayment(new BigDecimal("10000.00"));
                assertTrue(loan.getOutstandingBalance().compareTo(initialBalance.subtract(new BigDecimal("10000.00"))) == 0, "Loan balance reduced");
            });

            // Test 9: Financial Goals Progress
            test("Financial Goal Progress Percentage", () -> {
                FinancialGoal goal = new FinancialGoal(0, testUserId, "Vacation Fund", new BigDecimal("50000.00"),
                        new BigDecimal("25000.00"), "2026-12-31", "Travel", "IN_PROGRESS", null);
                assertTrue(goal.getProgressPercentage().compareTo(new BigDecimal("50.00")) == 0, "Goal progress should be 50.00%");
                assertTrue(goal.getRemainingAmount().compareTo(new BigDecimal("25000.00")) == 0, "Remaining amount should be 25000.00");
            });

            // Test 10: Consolidated Dashboard Summary Formulas
            test("Dashboard Summary Net Worth Formula", () -> {
                DashboardSummary summary = financeService.getDashboardSummary(testUserId);
                assertTrue(summary.getTotalLiquidCash().compareTo(BigDecimal.ZERO) > 0, "Liquid cash should be positive");
                assertTrue(summary.getTotalGrossAssets().compareTo(BigDecimal.ZERO) > 0, "Gross assets should be positive");

                BigDecimal expectedNetWorth = summary.getTotalGrossAssets().subtract(summary.getTotalLiabilities());
                assertTrue(summary.getNetWorth().compareTo(expectedNetWorth) == 0,
                        "Net worth formula (Gross Assets - Liabilities) must hold strictly");
            });

        } catch (Exception e) {
            System.err.println("Unexpected failure in test setup: " + e.getMessage());
            e.printStackTrace();
            testsFailed++;
        }

        System.out.println("================================================================================");
        System.out.printf("   TEST RESULTS: %d PASSED, %d FAILED%n", testsPassed, testsFailed);
        System.out.println("================================================================================");
        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private static void test(String name, TestRunnable runnable) {
        try {
            runnable.run();
            System.out.println("  [PASS] " + name);
            testsPassed++;
        } catch (Throwable t) {
            System.err.println("  [FAIL] " + name + ": " + t.getMessage());
            t.printStackTrace();
            testsFailed++;
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Assertion Failed: " + message);
        }
    }

    @FunctionalInterface
    interface TestRunnable {
        void run() throws Throwable;
    }
}

