package com.dostatok.model;

import com.dostatok.util.CurrencyUtil;
import java.math.BigDecimal;

/**
 * Domain model representing a single loan repayment installment.
 */
public class LoanRepayment {
    private int id;
    private int loanId;
    private Integer accountId;
    private BigDecimal amount;
    private BigDecimal principalComponent;
    private BigDecimal interestComponent;
    private String paymentDate;
    private String notes;
    private String createdAt;

    public LoanRepayment() {
        this.amount = BigDecimal.ZERO;
        this.principalComponent = BigDecimal.ZERO;
        this.interestComponent = BigDecimal.ZERO;
    }

    public LoanRepayment(int id, int loanId, Integer accountId, BigDecimal amount,
                         BigDecimal principalComponent, BigDecimal interestComponent,
                         String paymentDate, String notes, String createdAt) {
        this.id = id;
        this.loanId = loanId;
        this.accountId = accountId;
        this.amount = CurrencyUtil.round(amount);
        this.principalComponent = CurrencyUtil.round(principalComponent);
        this.interestComponent = CurrencyUtil.round(interestComponent);
        this.paymentDate = paymentDate;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLoanId() {
        return loanId;
    }

    public void setLoanId(int loanId) {
        this.loanId = loanId;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = CurrencyUtil.round(amount);
    }

    public BigDecimal getPrincipalComponent() {
        return principalComponent;
    }

    public void setPrincipalComponent(BigDecimal principalComponent) {
        this.principalComponent = CurrencyUtil.round(principalComponent);
    }

    public BigDecimal getInterestComponent() {
        return interestComponent;
    }

    public void setInterestComponent(BigDecimal interestComponent) {
        this.interestComponent = CurrencyUtil.round(interestComponent);
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
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

