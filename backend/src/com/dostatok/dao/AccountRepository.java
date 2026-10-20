package com.dostatok.dao;

import com.dostatok.exception.DatabaseOperationException;
import com.dostatok.model.BankAccount;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Interface contract for BankAccount persistence operations.
 * Demonstrates Abstraction and Dependency Separation in Java.
 */
public interface AccountRepository {
    List<BankAccount> findAllByUserId(int userId) throws DatabaseOperationException;

    Optional<BankAccount> findById(int id) throws DatabaseOperationException;

    BankAccount save(BankAccount account) throws DatabaseOperationException;

    void update(BankAccount account) throws DatabaseOperationException;

    void updateBalance(Connection conn, int accountId, BigDecimal newBalance) throws DatabaseOperationException;

    void delete(int id) throws DatabaseOperationException;

    boolean existsByAccountNumber(String accountNumber) throws DatabaseOperationException;
}

