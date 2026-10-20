package com.dostatok.model;

import com.dostatok.util.CurrencyUtil;
import java.math.BigDecimal;

/**
 * Domain model representing an expenditure record.
 */
public class Expense {
    private int id;
    private int userId;
    private Integer accountId;
    private String accountName;
    private String title;
    private BigDecimal amount;
    private String category;
    private String paymentMethod;
    private String expenseDate;
    private String notes;
    private String createdAt;

    public Expense() {
        this.amount = BigDecimal.ZERO;
        this.paymentMethod = "BANK_TRANSFER";
    }

    public Expense(int id, int userId, Integer accountId, String title, BigDecimal amount,
                   String category, String paymentMethod, String expenseDate, String notes, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.accountId = accountId;
        this.title = title;
        this.amount = CurrencyUtil.round(amount);
        this.category = category;
        this.paymentMethod = paymentMethod != null ? paymentMethod : "BANK_TRANSFER";
        this.expenseDate = expenseDate;
        this.notes = notes;
        this.createdAt = createdAt;
    }

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

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = CurrencyUtil.round(amount);
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(String expenseDate) {
        this.expenseDate = expenseDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}

