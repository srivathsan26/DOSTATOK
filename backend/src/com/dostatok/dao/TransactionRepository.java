package com.dostatok.dao;

import com.dostatok.exception.DatabaseOperationException;
import com.dostatok.model.FinancialTransaction;
import java.sql.Connection;
import java.util.List;

/**
 * Interface contract for Transaction persistence operations.
 */
public interface TransactionRepository {
    List<FinancialTransaction> findAllByUserId(int userId, int limit) throws DatabaseOperationException;

    List<FinancialTransaction> findByAccountId(int accountId) throws DatabaseOperationException;

    FinancialTransaction save(Connection conn, FinancialTransaction transaction) throws DatabaseOperationException;

    FinancialTransaction save(FinancialTransaction transaction) throws DatabaseOperationException;
}

