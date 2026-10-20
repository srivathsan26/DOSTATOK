package com.dostatok.main;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/** Clears a database's financial records while preserving user profiles. */
public final class ResetFinancialData {
    private static final DateTimeFormatter BACKUP_TIME = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private ResetFinancialData() { }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: ResetFinancialData <database-path>");
            System.exit(2);
        }

        Path database = Path.of(args[0]).toAbsolutePath().normalize();
        if (!Files.isRegularFile(database)) {
            System.err.println("Database file was not found: " + database);
            System.exit(2);
        }

        Path backup = database.resolveSibling(database.getFileName() + ".backup-"
                + LocalDateTime.now().format(BACKUP_TIME) + ".db");
        try {
            Files.copy(database, backup, StandardCopyOption.COPY_ATTRIBUTES);
            try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + database)) {
                try (Statement statement = connection.createStatement()) {
                    statement.execute("PRAGMA foreign_keys = ON");
                }
                connection.setAutoCommit(false);
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate("DELETE FROM loan_repayments");
                    statement.executeUpdate("DELETE FROM transactions");
                    statement.executeUpdate("DELETE FROM income");
                    statement.executeUpdate("DELETE FROM expenses");
                    statement.executeUpdate("DELETE FROM bank_accounts");
                    statement.executeUpdate("DELETE FROM investments");
                    statement.executeUpdate("DELETE FROM assets");
                    statement.executeUpdate("DELETE FROM loans");
                    statement.executeUpdate("DELETE FROM financial_goals");
                    connection.commit();
                } catch (Exception e) {
                    connection.rollback();
                    throw e;
                }
            }
            System.out.println("Financial data cleared. User profiles were kept.");
            System.out.println("Backup: " + backup);
        } catch (Exception e) {
            System.err.println("Reset failed: " + e.getMessage());
            System.err.println("If the reset began, the backup is at: " + backup);
            System.exit(1);
        }
    }
}
