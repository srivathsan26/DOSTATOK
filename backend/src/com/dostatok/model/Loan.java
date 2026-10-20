package com.dostatok.model;

import com.dostatok.exception.InvalidAmountException;
import com.dostatok.util.CurrencyUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Domain model representing a liability/loan.
 * Demonstrates financial calculations and encapsulation.
 */
public class Loan {
    private int id;
    private int userId;
    private String loanName;
    private String lender;
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private int tenureMonths;
    private String startDate;
    private BigDecimal outstandingBalance;
    private BigDecimal monthlyEmi;
    private String status; // ACTIVE, CLOSED
    private String createdAt;

    public Loan() {
        this.principalAmount = BigDecimal.ZERO;
        this.interestRate = BigDecimal.ZERO;
        this.outstandingBalance = BigDecimal.ZERO;
        this.monthlyEmi = BigDecimal.ZERO;
        this.status = "ACTIVE";
    }

    public Loan(int id, int userId, String loanName, String lender,
                BigDecimal principalAmount, BigDecimal interestRate, int tenureMonths,
                String startDate, BigDecimal outstandingBalance, BigDecimal monthlyEmi,
                String status, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.loanName = loanName;
        this.lender = lender;
        this.principalAmount = CurrencyUtil.round(principalAmount);
        this.interestRate = CurrencyUtil.round(interestRate);
        this.tenureMonths = tenureMonths;
        this.startDate = startDate;
        this.outstandingBalance = CurrencyUtil.round(outstandingBalance);
        this.monthlyEmi = CurrencyUtil.round(monthlyEmi != null ? monthlyEmi : calculateEmi(principalAmount, interestRate, tenureMonths));
        this.status = status != null ? status : "ACTIVE";
        this.createdAt = createdAt;
    }

    /**
     * Standard Equated Monthly Installment (EMI) calculation formula:
     * EMI = [P x r x (1+r)^n] / [(1+r)^n - 1]
     */
    public static BigDecimal calculateEmi(BigDecimal principal, BigDecimal annualInterestRate, int months) {
        if (principal == null || principal.compareTo(BigDecimal.ZERO) <= 0 || months <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        if (annualInterestRate == null || annualInterestRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
        }

        double p = principal.doubleValue();
        double r = (annualInterestRate.doubleValue() / 12.0) / 100.0;
        int n = months;

        double emi = (p * r * Math.pow(1 + r, n)) / (Math.pow(1 + r, n) - 1);
        return CurrencyUtil.round(BigDecimal.valueOf(emi));
    }

    /**
     * Applies a principal reduction repayment.
     */
    public void recordRepayment(BigDecimal amount) throws InvalidAmountException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Repayment amount must be positive", amount);
        }
        BigDecimal rounded = CurrencyUtil.round(amount);
        if (rounded.compareTo(outstandingBalance) >= 0) {
            this.outstandingBalance = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            this.status = "CLOSED";
        } else {
            this.outstandingBalance = this.outstandingBalance.subtract(rounded);
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

    public String getLoanName() {
        return loanName;
    }

    public void setLoanName(String loanName) {
        this.loanName = loanName;
    }

    public String getLender() {
        return lender;
    }

    public void setLender(String lender) {
        this.lender = lender;
    }

    public BigDecimal getPrincipalAmount() {
        return principalAmount;
    }

    public void setPrincipalAmount(BigDecimal principalAmount) {
        this.principalAmount = CurrencyUtil.round(principalAmount);
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = CurrencyUtil.round(interestRate);
    }

    public int getTenureMonths() {
        return tenureMonths;
    }

    public void setTenureMonths(int tenureMonths) {
        this.tenureMonths = tenureMonths;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public BigDecimal getOutstandingBalance() {
        return outstandingBalance;
    }

    public void setOutstandingBalance(BigDecimal outstandingBalance) {
        this.outstandingBalance = CurrencyUtil.round(outstandingBalance);
    }

    public BigDecimal getMonthlyEmi() {
        return monthlyEmi;
    }

    public void setMonthlyEmi(BigDecimal monthlyEmi) {
        this.monthlyEmi = CurrencyUtil.round(monthlyEmi);
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

