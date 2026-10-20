package com.dostatok.main;

import com.dostatok.exception.InsufficientBalanceException;
import com.dostatok.exception.InvalidAmountException;
import com.dostatok.model.*;
import com.dostatok.util.CurrencyUtil;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Dedicated College Demonstration Program for DOSTATOK.
 * Explicitly demonstrates the 9 mandatory Java OOP Concepts for Faculty Evaluation:
 * 1. Classes and Objects
 * 2. Encapsulation
 * 3. Inheritance
 * 4. Abstract Classes
 * 5. Interfaces
 * 6. Runtime Polymorphism
 * 7. Method Overloading
 * 8. Method Overriding
 * 9. Custom Exception Handling
 */
public final class OopDemo {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("   DOSTATOK — JAVA OBJECT-ORIENTED PROGRAMMING (OOP) LIVE DEMONSTRATION         ");
        System.out.println("================================================================================");

        demoClassesAndObjects();
        demoEncapsulation();
        demoInheritanceAndAbstractClasses();
        demoInterfaces();
        demoRuntimePolymorphism();
        demoMethodOverloading();
        demoMethodOverriding();
        demoCustomExceptionHandling();

        System.out.println("\n================================================================================");
        System.out.println("   ALL 9 OOP CONCEPTS DEMONSTRATED AND VALIDATED SUCCESSFULLY!                  ");
        System.out.println("================================================================================");
    }

    /**
     * Concept 1: Classes and Objects
     */
    private static void demoClassesAndObjects() {
        System.out.println("\n[1] CONCEPT: CLASSES AND OBJECTS");
        System.out.println("Description: Demonstrates instantiation, state maintenance, and instance methods.");

        User user = new User(101, "viva_student", "Rohan Mehta", "rohan@dostatok.in", "INR", "2026-10-12");
        Investment stock = new Investment(1, 101, "Tata Consultancy Services", "Stocks",
                new BigDecimal("50000.00"), new BigDecimal("62500.00"), "2026-01-15", "Bluechip holding", null);

        System.out.println(" - Instantiated User Object: " + user.getFullName() + " (" + user.getEmail() + ")");
        System.out.println(" - Instantiated Investment Object: " + stock.getName() + " | Invested: " +
                CurrencyUtil.formatInr(stock.getInvestedAmount()) + " | Valuation: " +
                CurrencyUtil.formatInr(stock.getCurrentValue()));
        System.out.println(" - Calculated Profit/Loss: " + CurrencyUtil.formatInr(stock.getProfitLoss()) +
                " (ROI: +" + stock.getReturnPercentage() + "%)");
    }

    /**
     * Concept 2: Encapsulation
     */
    private static void demoEncapsulation() {
        System.out.println("\n[2] CONCEPT: ENCAPSULATION");
        System.out.println("Description: Private fields prevent direct tampering; operations validate mutations.");

        SavingsAccount account = new SavingsAccount(1, 101, "HDFC009988", "Savings Reserve", "HDFC Bank",
                new BigDecimal("25000.00"), new BigDecimal("1000.00"), new BigDecimal("3.50"), "ACTIVE", null);

        System.out.println(" - Current Encapsulated Balance: " + CurrencyUtil.formatInr(account.getBalance()));
        try {
            System.out.println(" - Attempting controlled mutation: deposit ₹5,000 via account.deposit()");
            account.deposit(new BigDecimal("5000.00"));
            System.out.println(" - Updated Encapsulated Balance: " + CurrencyUtil.formatInr(account.getBalance()));

            System.out.println(" - Attempting invalid mutation with negative deposit (-₹2,000)...");
            account.deposit(new BigDecimal("-2000.00"));
        } catch (InvalidAmountException e) {
            System.out.println("   [SUCCESS - Encapsulation Guard]: Invalid state prevented: " + e.getMessage());
        }
    }

    /**
     * Concept 3 & 4: Inheritance and Abstract Classes
     */
    private static void demoInheritanceAndAbstractClasses() {
        System.out.println("\n[3 & 4] CONCEPTS: INHERITANCE AND ABSTRACT CLASSES");
        System.out.println("Description: Abstract BankAccount class extended by SavingsAccount and CurrentAccount.");

        BankAccount savings = new SavingsAccount(10, 101, "SB-1001", "Student Savings", "SBI",
                new BigDecimal("15000.00"), new BigDecimal("1000.00"), new BigDecimal("3.50"), "ACTIVE", null);

        BankAccount current = new CurrentAccount(20, 101, "CA-2002", "Agency Current", "Axis Bank",
                new BigDecimal("30000.00"), new BigDecimal("50000.00"), "ACTIVE", null);

        System.out.println(" - SavingsAccount extends BankAccount -> Class: " + savings.getClass().getSimpleName() +
                " is instance of BankAccount: " + (savings instanceof BankAccount));
        System.out.println(" - CurrentAccount extends BankAccount -> Class: " + current.getClass().getSimpleName() +
                " is instance of BankAccount: " + (current instanceof BankAccount));
    }

    /**
     * Concept 5: Interfaces
     */
    private static void demoInterfaces() {
        System.out.println("\n[5] CONCEPT: INTERFACES (ABSTRACTION & CONTRACT)");
        System.out.println("Description: AccountOperations defines public behavioral contract independent of implementation.");

        AccountOperations ops = new SavingsAccount(30, 101, "SB-9000", "Contract Demo", "Kotak",
                new BigDecimal("10000.00"), new BigDecimal("500.00"), new BigDecimal("4.00"), "ACTIVE", null);

        try {
            ops.deposit(new BigDecimal("2500.00"), "Contract deposit");
            System.out.println(" - AccountOperations.deposit() invoked on interface reference.");
            System.out.println(" - AccountOperations.getBalance(): " + CurrencyUtil.formatInr(ops.getBalance()));
            System.out.println(" - AccountOperations.getAccountType(): " + ops.getAccountType());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    /**
     * Concept 6: Runtime Polymorphism
     */
    private static void demoRuntimePolymorphism() {
        System.out.println("\n[6] CONCEPT: RUNTIME POLYMORPHISM (DYNAMIC METHOD DISPATCH)");
        System.out.println("Description: Iterating over BankAccount base references executing subclass policies dynamically.");

        List<BankAccount> portfolio = new ArrayList<>();
        // Savings Account: Balance = ₹5,000, Min Balance = ₹2,000 (Available to withdraw = ₹3,000)
        portfolio.add(new SavingsAccount(1, 101, "SAV-01", "Personal Savings", "HDFC",
                new BigDecimal("5000.00"), new BigDecimal("2000.00"), new BigDecimal("3.50"), "ACTIVE", null));

        // Current Account: Balance = ₹5,000, Overdraft = ₹10,000 (Available to withdraw = ₹15,000)
        portfolio.add(new CurrentAccount(2, 101, "CUR-02", "Consulting Current", "ICICI",
                new BigDecimal("5000.00"), new BigDecimal("10000.00"), "ACTIVE", null));

        BigDecimal withdrawalAttempt = new BigDecimal("7000.00");
        System.out.println(" - Both accounts have initial balance of ₹5,000.00.");
        System.out.println(" - Attempting withdrawal of " + CurrencyUtil.formatInr(withdrawalAttempt) + " on each polymorphic reference:\n");

        for (BankAccount acc : portfolio) {
            System.out.println("   Calling polymorphic method on: " + acc.getClass().getSimpleName() + " (" + acc.getAccountName() + ")");
            try {
                acc.withdraw(withdrawalAttempt, "Testing Polymorphic Withdrawal");
                System.out.println("   -> [SUCCESS]: Withdrawal approved! New balance: " + CurrencyUtil.formatInr(acc.getBalance()));
            } catch (InsufficientBalanceException e) {
                System.out.println("   -> [REJECTED via Savings Rule]: " + e.getMessage());
            } catch (InvalidAmountException e) {
                System.out.println("   -> [REJECTED via Invalid Amount]: " + e.getMessage());
            }
        }
    }

    /**
     * Concept 7: Method Overloading
     */
    private static void demoMethodOverloading() {
        System.out.println("\n[7] CONCEPT: METHOD OVERLOADING (COMPILE-TIME POLYMORPHISM)");
        System.out.println("Description: Methods with same name but different parameter signatures.");

        SavingsAccount acc = new SavingsAccount();
        acc.setAccountName("Overload Test");

        try {
            // Signature 1: deposit(BigDecimal amount)
            acc.deposit(new BigDecimal("1000.00"));
            System.out.println(" - Invoked overloaded signature 1: acc.deposit(BigDecimal) -> Balance: " +
                    CurrencyUtil.formatInr(acc.getBalance()));

            // Signature 2: deposit(BigDecimal amount, String description)
            acc.deposit(new BigDecimal("2500.00"), "Salary bonus payment");
            System.out.println(" - Invoked overloaded signature 2: acc.deposit(BigDecimal, String) -> Balance: " +
                    CurrencyUtil.formatInr(acc.getBalance()));
        } catch (InvalidAmountException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    /**
     * Concept 8: Method Overriding
     */
    private static void demoMethodOverriding() {
        System.out.println("\n[8] CONCEPT: METHOD OVERRIDING");
        System.out.println("Description: Subclasses override getAccountSummary() and validateWithdrawal() to provide customized behavior.");

        SavingsAccount savings = new SavingsAccount(1, 101, "SAV-99", "Super Savings", "HDFC",
                new BigDecimal("100000.00"), new BigDecimal("5000.00"), new BigDecimal("4.00"), "ACTIVE", null);
        CurrentAccount current = new CurrentAccount(2, 101, "CUR-88", "Apex Current", "SBI",
                new BigDecimal("200000.00"), new BigDecimal("75000.00"), "ACTIVE", null);

        System.out.println(" - Overridden in SavingsAccount:");
        System.out.println("   " + savings.getAccountSummary());
        System.out.println(" - Overridden in CurrentAccount:");
        System.out.println("   " + current.getAccountSummary());
    }

    /**
     * Concept 9: Custom Exception Handling
     */
    private static void demoCustomExceptionHandling() {
        System.out.println("\n[9] CONCEPT: CUSTOM EXCEPTION HANDLING");
        System.out.println("Description: Domain-specific exceptions prevent invalid state with checked/controlled error propagation.");

        SavingsAccount acc = new SavingsAccount(1, 101, "SAV-ERR", "Safety Test", "Canara Bank",
                new BigDecimal("5000.00"), new BigDecimal("2000.00"), new BigDecimal("3.00"), "ACTIVE", null);

        // Test 1: InvalidAmountException
        try {
            System.out.println(" - Test 1: Withdrawing zero amount (₹0.00)...");
            acc.withdraw(BigDecimal.ZERO);
        } catch (InvalidAmountException e) {
            System.out.println("   [CAUGHT InvalidAmountException]: " + e.getMessage());
        } catch (InsufficientBalanceException e) {
            System.out.println("   Unexpected exception: " + e.getMessage());
        }

        // Test 2: InsufficientBalanceException (Savings min-balance breach)
        try {
            System.out.println(" - Test 2: Withdrawing ₹4,500 from ₹5,000 (Min Balance is ₹2,000)...");
            acc.withdraw(new BigDecimal("4500.00"));
        } catch (InsufficientBalanceException e) {
            System.out.println("   [CAUGHT InsufficientBalanceException]: " + e.getMessage());
            System.out.println("   Attempted: " + CurrencyUtil.formatInr(e.getAttemptedAmount()) +
                    " | Available Balance: " + CurrencyUtil.formatInr(e.getCurrentBalance()));
        } catch (InvalidAmountException e) {
            System.out.println("   Unexpected exception: " + e.getMessage());
        }
    }
}

