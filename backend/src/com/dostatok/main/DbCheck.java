package com.dostatok.main;

import com.dostatok.database.DatabaseConfig;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public final class DbCheck {
    private DbCheck() {
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Calling DatabaseConfig.initializeDatabase()...");
        DatabaseConfig.initializeDatabase();

        try (Connection connection = DatabaseConfig.getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet rs = statement.executeQuery("SELECT name FROM sqlite_master WHERE type='table'")) {
                System.out.println("Tables in database:");
                while (rs.next()) {
                    System.out.println(" - " + rs.getString(1));
                }
            }
        }
    }
}