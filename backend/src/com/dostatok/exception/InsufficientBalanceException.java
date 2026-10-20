package com.dostatok.exception;

import java.math.BigDecimal;

/**
 * Thrown when a withdrawal or debit attempt violates account balance,
 * minimum balance policy, or overdraft limits.
 */
public class InsufficientBalanceException extends DostatokException {
    private final BigDecimal currentBalance;
    private final BigDecimal attemptedAmount;

    public InsufficientBalanceException(String message) {
        super(message, 400);
        this.currentBalance = BigDecimal.ZERO;
        this.attemptedAmount = BigDecimal.ZERO;
    }

    public InsufficientBalanceException(String message, BigDecimal currentBalance, BigDecimal attemptedAmount) {
        super(message, 400);
        this.currentBalance = currentBalance;
        this.attemptedAmount = attemptedAmount;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public BigDecimal getAttemptedAmount() {
        return attemptedAmount;
    }
}

