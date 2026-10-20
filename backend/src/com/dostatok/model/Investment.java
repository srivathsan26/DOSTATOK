package com.dostatok.model;

import com.dostatok.util.CurrencyUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Domain model representing an investment holding (Mutual Fund, Equity, Gold, FD, etc.).
 * Demonstrates domain business logic and safe monetary calculation.
 */
public class Investment {
    private int id;
    private int userId;
    private String name;
    private String investmentType;
    private BigDecimal investedAmount;
    private BigDecimal currentValue;
    private String purchaseDate;
    private String notes;
    private String createdAt;

    public Investment() {
        this.investedAmount = BigDecimal.ZERO;
        this.currentValue = BigDecimal.ZERO;
    }

    public Investment(int id, int userId, String name, String investmentType,
                      BigDecimal investedAmount, BigDecimal currentValue,
                      String purchaseDate, String notes, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.investmentType = investmentType;
        this.investedAmount = CurrencyUtil.round(investedAmount);
        this.currentValue = CurrencyUtil.round(currentValue);
        this.purchaseDate = purchaseDate;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    // Business Methods
    public BigDecimal getProfitLoss() {
        return CurrencyUtil.round(currentValue.subtract(investedAmount));
    }

    public BigDecimal getReturnPercentage() {
        if (investedAmount == null || investedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal diff = currentValue.subtract(investedAmount);
        return diff.multiply(BigDecimal.valueOf(100)).divide(investedAmount, 2, RoundingMode.HALF_UP);
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInvestmentType() {
        return investmentType;
    }

    public void setInvestmentType(String investmentType) {
        this.investmentType = investmentType;
    }

    public BigDecimal getInvestedAmount() {
        return investedAmount;
    }

    public void setInvestedAmount(BigDecimal investedAmount) {
        this.investedAmount = CurrencyUtil.round(investedAmount);
    }

    public BigDecimal getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(BigDecimal currentValue) {
        this.currentValue = CurrencyUtil.round(currentValue);
    }

    public String getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(String purchaseDate) {
        this.purchaseDate = purchaseDate;
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

