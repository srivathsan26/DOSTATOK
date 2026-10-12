package com.dostatok.main;

import com.dostatok.dao.DBConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public final class DbCheck {
    private DbCheck() {
    }

    public static void main(String[] args) throws Exception {
        try (Connection connection = DBConnection.getConnection();
             Statement statement = connection.createStatement()) {
            try (ResultSet versionResult = statement.executeQuery("SELECT sqlite_version()")) {
                versionResult.next();
                System.out.println("SQLite version: " + versionResult.getString(1));
            }
            try (ResultSet foreignKeysResult = statement.executeQuery("PRAGMA foreign_keys")) {
                foreignKeysResult.next();
                System.out.println("foreign_keys = " + foreignKeysResult.getInt(1));
            }
        }
    }
}