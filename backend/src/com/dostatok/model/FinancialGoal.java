package com.dostatok.model;

import com.dostatok.exception.InvalidAmountException;
import com.dostatok.util.CurrencyUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Domain model representing a savings / financial milestone goal.
 */
public class FinancialGoal {
    private int id;
    private int userId;
    private String title;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private String targetDate;
    private String category;
    private String status; // IN_PROGRESS, COMPLETED, CANCELLED
    private String createdAt;

    public FinancialGoal() {
        this.targetAmount = BigDecimal.ZERO;
        this.currentAmount = BigDecimal.ZERO;
        this.status = "IN_PROGRESS";
    }

    public FinancialGoal(int id, int userId, String title, BigDecimal targetAmount,
                         BigDecimal currentAmount, String targetDate, String category,
                         String status, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.targetAmount = CurrencyUtil.round(targetAmount);
        this.currentAmount = CurrencyUtil.round(currentAmount != null ? currentAmount : BigDecimal.ZERO);
        this.targetDate = targetDate;
        this.category = category;
        this.status = status != null ? status : "IN_PROGRESS";
        this.createdAt = createdAt;
    }

    // Business Methods
    public BigDecimal getProgressPercentage() {
        if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal pct = currentAmount.multiply(BigDecimal.valueOf(100))
                .divide(targetAmount, 2, RoundingMode.HALF_UP);
        return pct.min(BigDecimal.valueOf(100.00));
    }

    public BigDecimal getRemainingAmount() {
        if (currentAmount.compareTo(targetAmount) >= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return CurrencyUtil.round(targetAmount.subtract(currentAmount));
    }

    public void contribute(BigDecimal amount) throws InvalidAmountException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Contribution amount must be positive", amount);
        }
        this.currentAmount = this.currentAmount.add(CurrencyUtil.round(amount));
        if (this.currentAmount.compareTo(this.targetAmount) >= 0) {
            this.status = "COMPLETED";
        }
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = CurrencyUtil.round(targetAmount);
    }

    public BigDecimal getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(BigDecimal currentAmount) {
        this.currentAmount = CurrencyUtil.round(currentAmount);
    }

    public String getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(String targetDate) {
        this.targetDate = targetDate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

