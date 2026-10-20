package com.dostatok.dao;

import com.dostatok.database.DatabaseConfig;
import com.dostatok.exception.DatabaseOperationException;
import com.dostatok.model.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC/SQLite implementation of FinanceRepository.
 */
public class FinanceRepositoryImpl implements FinanceRepository {

    // ==================== INCOME ====================
    @Override
    public List<Income> findIncomeByUserId(int userId) throws DatabaseOperationException {
        String sql = "SELECT i.*, b.account_name FROM income i " +
                     "LEFT JOIN bank_accounts b ON i.account_id = b.id " +
                     "WHERE i.user_id = ? ORDER BY i.income_date DESC, i.id DESC";
        List<Income> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Income inc = new Income(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        (Integer) rs.getObject("account_id"),
                        rs.getString("title"),
                        rs.getBigDecimal("amount"),
                        rs.getString("category"),
                        rs.getString("source"),
                        rs.getString("income_date"),
                        rs.getString("notes"),
                        rs.getString("created_at")
                    );
                    inc.setAccountName(rs.getString("account_name"));
                    list.add(inc);
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to fetch income records: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Income> findIncomeById(int id) throws DatabaseOperationException {
        String sql = "SELECT i.*, b.account_name FROM income i " +
                     "LEFT JOIN bank_accounts b ON i.account_id = b.id WHERE i.id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Income inc = new Income(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        (Integer) rs.getObject("account_id"),
                        rs.getString("title"),
                        rs.getBigDecimal("amount"),
                        rs.getString("category"),
                        rs.getString("source"),
                        rs.getString("income_date"),
                        rs.getString("notes"),
                        rs.getString("created_at")
                    );
                    inc.setAccountName(rs.getString("account_name"));
                    return Optional.of(inc);
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to fetch income by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public Income saveIncome(Income income) throws DatabaseOperationException {
        String sql = "INSERT INTO income (user_id, account_id, title, amount, category, source, income_date, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, income.getUserId());
            if (income.getAccountId() != null) ps.setInt(2, income.getAccountId()); else ps.setNull(2, Types.INTEGER);
            ps.setString(3, income.getTitle());
            ps.setBigDecimal(4, income.getAmount());
            ps.setString(5, income.getCategory());
            ps.setString(6, income.getSource());
            ps.setString(7, income.getIncomeDate());
            ps.setString(8, income.getNotes());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) income.setId(rs.getInt(1));
            }
            return income;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to save income: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateIncome(Income income) throws DatabaseOperationException {
        String sql = "UPDATE income SET account_id = ?, title = ?, amount = ?, category = ?, source = ?, " +
                     "income_date = ?, notes = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (income.getAccountId() != null) ps.setInt(1, income.getAccountId()); else ps.setNull(1, Types.INTEGER);
            ps.setString(2, income.getTitle());
            ps.setBigDecimal(3, income.getAmount());
            ps.setString(4, income.getCategory());
            ps.setString(5, income.getSource());
            ps.setString(6, income.getIncomeDate());
            ps.setString(7, income.getNotes());
            ps.setInt(8, income.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update income: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteIncome(int id) throws DatabaseOperationException {
        String sql = "DELETE FROM income WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete income: " + e.getMessage(), e);
        }
    }

    // ==================== EXPENSES ====================
    @Override
    public List<Expense> findExpensesByUserId(int userId) throws DatabaseOperationException {
        String sql = "SELECT e.*, b.account_name FROM expenses e " +
                     "LEFT JOIN bank_accounts b ON e.account_id = b.id " +
                     "WHERE e.user_id = ? ORDER BY e.expense_date DESC, e.id DESC";
        List<Expense> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Expense exp = new Expense(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        (Integer) rs.getObject("account_id"),
                        rs.getString("title"),
                        rs.getBigDecimal("amount"),
                        rs.getString("category"),
                        rs.getString("payment_method"),
                        rs.getString("expense_date"),
                        rs.getString("notes"),
                        rs.getString("created_at")
                    );
                    exp.setAccountName(rs.getString("account_name"));
                    list.add(exp);
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to fetch expenses: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Expense> findExpenseById(int id) throws DatabaseOperationException {
        String sql = "SELECT e.*, b.account_name FROM expenses e " +
                     "LEFT JOIN bank_accounts b ON e.account_id = b.id WHERE e.id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Expense exp = new Expense(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        (Integer) rs.getObject("account_id"),
                        rs.getString("title"),
                        rs.getBigDecimal("amount"),
                        rs.getString("category"),
                        rs.getString("payment_method"),
                        rs.getString("expense_date"),
                        rs.getString("notes"),
                        rs.getString("created_at")
                    );
                    exp.setAccountName(rs.getString("account_name"));
                    return Optional.of(exp);
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to fetch expense by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public Expense saveExpense(Expense expense) throws DatabaseOperationException {
        String sql = "INSERT INTO expenses (user_id, account_id, title, amount, category, payment_method, expense_date, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, expense.getUserId());
            if (expense.getAccountId() != null) ps.setInt(2, expense.getAccountId()); else ps.setNull(2, Types.INTEGER);
            ps.setString(3, expense.getTitle());
            ps.setBigDecimal(4, expense.getAmount());
            ps.setString(5, expense.getCategory());
            ps.setString(6, expense.getPaymentMethod());
            ps.setString(7, expense.getExpenseDate());
            ps.setString(8, expense.getNotes());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) expense.setId(rs.getInt(1));
            }
            return expense;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to save expense: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateExpense(Expense expense) throws DatabaseOperationException {
        String sql = "UPDATE expenses SET account_id = ?, title = ?, amount = ?, category = ?, " +
                     "payment_method = ?, expense_date = ?, notes = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (expense.getAccountId() != null) ps.setInt(1, expense.getAccountId()); else ps.setNull(1, Types.INTEGER);
            ps.setString(2, expense.getTitle());
            ps.setBigDecimal(3, expense.getAmount());
            ps.setString(4, expense.getCategory());
            ps.setString(5, expense.getPaymentMethod());
            ps.setString(6, expense.getExpenseDate());
            ps.setString(7, expense.getNotes());
            ps.setInt(8, expense.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update expense: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteExpense(int id) throws DatabaseOperationException {
        String sql = "DELETE FROM expenses WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete expense: " + e.getMessage(), e);
        }
    }

    // ==================== INVESTMENTS ====================
    @Override
    public List<Investment> findInvestmentsByUserId(int userId) throws DatabaseOperationException {
        String sql = "SELECT * FROM investments WHERE user_id = ? ORDER BY current_value DESC, id DESC";
        List<Investment> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Investment(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("investment_type"),
                        rs.getBigDecimal("invested_amount"),
                        rs.getBigDecimal("current_value"),
                        rs.getString("purchase_date"),
                        rs.getString("notes"),
                        rs.getString("created_at")
                    ));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to fetch investments: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Investment> findInvestmentById(int id) throws DatabaseOperationException {
        String sql = "SELECT * FROM investments WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Investment(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("investment_type"),
                        rs.getBigDecimal("invested_amount"),
                        rs.getBigDecimal("current_value"),
                        rs.getString("purchase_date"),
                        rs.getString("notes"),
                        rs.getString("created_at")
                    ));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to fetch investment by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public Investment saveInvestment(Investment inv) throws DatabaseOperationException {
        String sql = "INSERT INTO investments (user_id, name, investment_type, invested_amount, current_value, purchase_date, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, inv.getUserId());
            ps.setString(2, inv.getName());
            ps.setString(3, inv.getInvestmentType());
            ps.setBigDecimal(4, inv.getInvestedAmount());
            ps.setBigDecimal(5, inv.getCurrentValue());
            ps.setString(6, inv.getPurchaseDate());
            ps.setString(7, inv.getNotes());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) inv.setId(rs.getInt(1));
            }
            return inv;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to save investment: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateInvestment(Investment inv) throws DatabaseOperationException {
        String sql = "UPDATE investments SET name = ?, investment_type = ?, invested_amount = ?, " +
                     "current_value = ?, purchase_date = ?, notes = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, inv.getName());
            ps.setString(2, inv.getInvestmentType());
            ps.setBigDecimal(3, inv.getInvestedAmount());
            ps.setBigDecimal(4, inv.getCurrentValue());
            ps.setString(5, inv.getPurchaseDate());
            ps.setString(6, inv.getNotes());
            ps.setInt(7, inv.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update investment: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteInvestment(int id) throws DatabaseOperationException {
        String sql = "DELETE FROM investments WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete investment: " + e.getMessage(), e);
        }
    }

    // ==================== ASSETS ====================
    @Override
    public List<Asset> findAssetsByUserId(int userId) throws DatabaseOperationException {
        String sql = "SELECT * FROM assets WHERE user_id = ? ORDER BY current_value DESC, id DESC";
        List<Asset> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Asset(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("asset_category"),
                        rs.getBigDecimal("purchase_value"),
                        rs.getBigDecimal("current_value"),
                        rs.getString("acquisition_date"),
                        rs.getString("notes"),
                        rs.getString("created_at")
                    ));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to fetch assets: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Asset> findAssetById(int id) throws DatabaseOperationException {
        String sql = "SELECT * FROM assets WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Asset(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("name"),
                        rs.getString("asset_category"),
                        rs.getBigDecimal("purchase_value"),
                        rs.getBigDecimal("current_value"),
                        rs.getString("acquisition_date"),
                        rs.getString("notes"),
                        rs.getString("created_at")
                    ));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to fetch asset by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public Asset saveAsset(Asset asset) throws DatabaseOperationException {
        String sql = "INSERT INTO assets (user_id, name, asset_category, purchase_value, current_value, acquisition_date, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, asset.getUserId());
            ps.setString(2, asset.getName());
            ps.setString(3, asset.getAssetCategory());
            ps.setBigDecimal(4, asset.getPurchaseValue());
            ps.setBigDecimal(5, asset.getCurrentValue());
            ps.setString(6, asset.getAcquisitionDate());
            ps.setString(7, asset.getNotes());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) asset.setId(rs.getInt(1));
            }
            return asset;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to save asset: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateAsset(Asset asset) throws DatabaseOperationException {
        String sql = "UPDATE assets SET name = ?, asset_category = ?, purchase_value = ?, " +
                     "current_value = ?, acquisition_date = ?, notes = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, asset.getName());
            ps.setString(2, asset.getAssetCategory());
            ps.setBigDecimal(3, asset.getPurchaseValue());
            ps.setBigDecimal(4, asset.getCurrentValue());
            ps.setString(5, asset.getAcquisitionDate());
            ps.setString(6, asset.getNotes());
            ps.setInt(7, asset.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update asset: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteAsset(int id) throws DatabaseOperationException {
        String sql = "DELETE FROM assets WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete asset: " + e.getMessage(), e);
        }
    }

    // ==================== LOANS & REPAYMENTS ====================
    @Override
    public List<Loan> findLoansByUserId(int userId) throws DatabaseOperationException {
        String sql = "SELECT * FROM loans WHERE user_id = ? ORDER BY outstanding_balance DESC, id DESC";
        List<Loan> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Loan(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("loan_name"),
                        rs.getString("lender"),
                        rs.getBigDecimal("principal_amount"),
                        rs.getBigDecimal("interest_rate"),
                        rs.getInt("tenure_months"),
                        rs.getString("start_date"),
                        rs.getBigDecimal("outstanding_balance"),
                        rs.getBigDecimal("monthly_emi"),
                        rs.getString("status"),
                        rs.getString("created_at")
                    ));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to fetch loans: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Loan> findLoanById(int id) throws DatabaseOperationException {
        String sql = "SELECT * FROM loans WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Loan(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("loan_name"),
                        rs.getString("lender"),
                        rs.getBigDecimal("principal_amount"),
                        rs.getBigDecimal("interest_rate"),
                        rs.getInt("tenure_months"),
                        rs.getString("start_date"),
                        rs.getBigDecimal("outstanding_balance"),
                        rs.getBigDecimal("monthly_emi"),
                        rs.getString("status"),
                        rs.getString("created_at")
                    ));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to fetch loan by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public Loan saveLoan(Loan loan) throws DatabaseOperationException {
        String sql = "INSERT INTO loans (user_id, loan_name, lender, principal_amount, interest_rate, " +
                     "tenure_months, start_date, outstanding_balance, monthly_emi, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, loan.getUserId());
            ps.setString(2, loan.getLoanName());
            ps.setString(3, loan.getLender());
            ps.setBigDecimal(4, loan.getPrincipalAmount());
            ps.setBigDecimal(5, loan.getInterestRate());
            ps.setInt(6, loan.getTenureMonths());
            ps.setString(7, loan.getStartDate());
            ps.setBigDecimal(8, loan.getOutstandingBalance());
            ps.setBigDecimal(9, loan.getMonthlyEmi());
            ps.setString(10, loan.getStatus());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) loan.setId(rs.getInt(1));
            }
            return loan;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to save loan: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateLoan(Loan loan) throws DatabaseOperationException {
        String sql = "UPDATE loans SET loan_name = ?, lender = ?, principal_amount = ?, interest_rate = ?, " +
                     "tenure_months = ?, start_date = ?, outstanding_balance = ?, monthly_emi = ?, status = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, loan.getLoanName());
            ps.setString(2, loan.getLender());
            ps.setBigDecimal(3, loan.getPrincipalAmount());
            ps.setBigDecimal(4, loan.getInterestRate());
            ps.setInt(5, loan.getTenureMonths());
            ps.setString(6, loan.getStartDate());
            ps.setBigDecimal(7, loan.getOutstandingBalance());
            ps.setBigDecimal(8, loan.getMonthlyEmi());
            ps.setString(9, loan.getStatus());
            ps.setInt(10, loan.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update loan: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteLoan(int id) throws DatabaseOperationException {
        String sql = "DELETE FROM loans WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete loan: " + e.getMessage(), e);
        }
    }

    @Override
    public void recordLoanRepayment(LoanRepayment repayment, BigDecimal newOutstandingBalance, String newStatus)
            throws DatabaseOperationException {
        String insertSql = "INSERT INTO loan_repayments (loan_id, account_id, amount, principal_component, " +
                           "interest_component, payment_date, notes) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String updateLoanSql = "UPDATE loans SET outstanding_balance = ?, status = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psIns = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement psUpd = conn.prepareStatement(updateLoanSql)) {

                psIns.setInt(1, repayment.getLoanId());
                if (repayment.getAccountId() != null) psIns.setInt(2, repayment.getAccountId());
                else psIns.setNull(2, Types.INTEGER);
                psIns.setBigDecimal(3, repayment.getAmount());
                psIns.setBigDecimal(4, repayment.getPrincipalComponent());
                psIns.setBigDecimal(5, repayment.getInterestComponent());
                psIns.setString(6, repayment.getPaymentDate());
                psIns.setString(7, repayment.getNotes());
                psIns.executeUpdate();

                try (ResultSet rs = psIns.getGeneratedKeys()) {
                    if (rs.next()) repayment.setId(rs.getInt(1));
                }

                psUpd.setBigDecimal(1, newOutstandingBalance);
                psUpd.setString(2, newStatus);
                psUpd.setInt(3, repayment.getLoanId());
                psUpd.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new DatabaseOperationException("Failed to record repayment atomically: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database transaction failed during loan repayment: " + e.getMessage(), e);
        }
    }

    @Override
    public List<LoanRepayment> findRepaymentsByLoanId(int loanId) throws DatabaseOperationException {
        String sql = "SELECT * FROM loan_repayments WHERE loan_id = ? ORDER BY payment_date DESC, id DESC";
        List<LoanRepayment> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, loanId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new LoanRepayment(
                        rs.getInt("id"),
                        rs.getInt("loan_id"),
                        (Integer) rs.getObject("account_id"),
                        rs.getBigDecimal("amount"),
                        rs.getBigDecimal("principal_component"),
                        rs.getBigDecimal("interest_component"),
                        rs.getString("payment_date"),
                        rs.getString("notes"),
                        rs.getString("created_at")
                    ));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to fetch loan repayments: " + e.getMessage(), e);
        }
    }

    // ==================== FINANCIAL GOALS ====================
    @Override
    public List<FinancialGoal> findGoalsByUserId(int userId) throws DatabaseOperationException {
        String sql = "SELECT * FROM financial_goals WHERE user_id = ? ORDER BY status ASC, target_date ASC";
        List<FinancialGoal> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new FinancialGoal(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("title"),
                        rs.getBigDecimal("target_amount"),
                        rs.getBigDecimal("current_amount"),
                        rs.getString("target_date"),
                        rs.getString("category"),
                        rs.getString("status"),
                        rs.getString("created_at")
                    ));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to fetch financial goals: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<FinancialGoal> findGoalById(int id) throws DatabaseOperationException {
        String sql = "SELECT * FROM financial_goals WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new FinancialGoal(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("title"),
                        rs.getBigDecimal("target_amount"),
                        rs.getBigDecimal("current_amount"),
                        rs.getString("target_date"),
                        rs.getString("category"),
                        rs.getString("status"),
                        rs.getString("created_at")
                    ));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to fetch goal by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public FinancialGoal saveGoal(FinancialGoal goal) throws DatabaseOperationException {
        String sql = "INSERT INTO financial_goals (user_id, title, target_amount, current_amount, target_date, category, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, goal.getUserId());
            ps.setString(2, goal.getTitle());
            ps.setBigDecimal(3, goal.getTargetAmount());
            ps.setBigDecimal(4, goal.getCurrentAmount());
            ps.setString(5, goal.getTargetDate());
            ps.setString(6, goal.getCategory());
            ps.setString(7, goal.getStatus());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) goal.setId(rs.getInt(1));
            }
            return goal;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to save goal: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateGoal(FinancialGoal goal) throws DatabaseOperationException {
        String sql = "UPDATE financial_goals SET title = ?, target_amount = ?, current_amount = ?, " +
                     "target_date = ?, category = ?, status = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, goal.getTitle());
            ps.setBigDecimal(2, goal.getTargetAmount());
            ps.setBigDecimal(3, goal.getCurrentAmount());
            ps.setString(4, goal.getTargetDate());
            ps.setString(5, goal.getCategory());
            ps.setString(6, goal.getStatus());
            ps.setInt(7, goal.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update goal: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteGoal(int id) throws DatabaseOperationException {
        String sql = "DELETE FROM financial_goals WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete goal: " + e.getMessage(), e);
        }
    }

    @Override
    public void contributeToGoal(int goalId, BigDecimal newCurrentAmount, String status) throws DatabaseOperationException {
        String sql = "UPDATE financial_goals SET current_amount = ?, status = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, newCurrentAmount);
            ps.setString(2, status);
            ps.setInt(3, goalId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to contribute to goal: " + e.getMessage(), e);
        }
    }
}

