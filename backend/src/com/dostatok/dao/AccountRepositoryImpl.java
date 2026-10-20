package com.dostatok.dao;

import com.dostatok.database.DatabaseConfig;
import com.dostatok.exception.DatabaseOperationException;
import com.dostatok.model.BankAccount;
import com.dostatok.model.CurrentAccount;
import com.dostatok.model.SavingsAccount;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC/SQLite implementation of AccountRepository.
 * Maps SQLite records dynamically into polymorphic BankAccount instances.
 */
public class AccountRepositoryImpl implements AccountRepository {

    @Override
    public List<BankAccount> findAllByUserId(int userId) throws DatabaseOperationException {
        String sql = "SELECT * FROM bank_accounts WHERE user_id = ? ORDER BY id ASC";
        List<BankAccount> accounts = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    accounts.add(mapRowToAccount(rs));
                }
            }
            return accounts;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to load accounts for user: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<BankAccount> findById(int id) throws DatabaseOperationException {
        String sql = "SELECT * FROM bank_accounts WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToAccount(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to find account by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public BankAccount save(BankAccount account) throws DatabaseOperationException {
        String sql = "INSERT INTO bank_accounts (user_id, account_number, account_name, account_type, bank_name, " +
                     "balance, minimum_balance, overdraft_limit, interest_rate, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, account.getUserId());
            ps.setString(2, account.getAccountNumber());
            ps.setString(3, account.getAccountName());
            ps.setString(4, account.getAccountType());
            ps.setString(5, account.getBankName());
            ps.setBigDecimal(6, account.getBalance());
            ps.setBigDecimal(7, account.getMinimumBalance());
            ps.setBigDecimal(8, account.getOverdraftLimit());
            ps.setBigDecimal(9, account.getInterestRate());
            ps.setString(10, account.getStatus());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    account.setId(rs.getInt(1));
                }
            }
            return account;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to create bank account: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(BankAccount account) throws DatabaseOperationException {
        String sql = "UPDATE bank_accounts SET account_name = ?, bank_name = ?, minimum_balance = ?, " +
                     "overdraft_limit = ?, interest_rate = ?, status = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, account.getAccountName());
            ps.setString(2, account.getBankName());
            ps.setBigDecimal(3, account.getMinimumBalance());
            ps.setBigDecimal(4, account.getOverdraftLimit());
            ps.setBigDecimal(5, account.getInterestRate());
            ps.setString(6, account.getStatus());
            ps.setInt(7, account.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update account: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateBalance(Connection conn, int accountId, BigDecimal newBalance) throws DatabaseOperationException {
        String sql = "UPDATE bank_accounts SET balance = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, newBalance);
            ps.setInt(2, accountId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to update balance atomically: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) throws DatabaseOperationException {
        String sql = "DELETE FROM bank_accounts WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to delete account: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsByAccountNumber(String accountNumber) throws DatabaseOperationException {
        String sql = "SELECT count(*) FROM bank_accounts WHERE account_number = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to verify account number: " + e.getMessage(), e);
        }
    }

    /**
     * Polymorphic Factory / Mapper Method:
     * Instantiates the exact BankAccount subclass based on the database column account_type.
     */
    private BankAccount mapRowToAccount(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int userId = rs.getInt("user_id");
        String accNum = rs.getString("account_number");
        String accName = rs.getString("account_name");
        String accType = rs.getString("account_type");
        String bankName = rs.getString("bank_name");
        BigDecimal balance = rs.getBigDecimal("balance");
        BigDecimal minBal = rs.getBigDecimal("minimum_balance");
        BigDecimal odLimit = rs.getBigDecimal("overdraft_limit");
        BigDecimal intRate = rs.getBigDecimal("interest_rate");
        String status = rs.getString("status");
        String createdAt = rs.getString("created_at");

        if ("SAVINGS".equalsIgnoreCase(accType)) {
            return new SavingsAccount(id, userId, accNum, accName, bankName, balance, minBal, intRate, status, createdAt);
        } else {
            return new CurrentAccount(id, userId, accNum, accName, bankName, balance, odLimit, status, createdAt);
        }
    }
}

