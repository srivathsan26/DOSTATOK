package com.dostatok.dao;

import com.dostatok.database.DatabaseConfig;
import com.dostatok.exception.DatabaseOperationException;
import com.dostatok.model.FinancialTransaction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC/SQLite implementation of TransactionRepository.
 */
public class TransactionRepositoryImpl implements TransactionRepository {

    @Override
    public List<FinancialTransaction> findAllByUserId(int userId, int limit) throws DatabaseOperationException {
        String sql = "SELECT t.*, b.account_name, b.account_number FROM transactions t " +
                     "LEFT JOIN bank_accounts b ON t.account_id = b.id " +
                     "WHERE t.user_id = ? ORDER BY t.transaction_date DESC, t.id DESC " +
                     (limit > 0 ? "LIMIT " + limit : "");
        List<FinancialTransaction> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToTransaction(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to retrieve transactions: " + e.getMessage(), e);
        }
    }

    @Override
    public List<FinancialTransaction> findByAccountId(int accountId) throws DatabaseOperationException {
        String sql = "SELECT t.*, b.account_name, b.account_number FROM transactions t " +
                     "LEFT JOIN bank_accounts b ON t.account_id = b.id " +
                     "WHERE t.account_id = ? ORDER BY t.transaction_date DESC, t.id DESC";
        List<FinancialTransaction> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToTransaction(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to retrieve account transactions: " + e.getMessage(), e);
        }
    }

    @Override
    public FinancialTransaction save(Connection conn, FinancialTransaction tx) throws DatabaseOperationException {
        String sql = "INSERT INTO transactions (user_id, account_id, transaction_type, amount, balance_after, category, description) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, tx.getUserId());
            ps.setInt(2, tx.getAccountId());
            ps.setString(3, tx.getTransactionType());
            ps.setBigDecimal(4, tx.getAmount());
            ps.setBigDecimal(5, tx.getBalanceAfter());
            ps.setString(6, tx.getCategory());
            ps.setString(7, tx.getDescription());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    tx.setId(rs.getInt(1));
                }
            }
            return tx;
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to insert transaction record: " + e.getMessage(), e);
        }
    }

    @Override
    public FinancialTransaction save(FinancialTransaction transaction) throws DatabaseOperationException {
        try (Connection conn = DatabaseConfig.getConnection()) {
            return save(conn, transaction);
        } catch (SQLException e) {
            throw new DatabaseOperationException("Failed to acquire connection for transaction save: " + e.getMessage(), e);
        }
    }

    private FinancialTransaction mapRowToTransaction(ResultSet rs) throws SQLException {
        FinancialTransaction tx = new FinancialTransaction(
            rs.getInt("id"),
            rs.getInt("user_id"),
            rs.getInt("account_id"),
            rs.getString("transaction_type"),
            rs.getBigDecimal("amount"),
            rs.getBigDecimal("balance_after"),
            rs.getString("category"),
            rs.getString("description"),
            rs.getString("transaction_date"),
            rs.getString("created_at")
        );
        try {
            tx.setAccountName(rs.getString("account_name"));
            tx.setAccountNumber(rs.getString("account_number"));
        } catch (SQLException ignored) {
            // Optional join columns
        }
        return tx;
    }
}

