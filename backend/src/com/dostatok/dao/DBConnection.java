package com.dostatok.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DBConnection {
    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:../database/dostatok.db");
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            return connection;
        } catch (SQLException exception) {
            connection.close();
            throw exception;
        }
    }
}