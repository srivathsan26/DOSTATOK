package com.dostatok.model;

import com.dostatok.util.CurrencyUtil;
import java.math.BigDecimal;

/**
 * Domain model representing an income record.
 */
public class Income {
    private int id;
    private int userId;
    private Integer accountId;
    private String accountName;
    private String title;
    private BigDecimal amount;
    private String category;
    private String source;
    private String incomeDate;
    private String notes;
    private String createdAt;

    public Income() {
        this.amount = BigDecimal.ZERO;
    }

    public Income(int id, int userId, Integer accountId, String title, BigDecimal amount,
                  String category, String source, String incomeDate, String notes, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.accountId = accountId;
        this.title = title;
        this.amount = CurrencyUtil.round(amount);
        this.category = category;
        this.source = source;
        this.incomeDate = incomeDate;
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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getIncomeDate() {
        return incomeDate;
    }

    public void setIncomeDate(String incomeDate) {
        this.incomeDate = incomeDate;
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

