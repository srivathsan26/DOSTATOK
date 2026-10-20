package com.dostatok.dao;

import com.dostatok.database.DatabaseConfig;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Provides database connections to the DAO layer.
 * Delegates to centralized DatabaseConfig for predictable path resolution.
 */
public final class DBConnection {
    private DBConnection() {
        // Prevent instantiation
    }

    public static Connection getConnection() throws SQLException {
        return DatabaseConfig.getConnection();
    }
}