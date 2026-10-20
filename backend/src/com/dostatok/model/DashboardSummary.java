package com.dostatok.model;

import com.dostatok.util.CurrencyUtil;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregated domain model representing the comprehensive financial summary.
 * Follows the strict accounting formula:
 * Net Worth = Total Assets (Liquid Cash + Investments + Physical Assets) - Total Liabilities (Loans)
 */
public class DashboardSummary {
    private BigDecimal totalLiquidCash;
    private BigDecimal totalInvestmentsValue;
    private BigDecimal totalInvestedPrincipal;
    private BigDecimal totalInvestmentProfitLoss;
    private BigDecimal totalAssetsValue;
    private BigDecimal totalGrossAssets;
    private BigDecimal totalLiabilities;
    private BigDecimal netWorth;
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netSavings;
    private int accountCount;
    private int goalCount;
    private List<FinancialTransaction> recentTransactions;
    private List<CategoryTotal> expenseCategories;
    private List<CategoryTotal> incomeCategories;

    public DashboardSummary() {
        this.totalLiquidCash = BigDecimal.ZERO;
        this.totalInvestmentsValue = BigDecimal.ZERO;
        this.totalInvestedPrincipal = BigDecimal.ZERO;
        this.totalInvestmentProfitLoss = BigDecimal.ZERO;
        this.totalAssetsValue = BigDecimal.ZERO;
        this.totalGrossAssets = BigDecimal.ZERO;
        this.totalLiabilities = BigDecimal.ZERO;
        this.netWorth = BigDecimal.ZERO;
        this.totalIncome = BigDecimal.ZERO;
        this.totalExpenses = BigDecimal.ZERO;
        this.netSavings = BigDecimal.ZERO;
        this.recentTransactions = new ArrayList<>();
        this.expenseCategories = new ArrayList<>();
        this.incomeCategories = new ArrayList<>();
    }

    public static class CategoryTotal {
        private String category;
        private BigDecimal total;

        public CategoryTotal(String category, BigDecimal total) {
            this.category = category;
            this.total = CurrencyUtil.round(total);
        }

        public String getCategory() {
            return category;
        }

        public BigDecimal getTotal() {
            return total;
        }
    }

    // Getters and Setters
    public BigDecimal getTotalLiquidCash() {
        return totalLiquidCash;
    }

    public void setTotalLiquidCash(BigDecimal totalLiquidCash) {
        this.totalLiquidCash = CurrencyUtil.round(totalLiquidCash);
    }

    public BigDecimal getTotalInvestmentsValue() {
        return totalInvestmentsValue;
    }

    public void setTotalInvestmentsValue(BigDecimal totalInvestmentsValue) {
        this.totalInvestmentsValue = CurrencyUtil.round(totalInvestmentsValue);
    }

    public BigDecimal getTotalInvestedPrincipal() {
        return totalInvestedPrincipal;
    }

    public void setTotalInvestedPrincipal(BigDecimal totalInvestedPrincipal) {
        this.totalInvestedPrincipal = CurrencyUtil.round(totalInvestedPrincipal);
    }

    public BigDecimal getTotalInvestmentProfitLoss() {
        return totalInvestmentProfitLoss;
    }

    public void setTotalInvestmentProfitLoss(BigDecimal totalInvestmentProfitLoss) {
        this.totalInvestmentProfitLoss = CurrencyUtil.round(totalInvestmentProfitLoss);
    }

    public BigDecimal getTotalAssetsValue() {
        return totalAssetsValue;
    }

    public void setTotalAssetsValue(BigDecimal totalAssetsValue) {
        this.totalAssetsValue = CurrencyUtil.round(totalAssetsValue);
    }

    public BigDecimal getTotalGrossAssets() {
        return totalGrossAssets;
    }

    public void setTotalGrossAssets(BigDecimal totalGrossAssets) {
        this.totalGrossAssets = CurrencyUtil.round(totalGrossAssets);
    }

    public BigDecimal getTotalLiabilities() {
        return totalLiabilities;
    }

    public void setTotalLiabilities(BigDecimal totalLiabilities) {
        this.totalLiabilities = CurrencyUtil.round(totalLiabilities);
    }

    public BigDecimal getNetWorth() {
        return netWorth;
    }

    public void setNetWorth(BigDecimal netWorth) {
        this.netWorth = CurrencyUtil.round(netWorth);
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = CurrencyUtil.round(totalIncome);
    }

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(BigDecimal totalExpenses) {
        this.totalExpenses = CurrencyUtil.round(totalExpenses);
    }

    public BigDecimal getNetSavings() {
        return netSavings;
    }

    public void setNetSavings(BigDecimal netSavings) {
        this.netSavings = CurrencyUtil.round(netSavings);
    }

    public int getAccountCount() {
        return accountCount;
    }

    public void setAccountCount(int accountCount) {
        this.accountCount = accountCount;
    }

    public int getGoalCount() {
        return goalCount;
    }

    public void setGoalCount(int goalCount) {
        this.goalCount = goalCount;
    }

    public List<FinancialTransaction> getRecentTransactions() {
        return recentTransactions;
    }

    public void setRecentTransactions(List<FinancialTransaction> recentTransactions) {
        this.recentTransactions = recentTransactions;
    }

    public List<CategoryTotal> getExpenseCategories() {
        return expenseCategories;
    }

    public void setExpenseCategories(List<CategoryTotal> expenseCategories) {
        this.expenseCategories = expenseCategories;
    }

    public List<CategoryTotal> getIncomeCategories() {
        return incomeCategories;
    }

    public void setIncomeCategories(List<CategoryTotal> incomeCategories) {
        this.incomeCategories = incomeCategories;
    }
}

