package com.dostatok.model;

import com.dostatok.util.CurrencyUtil;
import java.math.BigDecimal;

/**
 * Domain model representing a financial transaction (ledger entry).
 * Demonstrates Constructor Overloading and Encapsulation.
 */
public class FinancialTransaction {
    private int id;
    private int userId;
    private int accountId;
    private String accountName;
    private String accountNumber;
    private String transactionType; // DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String category;
    private String description;
    private String transactionDate;
    private String createdAt;

    // Default constructor
    public FinancialTransaction() {
        this.amount = BigDecimal.ZERO;
        this.balanceAfter = BigDecimal.ZERO;
    }

    // Overloaded Constructor 1: Essential fields only
    public FinancialTransaction(int userId, int accountId, String transactionType,
                                BigDecimal amount, BigDecimal balanceAfter, String category) {
        this(0, userId, accountId, transactionType, amount, balanceAfter, category, null, null, null);
    }

    // Overloaded Constructor 2: Full fields
    public FinancialTransaction(int id, int userId, int accountId, String transactionType,
                                BigDecimal amount, BigDecimal balanceAfter, String category,
                                String description, String transactionDate, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.accountId = accountId;
        this.transactionType = transactionType;
        this.amount = CurrencyUtil.round(amount);
        this.balanceAfter = CurrencyUtil.round(balanceAfter);
        this.category = category != null ? category : "General";
        this.description = description;
        this.transactionDate = transactionDate;
        this.createdAt = createdAt;
    }

    // Getters and Setters
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

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = CurrencyUtil.round(amount);
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = CurrencyUtil.round(balanceAfter);
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}

