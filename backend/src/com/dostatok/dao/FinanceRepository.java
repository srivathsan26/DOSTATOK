package com.dostatok.dao;

import com.dostatok.exception.DatabaseOperationException;
import com.dostatok.model.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Interface contract for Income, Expense, Investment, Asset, Loan, and Goal persistence.
 */
public interface FinanceRepository {
    // Income
    List<Income> findIncomeByUserId(int userId) throws DatabaseOperationException;
    Optional<Income> findIncomeById(int id) throws DatabaseOperationException;
    Income saveIncome(Income income) throws DatabaseOperationException;
    void updateIncome(Income income) throws DatabaseOperationException;
    void deleteIncome(int id) throws DatabaseOperationException;

    // Expenses
    List<Expense> findExpensesByUserId(int userId) throws DatabaseOperationException;
    Optional<Expense> findExpenseById(int id) throws DatabaseOperationException;
    Expense saveExpense(Expense expense) throws DatabaseOperationException;
    void updateExpense(Expense expense) throws DatabaseOperationException;
    void deleteExpense(int id) throws DatabaseOperationException;

    // Investments
    List<Investment> findInvestmentsByUserId(int userId) throws DatabaseOperationException;
    Optional<Investment> findInvestmentById(int id) throws DatabaseOperationException;
    Investment saveInvestment(Investment investment) throws DatabaseOperationException;
    void updateInvestment(Investment investment) throws DatabaseOperationException;
    void deleteInvestment(int id) throws DatabaseOperationException;

    // Assets
    List<Asset> findAssetsByUserId(int userId) throws DatabaseOperationException;
    Optional<Asset> findAssetById(int id) throws DatabaseOperationException;
    Asset saveAsset(Asset asset) throws DatabaseOperationException;
    void updateAsset(Asset asset) throws DatabaseOperationException;
    void deleteAsset(int id) throws DatabaseOperationException;

    // Loans & Repayments
    List<Loan> findLoansByUserId(int userId) throws DatabaseOperationException;
    Optional<Loan> findLoanById(int id) throws DatabaseOperationException;
    Loan saveLoan(Loan loan) throws DatabaseOperationException;
    void updateLoan(Loan loan) throws DatabaseOperationException;
    void deleteLoan(int id) throws DatabaseOperationException;
    void recordLoanRepayment(LoanRepayment repayment, BigDecimal newOutstandingBalance, String newStatus) throws DatabaseOperationException;
    List<LoanRepayment> findRepaymentsByLoanId(int loanId) throws DatabaseOperationException;

    // Goals
    List<FinancialGoal> findGoalsByUserId(int userId) throws DatabaseOperationException;
    Optional<FinancialGoal> findGoalById(int id) throws DatabaseOperationException;
    FinancialGoal saveGoal(FinancialGoal goal) throws DatabaseOperationException;
    void updateGoal(FinancialGoal goal) throws DatabaseOperationException;
    void deleteGoal(int id) throws DatabaseOperationException;
    void contributeToGoal(int goalId, BigDecimal newCurrentAmount, String status) throws DatabaseOperationException;
}

