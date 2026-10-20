package com.dostatok.model;

import com.dostatok.exception.InsufficientBalanceException;
import com.dostatok.exception.InvalidAmountException;
import java.math.BigDecimal;

/**
 * Interface defining operations contract for bank account entities.
 * Demonstrates Abstraction in Java OOP.
 */
public interface AccountOperations {
    /**
     * Deposits money into the account.
     *
     * @param amount positive deposit amount
     * @param description contextual transaction note
     * @throws InvalidAmountException if amount <= 0
     */
    void deposit(BigDecimal amount, String description) throws InvalidAmountException;

    /**
     * Withdraws money from the account subject to account policy.
     *
     * @param amount positive withdrawal amount
     * @param description contextual transaction note
     * @throws InsufficientBalanceException if funds/policy are violated
     * @throws InvalidAmountException if amount <= 0
     */
    void withdraw(BigDecimal amount, String description) throws InsufficientBalanceException, InvalidAmountException;

    /**
     * Returns current balance.
     */
    BigDecimal getBalance();

    /**
     * Returns account unique number.
     */
    String getAccountNumber();

    /**
     * Returns account classification (e.g. SAVINGS or CURRENT).
     */
    String getAccountType();
}

