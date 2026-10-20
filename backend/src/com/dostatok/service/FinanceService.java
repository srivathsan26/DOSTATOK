package com.dostatok.service;

import com.dostatok.dao.AccountRepository;
import com.dostatok.dao.FinanceRepository;
import com.dostatok.dao.TransactionRepository;
import com.dostatok.exception.DatabaseOperationException;
import com.dostatok.exception.InsufficientBalanceException;
import com.dostatok.exception.InvalidAmountException;
import com.dostatok.exception.ResourceNotFoundException;
import com.dostatok.exception.ValidationException;
import com.dostatok.model.*;
import com.dostatok.util.CurrencyUtil;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service managing Income, Expenses, Investments, Assets, Loans, Goals,
 * and Consolidated Dashboard Financial Analytics.
 */
public class FinanceService {
    private final FinanceRepository financeRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    public FinanceService(FinanceRepository financeRepository, AccountRepository accountRepository,
                          TransactionRepository transactionRepository, AccountService accountService) {
        this.financeRepository = financeRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
    }

    // ==================== DASHBOARD SUMMARY ====================
    public DashboardSummary getDashboardSummary(int userId) throws DatabaseOperationException {
        DashboardSummary summary = new DashboardSummary();

        // 1. Bank Accounts (Liquid Assets)
        List<BankAccount> accounts = accountRepository.findAllByUserId(userId);
        summary.setAccountCount(accounts.size());
        BigDecimal totalLiquid = accounts.stream()
                .map(BankAccount::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.setTotalLiquidCash(totalLiquid);

        // 2. Investments
        List<Investment> investments = financeRepository.findInvestmentsByUserId(userId);
        BigDecimal totalInvPrincipal = investments.stream()
                .map(Investment::getInvestedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalInvCurrent = investments.stream()
                .map(Investment::getCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.setTotalInvestedPrincipal(totalInvPrincipal);
        summary.setTotalInvestmentsValue(totalInvCurrent);
        summary.setTotalInvestmentProfitLoss(totalInvCurrent.subtract(totalInvPrincipal));

        // 3. Physical Capital Assets
        List<Asset> assets = financeRepository.findAssetsByUserId(userId);
        BigDecimal totalAssetsVal = assets.stream()
                .map(Asset::getCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.setTotalAssetsValue(totalAssetsVal);

        // 4. Gross Total Assets = Liquid Cash + Investments + Physical Assets
        BigDecimal grossAssets = totalLiquid.add(totalInvCurrent).add(totalAssetsVal);
        summary.setTotalGrossAssets(grossAssets);

        // 5. Total Liabilities (Outstanding Loans)
        List<Loan> loans = financeRepository.findLoansByUserId(userId);
        BigDecimal totalLiabilities = loans.stream()
                .filter(l -> "ACTIVE".equalsIgnoreCase(l.getStatus()))
                .map(Loan::getOutstandingBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.setTotalLiabilities(totalLiabilities);

        // 6. Net Worth = Total Assets - Total Liabilities
        BigDecimal netWorth = grossAssets.subtract(totalLiabilities);
        summary.setNetWorth(netWorth);

        // 7. Income & Expenses
        List<Income> incomes = financeRepository.findIncomeByUserId(userId);
        BigDecimal totalIncome = incomes.stream()
                .map(Income::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.setTotalIncome(totalIncome);

        List<Expense> expenses = financeRepository.findExpensesByUserId(userId);
        BigDecimal totalExpenses = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        summary.setTotalExpenses(totalExpenses);

        // 8. Net Cashflow Savings = Total Income - Total Expenses
        summary.setNetSavings(totalIncome.subtract(totalExpenses));

        // 9. Goals count
        List<FinancialGoal> goals = financeRepository.findGoalsByUserId(userId);
        summary.setGoalCount(goals.size());

        // 10. Recent Transactions (latest 8)
        summary.setRecentTransactions(transactionRepository.findAllByUserId(userId, 8));

        // 11. Category breakdowns for charts
        Map<String, BigDecimal> expCatMap = expenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));
        List<DashboardSummary.CategoryTotal> expenseCatList = expCatMap.entrySet().stream()
                .map(e -> new DashboardSummary.CategoryTotal(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(DashboardSummary.CategoryTotal::getTotal).reversed())
                .collect(Collectors.toList());
        summary.setExpenseCategories(expenseCatList);

        Map<String, BigDecimal> incCatMap = incomes.stream()
                .collect(Collectors.groupingBy(
                        Income::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Income::getAmount, BigDecimal::add)
                ));
        List<DashboardSummary.CategoryTotal> incomeCatList = incCatMap.entrySet().stream()
                .map(e -> new DashboardSummary.CategoryTotal(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(DashboardSummary.CategoryTotal::getTotal).reversed())
                .collect(Collectors.toList());
        summary.setIncomeCategories(incomeCatList);

        return summary;
    }

    // ==================== INCOME ====================
    public List<Income> getIncomeList(int userId) throws DatabaseOperationException {
        return financeRepository.findIncomeByUserId(userId);
    }

    public Income addIncome(Income income, boolean updateAccountBalance)
            throws ValidationException, InvalidAmountException, ResourceNotFoundException, DatabaseOperationException {
        if (income.getTitle() == null || income.getTitle().trim().isEmpty()) {
            throw new ValidationException("Income title is required.");
        }
        if (income.getAmount() == null || income.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Income amount must be positive.", income.getAmount());
        }
        if (income.getCategory() == null || income.getCategory().trim().isEmpty()) {
            income.setCategory("Salary");
        }
        if (income.getSource() == null || income.getSource().trim().isEmpty()) {
            income.setSource("General");
        }
        if (income.getIncomeDate() == null || income.getIncomeDate().trim().isEmpty()) {
            income.setIncomeDate(java.time.LocalDate.now().toString());
        }

        Income saved = financeRepository.saveIncome(income);

        if (updateAccountBalance && income.getAccountId() != null) {
            accountService.deposit(income.getAccountId(), income.getAmount(), income.getCategory(), "Income: " + income.getTitle());
        }

        return saved;
    }

    public void updateIncome(Income income) throws DatabaseOperationException, ValidationException, InvalidAmountException {
        if (income.getTitle() == null || income.getTitle().trim().isEmpty()) {
            throw new ValidationException("Income title is required.");
        }
        if (income.getAmount() == null || income.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Income amount must be positive.", income.getAmount());
        }
        financeRepository.updateIncome(income);
    }

    public void deleteIncome(int id) throws DatabaseOperationException {
        financeRepository.deleteIncome(id);
    }

    // ==================== EXPENSES ====================
    public List<Expense> getExpenseList(int userId) throws DatabaseOperationException {
        return financeRepository.findExpensesByUserId(userId);
    }

    public Expense addExpense(Expense expense, boolean deductFromAccount)
            throws ValidationException, InvalidAmountException, ResourceNotFoundException,
                   InsufficientBalanceException, DatabaseOperationException {
        if (expense.getTitle() == null || expense.getTitle().trim().isEmpty()) {
            throw new ValidationException("Expense title is required.");
        }
        if (expense.getAmount() == null || expense.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Expense amount must be positive.", expense.getAmount());
        }
        if (expense.getCategory() == null || expense.getCategory().trim().isEmpty()) {
            expense.setCategory("General");
        }
        if (expense.getExpenseDate() == null || expense.getExpenseDate().trim().isEmpty()) {
            expense.setExpenseDate(java.time.LocalDate.now().toString());
        }

        if (deductFromAccount && expense.getAccountId() != null) {
            // Deduct via polymorphic withdrawal rule first
            accountService.withdraw(expense.getAccountId(), expense.getAmount(), expense.getCategory(), "Expense: " + expense.getTitle());
        }

        return financeRepository.saveExpense(expense);
    }

    public void updateExpense(Expense expense) throws DatabaseOperationException, ValidationException, InvalidAmountException {
        if (expense.getTitle() == null || expense.getTitle().trim().isEmpty()) {
            throw new ValidationException("Expense title is required.");
        }
        if (expense.getAmount() == null || expense.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Expense amount must be positive.", expense.getAmount());
        }
        financeRepository.updateExpense(expense);
    }

    public void deleteExpense(int id) throws DatabaseOperationException {
        financeRepository.deleteExpense(id);
    }

    // ==================== INVESTMENTS ====================
    public List<Investment> getInvestments(int userId) throws DatabaseOperationException {
        return financeRepository.findInvestmentsByUserId(userId);
    }

    public Investment addInvestment(Investment inv) throws ValidationException, InvalidAmountException, DatabaseOperationException {
        if (inv.getName() == null || inv.getName().trim().isEmpty()) {
            throw new ValidationException("Investment name is required.");
        }
        if (inv.getInvestmentType() == null || inv.getInvestmentType().trim().isEmpty()) {
            inv.setInvestmentType("Mutual Fund");
        }
        if (inv.getInvestedAmount() == null || inv.getInvestedAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidAmountException("Invested principal cannot be negative.", inv.getInvestedAmount());
        }
        if (inv.getCurrentValue() == null || inv.getCurrentValue().compareTo(BigDecimal.ZERO) < 0) {
            inv.setCurrentValue(inv.getInvestedAmount());
        }
        if (inv.getPurchaseDate() == null || inv.getPurchaseDate().trim().isEmpty()) {
            inv.setPurchaseDate(java.time.LocalDate.now().toString());
        }
        return financeRepository.saveInvestment(inv);
    }

    public void updateInvestment(Investment inv) throws ValidationException, InvalidAmountException, DatabaseOperationException {
        if (inv.getName() == null || inv.getName().trim().isEmpty()) {
            throw new ValidationException("Investment name is required.");
        }
        if (inv.getInvestedAmount() == null || inv.getInvestedAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidAmountException("Invested principal cannot be negative.", inv.getInvestedAmount());
        }
        financeRepository.updateInvestment(inv);
    }

    public void deleteInvestment(int id) throws DatabaseOperationException {
        financeRepository.deleteInvestment(id);
    }

    // ==================== ASSETS ====================
    public List<Asset> getAssets(int userId) throws DatabaseOperationException {
        return financeRepository.findAssetsByUserId(userId);
    }

    public Asset addAsset(Asset asset) throws ValidationException, InvalidAmountException, DatabaseOperationException {
        if (asset.getName() == null || asset.getName().trim().isEmpty()) {
            throw new ValidationException("Asset name is required.");
        }
        if (asset.getAssetCategory() == null || asset.getAssetCategory().trim().isEmpty()) {
            asset.setAssetCategory("MOVABLE");
        }
        if (asset.getPurchaseValue() == null || asset.getPurchaseValue().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidAmountException("Asset purchase value cannot be negative.", asset.getPurchaseValue());
        }
        if (asset.getCurrentValue() == null || asset.getCurrentValue().compareTo(BigDecimal.ZERO) < 0) {
            asset.setCurrentValue(asset.getPurchaseValue());
        }
        if (asset.getAcquisitionDate() == null || asset.getAcquisitionDate().trim().isEmpty()) {
            asset.setAcquisitionDate(java.time.LocalDate.now().toString());
        }
        return financeRepository.saveAsset(asset);
    }

    public void updateAsset(Asset asset) throws ValidationException, InvalidAmountException, DatabaseOperationException {
        if (asset.getName() == null || asset.getName().trim().isEmpty()) {
            throw new ValidationException("Asset name is required.");
        }
        financeRepository.updateAsset(asset);
    }

    public void deleteAsset(int id) throws DatabaseOperationException {
        financeRepository.deleteAsset(id);
    }

    // ==================== LOANS & REPAYMENTS ====================
    public List<Loan> getLoans(int userId) throws DatabaseOperationException {
        return financeRepository.findLoansByUserId(userId);
    }

    public Loan addLoan(Loan loan) throws ValidationException, InvalidAmountException, DatabaseOperationException {
        if (loan.getLoanName() == null || loan.getLoanName().trim().isEmpty()) {
            throw new ValidationException("Loan title is required.");
        }
        if (loan.getLender() == null || loan.getLender().trim().isEmpty()) {
            throw new ValidationException("Lender institution is required.");
        }
        if (loan.getPrincipalAmount() == null || loan.getPrincipalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Loan principal must be strictly positive.", loan.getPrincipalAmount());
        }
        if (loan.getTenureMonths() <= 0) {
            throw new ValidationException("Loan tenure must be at least 1 month.");
        }
        if (loan.getOutstandingBalance() == null || loan.getOutstandingBalance().compareTo(BigDecimal.ZERO) <= 0) {
            loan.setOutstandingBalance(loan.getPrincipalAmount());
        }
        if (loan.getMonthlyEmi() == null || loan.getMonthlyEmi().compareTo(BigDecimal.ZERO) <= 0) {
            loan.setMonthlyEmi(Loan.calculateEmi(loan.getPrincipalAmount(), loan.getInterestRate(), loan.getTenureMonths()));
        }
        if (loan.getStartDate() == null || loan.getStartDate().trim().isEmpty()) {
            loan.setStartDate(java.time.LocalDate.now().toString());
        }
        return financeRepository.saveLoan(loan);
    }

    public void updateLoan(Loan loan) throws ValidationException, DatabaseOperationException {
        if (loan.getLoanName() == null || loan.getLoanName().trim().isEmpty()) {
            throw new ValidationException("Loan title is required.");
        }
        financeRepository.updateLoan(loan);
    }

    public void deleteLoan(int id) throws DatabaseOperationException {
        financeRepository.deleteLoan(id);
    }

    public void recordRepayment(int loanId, Integer accountId, BigDecimal amount, String notes)
            throws ResourceNotFoundException, InvalidAmountException, InsufficientBalanceException, DatabaseOperationException {

        Loan loan = financeRepository.findLoanById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan with ID " + loanId + " not found."));

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Repayment amount must be positive.", amount);
        }

        // If paying from an account, deduct funds first
        if (accountId != null) {
            accountService.withdraw(accountId, amount, "Loan Repayment", "EMI payment for: " + loan.getLoanName());
        }

        // Calculate breakdown approximation
        loan.recordRepayment(amount);

        BigDecimal interestComp = CurrencyUtil.round(amount.multiply(new BigDecimal("0.25"))); // 25% approx interest
        BigDecimal principalComp = CurrencyUtil.round(amount.subtract(interestComp));

        LoanRepayment repayment = new LoanRepayment(
            0, loanId, accountId, amount, principalComp, interestComp,
            java.time.LocalDate.now().toString(), notes, null
        );

        financeRepository.recordLoanRepayment(repayment, loan.getOutstandingBalance(), loan.getStatus());
    }

    public List<LoanRepayment> getLoanRepayments(int loanId) throws DatabaseOperationException {
        return financeRepository.findRepaymentsByLoanId(loanId);
    }

    // ==================== FINANCIAL GOALS ====================
    public List<FinancialGoal> getGoals(int userId) throws DatabaseOperationException {
        return financeRepository.findGoalsByUserId(userId);
    }

    public FinancialGoal addGoal(FinancialGoal goal) throws ValidationException, InvalidAmountException, DatabaseOperationException {
        if (goal.getTitle() == null || goal.getTitle().trim().isEmpty()) {
            throw new ValidationException("Goal title is required.");
        }
        if (goal.getTargetAmount() == null || goal.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Target amount must be strictly positive.", goal.getTargetAmount());
        }
        if (goal.getCategory() == null || goal.getCategory().trim().isEmpty()) {
            goal.setCategory("Savings");
        }
        if (goal.getTargetDate() == null || goal.getTargetDate().trim().isEmpty()) {
            goal.setTargetDate(java.time.LocalDate.now().plusMonths(6).toString());
        }
        return financeRepository.saveGoal(goal);
    }

    public void updateGoal(FinancialGoal goal) throws ValidationException, InvalidAmountException, DatabaseOperationException {
        if (goal.getTitle() == null || goal.getTitle().trim().isEmpty()) {
            throw new ValidationException("Goal title is required.");
        }
        if (goal.getTargetAmount() == null || goal.getTargetAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Target amount must be strictly positive.", goal.getTargetAmount());
        }
        financeRepository.updateGoal(goal);
    }

    public void deleteGoal(int id) throws DatabaseOperationException {
        financeRepository.deleteGoal(id);
    }

    public void contributeToGoal(int goalId, Integer accountId, BigDecimal amount)
            throws ResourceNotFoundException, InvalidAmountException, InsufficientBalanceException, DatabaseOperationException {

        FinancialGoal goal = financeRepository.findGoalById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal with ID " + goalId + " not found."));

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Contribution amount must be positive.", amount);
        }

        if (accountId != null) {
            accountService.withdraw(accountId, amount, "Savings Goal", "Contribution to goal: " + goal.getTitle());
        }

        goal.contribute(amount);
        financeRepository.contributeToGoal(goalId, goal.getCurrentAmount(), goal.getStatus());
    }
}

