package com.dostatok.model;

import com.dostatok.exception.InsufficientBalanceException;
import com.dostatok.exception.InvalidAmountException;
import com.dostatok.util.CurrencyUtil;
import java.math.BigDecimal;

/**
 * Concrete subclass representing a Current Account.
 * Demonstrates:
 * 1. Inheritance (extends BankAccount)
 * 2. Method Overriding (validateWithdrawal, getAccountType, getAccountSummary)
 * 3. Polymorphism (permits overdraft facilities for business cash flows)
 */
public class CurrentAccount extends BankAccount {

    public CurrentAccount() {
        super();
        setMinimumBalance(BigDecimal.ZERO); // Current accounts usually have zero min balance requirement
        setOverdraftLimit(new BigDecimal("50000.00")); // Default overdraft facility
        setInterestRate(BigDecimal.ZERO); // Current accounts yield 0% interest
    }

    public CurrentAccount(int id, int userId, String accountNumber, String accountName, String bankName,
                          BigDecimal balance, BigDecimal overdraftLimit, String status, String createdAt) {
        super(id, userId, accountNumber, accountName, bankName, balance,
              BigDecimal.ZERO, // Minimum balance is 0
              overdraftLimit != null ? overdraftLimit : new BigDecimal("50000.00"),
              BigDecimal.ZERO, // Interest rate is 0
              status, createdAt);
    }

    @Override
    public String getAccountType() {
        return "CURRENT";
    }

    /**
     * Polymorphic method implementation:
     * A Current Account permits withdrawals beyond current balance up to the configured overdraftLimit.
     */
    @Override
    public void validateWithdrawal(BigDecimal amount) throws InsufficientBalanceException, InvalidAmountException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be strictly greater than zero.", amount);
        }

        BigDecimal availableFunds = getBalance().add(getOverdraftLimit());
        if (amount.compareTo(availableFunds) > 0) {
            String msg = String.format(
                "Withdrawal of %s rejected. Exceeds total available liquidity %s (Balance: %s + Overdraft Limit: %s).",
                CurrencyUtil.formatInr(amount),
                CurrencyUtil.formatInr(availableFunds),
                CurrencyUtil.formatInr(getBalance()),
                CurrencyUtil.formatInr(getOverdraftLimit())
            );
            throw new InsufficientBalanceException(msg, getBalance(), amount);
        }
    }

    /**
     * Returns total effective liquidity including overdraft headroom.
     */
    public BigDecimal getEffectiveAvailableLiquidity() {
        return CurrencyUtil.round(getBalance().add(getOverdraftLimit()));
    }

    @Override
    public String getAccountSummary() {
        return String.format(
            "Current Account [%s - %s]: Balance %s | Overdraft Limit: %s | Total Purchasing Power: %s",
            getAccountNumber(), getAccountName(),
            CurrencyUtil.formatInr(getBalance()),
            CurrencyUtil.formatInr(getOverdraftLimit()),
            CurrencyUtil.formatInr(getEffectiveAvailableLiquidity())
        );
    }
}

