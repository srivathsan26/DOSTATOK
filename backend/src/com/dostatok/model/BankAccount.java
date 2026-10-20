package com.dostatok.model;

import com.dostatok.exception.InsufficientBalanceException;
import com.dostatok.exception.InvalidAmountException;
import com.dostatok.util.CurrencyUtil;
import java.math.BigDecimal;

/**
 * Abstract base class representing a Bank Account.
 * Demonstrates:
 * 1. Abstraction (abstract class and abstract methods)
 * 2. Encapsulation (private fields, protected internal mutation, public getters)
 * 3. Method Overloading (multiple deposit/withdraw signatures)
 */
public abstract class BankAccount implements AccountOperations {
    private int id;
    private int userId;
    private String accountNumber;
    private String accountName;
    private String bankName;
    private BigDecimal balance;
    private BigDecimal minimumBalance;
    private BigDecimal overdraftLimit;
    private BigDecimal interestRate;
    private String status;
    private String createdAt;

    // Default constructor
    public BankAccount() {
        this.balance = BigDecimal.ZERO;
        this.minimumBalance = BigDecimal.ZERO;
        this.overdraftLimit = BigDecimal.ZERO;
        this.interestRate = BigDecimal.ZERO;
        this.status = "ACTIVE";
    }

    // Overloaded parameterized constructor
    public BankAccount(int id, int userId, String accountNumber, String accountName, String bankName,
                       BigDecimal balance, BigDecimal minimumBalance, BigDecimal overdraftLimit,
                       BigDecimal interestRate, String status, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.accountNumber = accountNumber;
        this.accountName = accountName;
        this.bankName = bankName;
        this.balance = CurrencyUtil.round(balance != null ? balance : BigDecimal.ZERO);
        this.minimumBalance = CurrencyUtil.round(minimumBalance != null ? minimumBalance : BigDecimal.ZERO);
        this.overdraftLimit = CurrencyUtil.round(overdraftLimit != null ? overdraftLimit : BigDecimal.ZERO);
        this.interestRate = CurrencyUtil.round(interestRate != null ? interestRate : BigDecimal.ZERO);
        this.status = status != null ? status : "ACTIVE";
        this.createdAt = createdAt;
    }

    // Abstract method: Each subclass must implement its own withdrawal validation policy
    public abstract void validateWithdrawal(BigDecimal amount)
            throws InsufficientBalanceException, InvalidAmountException;

    // Abstract method: Polymorphic account summary string
    public abstract String getAccountSummary();

    // AccountOperations Interface Implementation - Method Overloading (2 signatures)
    public void deposit(BigDecimal amount) throws InvalidAmountException {
        deposit(amount, "Deposit to " + accountName);
    }

    @Override
    public void deposit(BigDecimal amount, String description) throws InvalidAmountException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Deposit amount must be strictly greater than zero.", amount);
        }
        BigDecimal rounded = CurrencyUtil.round(amount);
        this.balance = this.balance.add(rounded);
    }

    public void withdraw(BigDecimal amount) throws InsufficientBalanceException, InvalidAmountException {
        withdraw(amount, "Withdrawal from " + accountName);
    }

    @Override
    public void withdraw(BigDecimal amount, String description)
            throws InsufficientBalanceException, InvalidAmountException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be strictly greater than zero.", amount);
        }
        BigDecimal rounded = CurrencyUtil.round(amount);
        validateWithdrawal(rounded);
        this.balance = this.balance.subtract(rounded);
    }

    // Encapsulated Getters & Controlled Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    @Override
    public BigDecimal getBalance() {
        return balance;
    }

    // Protected setter for database restoration only (cannot be called by outside callers)
    public void setBalanceDirectly(BigDecimal balance) {
        this.balance = CurrencyUtil.round(balance != null ? balance : BigDecimal.ZERO);
    }

    public BigDecimal getMinimumBalance() {
        return minimumBalance;
    }

    public void setMinimumBalance(BigDecimal minimumBalance) {
        this.minimumBalance = CurrencyUtil.round(minimumBalance != null ? minimumBalance : BigDecimal.ZERO);
    }

    public BigDecimal getOverdraftLimit() {
        return overdraftLimit;
    }

    public void setOverdraftLimit(BigDecimal overdraftLimit) {
        this.overdraftLimit = CurrencyUtil.round(overdraftLimit != null ? overdraftLimit : BigDecimal.ZERO);
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = CurrencyUtil.round(interestRate != null ? interestRate : BigDecimal.ZERO);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}

