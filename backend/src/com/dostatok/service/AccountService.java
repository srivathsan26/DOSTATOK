package com.dostatok.service;

import com.dostatok.dao.AccountRepository;
import com.dostatok.dao.TransactionRepository;
import com.dostatok.database.DatabaseConfig;
import com.dostatok.exception.DatabaseOperationException;
import com.dostatok.exception.InsufficientBalanceException;
import com.dostatok.exception.InvalidAmountException;
import com.dostatok.exception.ResourceNotFoundException;
import com.dostatok.exception.ValidationException;
import com.dostatok.model.BankAccount;
import com.dostatok.model.CurrentAccount;
import com.dostatok.model.FinancialTransaction;
import com.dostatok.model.SavingsAccount;
import com.dostatok.util.CurrencyUtil;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Service orchestrating Bank Account business workflows and atomic transactions.
 * Demonstrates:
 * 1. Abstraction (depends on AccountRepository & TransactionRepository interfaces)
 * 2. Polymorphism (handles SavingsAccount and CurrentAccount via base BankAccount references)
 * 3. Exception Handling (custom domain exceptions and transaction rollback)
 */
public class AccountService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<BankAccount> getUserAccounts(int userId) throws DatabaseOperationException {
        return accountRepository.findAllByUserId(userId);
    }

    public BankAccount getAccountById(int accountId) throws ResourceNotFoundException, DatabaseOperationException {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account with ID " + accountId + " not found."));
    }

    public BankAccount createAccount(int userId, String accountNumber, String accountName,
                                     String accountType, String bankName, BigDecimal initialBalance,
                                     BigDecimal minimumBalance, BigDecimal overdraftLimit, BigDecimal interestRate)
            throws ValidationException, DatabaseOperationException, InvalidAmountException {

        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new ValidationException("Account number is required.");
        }
        if (accountName == null || accountName.trim().isEmpty()) {
            throw new ValidationException("Account name is required.");
        }
        if (bankName == null || bankName.trim().isEmpty()) {
            throw new ValidationException("Bank name is required.");
        }
        if (accountRepository.existsByAccountNumber(accountNumber.trim())) {
            throw new ValidationException("An account with number '" + accountNumber.trim() + "' already exists.");
        }

        BigDecimal balance = initialBalance != null ? CurrencyUtil.round(initialBalance) : BigDecimal.ZERO;
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidAmountException("Initial balance cannot be negative.", balance);
        }

        BankAccount account;
        if ("CURRENT".equalsIgnoreCase(accountType)) {
            account = new CurrentAccount(0, userId, accountNumber.trim(), accountName.trim(), bankName.trim(),
                    balance, overdraftLimit != null ? overdraftLimit : new BigDecimal("50000.00"), "ACTIVE", null);
        } else {
            account = new SavingsAccount(0, userId, accountNumber.trim(), accountName.trim(), bankName.trim(),
                    balance, minimumBalance != null ? minimumBalance : new BigDecimal("1000.00"),
                    interestRate != null ? interestRate : new BigDecimal("3.50"), "ACTIVE", null);
        }

        BankAccount saved = accountRepository.save(account);

        // Record initial balance deposit transaction if > 0
        if (balance.compareTo(BigDecimal.ZERO) > 0) {
            FinancialTransaction tx = new FinancialTransaction(
                userId, saved.getId(), "DEPOSIT", balance, balance, "Opening Balance"
            );
            tx.setDescription("Initial opening balance credit");
            transactionRepository.save(tx);
        }

        return saved;
    }

    public void updateAccount(int accountId, String accountName, String bankName,
                              BigDecimal minimumBalance, BigDecimal overdraftLimit,
                              BigDecimal interestRate, String status)
            throws ResourceNotFoundException, DatabaseOperationException {

        BankAccount account = getAccountById(accountId);
        if (accountName != null && !accountName.trim().isEmpty()) account.setAccountName(accountName.trim());
        if (bankName != null && !bankName.trim().isEmpty()) account.setBankName(bankName.trim());
        if (minimumBalance != null) account.setMinimumBalance(minimumBalance);
        if (overdraftLimit != null) account.setOverdraftLimit(overdraftLimit);
        if (interestRate != null) account.setInterestRate(interestRate);
        if (status != null) account.setStatus(status);

        accountRepository.update(account);
    }

    public void deleteAccount(int accountId) throws ResourceNotFoundException, DatabaseOperationException, ValidationException {
        BankAccount account = getAccountById(accountId);
        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new ValidationException("Cannot delete account with an active positive balance (" +
                    CurrencyUtil.formatInr(account.getBalance()) + "). Please withdraw or transfer funds first.");
        }
        accountRepository.delete(accountId);
    }

    /**
     * Executes a deposit atomically:
     * Updates account balance and records transaction history in one SQLite transaction.
     */
    public FinancialTransaction deposit(int accountId, BigDecimal amount, String category, String description)
            throws ResourceNotFoundException, InvalidAmountException, DatabaseOperationException {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Deposit amount must be strictly greater than zero.", amount);
        }

        BankAccount account = getAccountById(accountId);
        account.deposit(amount, description); // Encapsulated mutation

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                accountRepository.updateBalance(conn, account.getId(), account.getBalance());

                FinancialTransaction tx = new FinancialTransaction(
                    account.getUserId(), account.getId(), "DEPOSIT",
                    amount, account.getBalance(),
                    category != null ? category : "Deposit"
                );
                tx.setDescription(description != null ? description : "Simulated bank deposit");
                FinancialTransaction savedTx = transactionRepository.save(conn, tx);

                conn.commit();
                savedTx.setAccountName(account.getAccountName());
                savedTx.setAccountNumber(account.getAccountNumber());
                return savedTx;
            } catch (Exception e) {
                conn.rollback();
                throw new DatabaseOperationException("Failed to complete deposit transaction atomically: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database connection failure: " + e.getMessage(), e);
        }
    }

    /**
     * Executes a withdrawal atomically:
     * Applies runtime polymorphic withdrawal validation (SavingsAccount min-balance vs CurrentAccount overdraft),
     * updates balance, and logs transaction history in one atomic SQLite transaction.
     */
    public FinancialTransaction withdraw(int accountId, BigDecimal amount, String category, String description)
            throws ResourceNotFoundException, InsufficientBalanceException, InvalidAmountException, DatabaseOperationException {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be strictly greater than zero.", amount);
        }

        BankAccount account = getAccountById(accountId);
        // Polymorphic call: executes SavingsAccount or CurrentAccount specific policy
        account.withdraw(amount, description);

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                accountRepository.updateBalance(conn, account.getId(), account.getBalance());

                FinancialTransaction tx = new FinancialTransaction(
                    account.getUserId(), account.getId(), "WITHDRAWAL",
                    amount, account.getBalance(),
                    category != null ? category : "Withdrawal"
                );
                tx.setDescription(description != null ? description : "Simulated bank withdrawal");
                FinancialTransaction savedTx = transactionRepository.save(conn, tx);

                conn.commit();
                savedTx.setAccountName(account.getAccountName());
                savedTx.setAccountNumber(account.getAccountNumber());
                return savedTx;
            } catch (Exception e) {
                conn.rollback();
                throw new DatabaseOperationException("Failed to complete withdrawal transaction atomically: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database connection failure: " + e.getMessage(), e);
        }
    }

    /**
     * Executes an inter-account fund transfer atomically in a single SQLite transaction.
     */
    public void transfer(int fromAccountId, int toAccountId, BigDecimal amount, String description)
            throws ResourceNotFoundException, InsufficientBalanceException, InvalidAmountException,
                   ValidationException, DatabaseOperationException {

        if (fromAccountId == toAccountId) {
            throw new ValidationException("Source and destination accounts cannot be identical.");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Transfer amount must be strictly positive.", amount);
        }

        BankAccount source = getAccountById(fromAccountId);
        BankAccount destination = getAccountById(toAccountId);

        // Polymorphic check on source account
        source.withdraw(amount, "Transfer to " + destination.getAccountNumber());
        destination.deposit(amount, "Transfer from " + source.getAccountNumber());

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                accountRepository.updateBalance(conn, source.getId(), source.getBalance());
                accountRepository.updateBalance(conn, destination.getId(), destination.getBalance());

                FinancialTransaction txDebit = new FinancialTransaction(
                    source.getUserId(), source.getId(), "TRANSFER_OUT",
                    amount, source.getBalance(), "Transfer"
                );
                txDebit.setDescription("Transfer to " + destination.getAccountName() + " (" + destination.getAccountNumber() + ")" +
                        (description != null ? " - " + description : ""));
                transactionRepository.save(conn, txDebit);

                FinancialTransaction txCredit = new FinancialTransaction(
                    destination.getUserId(), destination.getId(), "TRANSFER_IN",
                    amount, destination.getBalance(), "Transfer"
                );
                txCredit.setDescription("Transfer from " + source.getAccountName() + " (" + source.getAccountNumber() + ")" +
                        (description != null ? " - " + description : ""));
                transactionRepository.save(conn, txCredit);

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw new DatabaseOperationException("Fund transfer transaction rolled back: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database failure during transfer: " + e.getMessage(), e);
        }
    }

    public List<FinancialTransaction> getAccountTransactions(int accountId) throws DatabaseOperationException {
        return transactionRepository.findByAccountId(accountId);
    }

    public List<FinancialTransaction> getAllTransactions(int userId, int limit) throws DatabaseOperationException {
        return transactionRepository.findAllByUserId(userId, limit);
    }
}

