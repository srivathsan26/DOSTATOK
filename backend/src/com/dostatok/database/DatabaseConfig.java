package com.dostatok.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages SQLite database path resolution, connection provisioning,
 * and versioned schema migration on startup. Normal startup never seeds demo data.
 */
public final class DatabaseConfig {
    private static Path resolvedDbPath;

    private DatabaseConfig() {
        // Utility / configuration class
    }

    /**
     * Resolves the canonical absolute path of the SQLite database file
     * safely regardless of working directory.
     */
    public static synchronized Path getDatabasePath() {
        if (resolvedDbPath != null) {
            return resolvedDbPath;
        }

        // 1. Check system property or env var
        String envPath = System.getProperty("DOSTATOK_DB_PATH");
        if (envPath == null || envPath.isBlank()) {
            envPath = System.getenv("DOSTATOK_DB_PATH");
        }
        if (envPath != null && !envPath.isBlank()) {
            resolvedDbPath = Paths.get(envPath).toAbsolutePath().normalize();
            ensureParentDir(resolvedDbPath);
            return resolvedDbPath;
        }

        // 2. Discover based on current working directory
        Path currentDir = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();

        // Check if we are running in workspace root (DOSTATOK/)
        Path candidate1 = currentDir.resolve("database").resolve("dostatok.db");
        // Check if we are in backend/
        Path candidate2 = currentDir.resolve("..").resolve("database").resolve("dostatok.db").normalize();
        // Check backend/data/
        Path candidate3 = currentDir.resolve("backend").resolve("data").resolve("dostatok.db");
        Path candidate4 = currentDir.resolve("data").resolve("dostatok.db");

        if (Files.exists(candidate1.getParent())) {
            resolvedDbPath = candidate1;
        } else if (Files.exists(candidate2.getParent())) {
            resolvedDbPath = candidate2;
        } else if (Files.exists(candidate3.getParent())) {
            resolvedDbPath = candidate3;
        } else if (Files.exists(candidate4.getParent())) {
            resolvedDbPath = candidate4;
        } else {
            // Default to candidate 1
            resolvedDbPath = candidate1;
        }

        ensureParentDir(resolvedDbPath);
        return resolvedDbPath;
    }

    private static void ensureParentDir(Path path) {
        try {
            if (path.getParent() != null && !Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not create database parent directory: " + e.getMessage());
        }
    }

    public static String getJdbcUrl() {
        return "jdbc:sqlite:" + getDatabasePath().toString();
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(getJdbcUrl());
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    /** Creates the schema if needed and applies repeatable, versioned migrations. */
    public static synchronized void initializeDatabase() {
        Path dbFile = getDatabasePath();
        System.out.println("Connecting to SQLite database at: " + dbFile.toAbsolutePath());

        try (Connection conn = getConnection()) {
            boolean tablesExist;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                         "SELECT count(*) FROM sqlite_master WHERE type='table' AND name='users'")) {
                tablesExist = rs.next() && rs.getInt(1) > 0;
            }

            if (!tablesExist) {
                System.out.println("Initializing database tables from schema...");
                executeSqlScript(conn, findSchemaFile());
            }
            applyMigrations(conn);
            System.out.println("SQLite schema initialized; no demo records were seeded.");
        } catch (Exception e) {
            throw new IllegalStateException("Database initialization failed; server startup was stopped.", e);
        }
    }

    private static void applyMigrations(Connection conn) throws SQLException {
        conn.setAutoCommit(false);
        try {
            int version;
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("PRAGMA user_version")) {
                version = rs.next() ? rs.getInt(1) : 0;
            }
            if (version < 1) {
                if (!columnExists(conn, "users", "password_hash")) {
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute("ALTER TABLE users ADD COLUMN password_hash TEXT");
                    }
                }
                // The former demo profile and all its fictional financial records are retired.
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE username = ? OR email = ?")) {
                    stmt.setString(1, "demo_user");
                    stmt.setString(2, "aditya.sharma@dostatok.in");
                    stmt.executeUpdate();
                }
                try (Statement stmt = conn.createStatement()) { stmt.execute("PRAGMA user_version = 1"); }
                version = 1;
            }
            if (version < 2) {
                try (Statement stmt = conn.createStatement()) {
                    if (!columnExists(conn, "users", "age")) stmt.execute("ALTER TABLE users ADD COLUMN age INTEGER");
                    if (!columnExists(conn, "users", "occupation")) stmt.execute("ALTER TABLE users ADD COLUMN occupation TEXT");
                    if (!columnExists(conn, "users", "address")) stmt.execute("ALTER TABLE users ADD COLUMN address TEXT");
                    if (!columnExists(conn, "users", "annual_income")) stmt.execute("ALTER TABLE users ADD COLUMN annual_income DECIMAL(15,2)");
                    stmt.execute("PRAGMA user_version = 2");
                }
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    private static boolean columnExists(Connection conn, String table, String column) throws SQLException {
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + table + ")")) {
            while (rs.next()) if (column.equalsIgnoreCase(rs.getString("name"))) return true;
            return false;
        }
    }

    private static Path findSchemaFile() {
        Path p1 = getDatabasePath().getParent().resolve("schema.sql");
        if (Files.exists(p1)) return p1;
        Path p2 = Paths.get("database", "schema.sql").toAbsolutePath();
        if (Files.exists(p2)) return p2;
        Path p3 = Paths.get("..", "database", "schema.sql").toAbsolutePath().normalize();
        if (Files.exists(p3)) return p3;
        return p1;
    }

    private static void executeSqlScript(Connection conn, Path scriptPath) throws IOException, SQLException {
        if (!Files.exists(scriptPath)) {
            System.err.println("Warning: SQL script not found at " + scriptPath);
            return;
        }

        String rawContent = Files.readString(scriptPath);
        // Strip block comments /* ... */ and line comments -- ...
        String cleaned = rawContent.replaceAll("/\\*[\\s\\S]*?\\*/", "");
        cleaned = cleaned.replaceAll("(?m)^\\s*--.*$", "");

        String[] statements = cleaned.split(";");
        try (Statement stmt = conn.createStatement()) {
            for (String rawStmt : statements) {
                String sql = rawStmt.trim();
                if (sql.isEmpty()) {
                    continue;
                }
                stmt.execute(sql);
            }
        }
    }
}
