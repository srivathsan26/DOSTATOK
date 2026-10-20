package com.dostatok.model;

import com.dostatok.exception.InsufficientBalanceException;
import com.dostatok.exception.InvalidAmountException;
import com.dostatok.util.CurrencyUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Concrete subclass representing a Savings Account.
 * Demonstrates:
 * 1. Inheritance (extends BankAccount)
 * 2. Method Overriding (validateWithdrawal, getAccountType, getAccountSummary)
 * 3. Polymorphism (account-specific financial logic)
 */
public class SavingsAccount extends BankAccount {

    public SavingsAccount() {
        super();
        setMinimumBalance(new BigDecimal("1000.00")); // Default minimum balance policy
        setInterestRate(new BigDecimal("3.50"));     // 3.5% p.a.
    }

    public SavingsAccount(int id, int userId, String accountNumber, String accountName, String bankName,
                          BigDecimal balance, BigDecimal minimumBalance, BigDecimal interestRate,
                          String status, String createdAt) {
        super(id, userId, accountNumber, accountName, bankName, balance,
              minimumBalance != null ? minimumBalance : new BigDecimal("1000.00"),
              BigDecimal.ZERO, // Overdraft limit is always 0 for savings
              interestRate != null ? interestRate : new BigDecimal("3.50"),
              status, createdAt);
    }

    @Override
    public String getAccountType() {
        return "SAVINGS";
    }

    /**
     * Polymorphic method implementation:
     * A Savings Account requires balance after withdrawal to remain at or above minimumBalance.
     */
    @Override
    public void validateWithdrawal(BigDecimal amount) throws InsufficientBalanceException, InvalidAmountException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be strictly greater than zero.", amount);
        }

        BigDecimal balanceAfter = getBalance().subtract(amount);
        if (balanceAfter.compareTo(getMinimumBalance()) < 0) {
            String msg = String.format(
                "Withdrawal of %s rejected. Savings account must maintain minimum balance of %s (Current: %s).",
                CurrencyUtil.formatInr(amount),
                CurrencyUtil.formatInr(getMinimumBalance()),
                CurrencyUtil.formatInr(getBalance())
            );
            throw new InsufficientBalanceException(msg, getBalance(), amount);
        }
    }

    /**
     * Calculates projected annual interest based on current balance and interest rate.
     */
    public BigDecimal calculateEstimatedAnnualInterest() {
        if (getBalance().compareTo(BigDecimal.ZERO) <= 0 || getInterestRate().compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return CurrencyUtil.round(
            getBalance().multiply(getInterestRate()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
        );
    }

    @Override
    public String getAccountSummary() {
        return String.format(
            "Savings Account [%s - %s]: Balance %s | Min Balance: %s | Interest Rate: %s%% p.a. (Est. Annual Interest: %s)",
            getAccountNumber(), getAccountName(),
            CurrencyUtil.formatInr(getBalance()),
            CurrencyUtil.formatInr(getMinimumBalance()),
            getInterestRate().toPlainString(),
            CurrencyUtil.formatInr(calculateEstimatedAnnualInterest())
        );
    }
}

