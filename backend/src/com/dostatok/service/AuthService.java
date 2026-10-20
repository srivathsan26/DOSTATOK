package com.dostatok.service;

import com.dostatok.dao.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.regex.Pattern;

/** Registration and credential verification for local application users. */
public final class AuthService {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private final PasswordHasher hasher = new PasswordHasher();
    private final String dummyHash = hasher.hash("constant-time-dummy".toCharArray());

    public record Profile(int id, String name, String email, Integer age, String occupation,
                          String address, BigDecimal annualIncome, boolean profileComplete) {}

    public Profile register(String name, String email, char[] password) throws SQLException {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        String normalizedName = name == null ? "" : name.trim();
        if (normalizedName.isBlank() || normalizedName.length() > 100) throw new IllegalArgumentException("Enter a name of at most 100 characters.");
        if (!EMAIL.matcher(normalizedEmail).matches() || normalizedEmail.length() > 254) throw new IllegalArgumentException("Enter a valid email address.");
        if (password == null || password.length < 12 || password.length > 128) throw new IllegalArgumentException("Password must be 12 to 128 characters.");
        String username = normalizedEmail;
        String hash = hasher.hash(password);
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(
                "INSERT INTO users (username, full_name, email, currency, password_hash) VALUES (?, ?, ?, 'INR', ?)")) {
            p.setString(1, username); p.setString(2, normalizedName); p.setString(3, normalizedEmail); p.setString(4, hash);
            p.executeUpdate();
            int id;
            try (PreparedStatement q = c.prepareStatement("SELECT id FROM users WHERE email = ?")) {
                q.setString(1, normalizedEmail);
                try (ResultSet r = q.executeQuery()) { r.next(); id = r.getInt(1); }
            }
            return readProfile(c, id);
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase(Locale.ROOT).contains("unique")) {
                throw new IllegalArgumentException("An account with this email already exists.");
            }
            throw e;
        }
    }

    public Profile authenticate(String email, char[] password) throws SQLException {
        String normalized = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(
                "SELECT id, full_name, email, password_hash FROM users WHERE email = ?")) {
            p.setString(1, normalized);
            int id;
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) { hasher.verify(password, dummyHash); return null; }
                String hash = r.getString("password_hash");
                if (hash == null || !hasher.verify(password, hash)) return null;
                id = r.getInt("id");
            }
            return readProfile(c, id);
        }
    }

    public Profile getProfile(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection()) { return readProfile(c, id); }
    }

    public Profile updateProfile(int id, int age, String occupation, String address, BigDecimal annualIncome) throws SQLException {
        String cleanOccupation = occupation == null ? "" : occupation.trim();
        String cleanAddress = address == null ? "" : address.trim();
        if (age < 13 || age > 120) throw new IllegalArgumentException("Age must be between 13 and 120.");
        if (cleanOccupation.isBlank() || cleanOccupation.length() > 100) throw new IllegalArgumentException("Enter a job or occupation of at most 100 characters.");
        if (cleanAddress.isBlank() || cleanAddress.length() > 500) throw new IllegalArgumentException("Enter an address of at most 500 characters.");
        if (annualIncome == null || annualIncome.signum() < 0 || annualIncome.scale() > 2 || annualIncome.precision() > 15) {
            throw new IllegalArgumentException("Enter a valid non-negative annual income with up to two decimal places.");
        }
        try (Connection c = DBConnection.getConnection(); PreparedStatement p = c.prepareStatement(
                "UPDATE users SET age = ?, occupation = ?, address = ?, annual_income = ? WHERE id = ?")) {
            p.setInt(1, age); p.setString(2, cleanOccupation); p.setString(3, cleanAddress);
            p.setBigDecimal(4, annualIncome); p.setInt(5, id);
            if (p.executeUpdate() == 0) return null;
            return readProfile(c, id);
        }
    }

    private Profile readProfile(Connection c, int id) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "SELECT id, full_name, email, age, occupation, address, annual_income FROM users WHERE id = ?")) {
            p.setInt(1, id);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) return null;
                int ageValue = r.getInt("age");
                Integer age = r.wasNull() ? null : ageValue;
                BigDecimal income = r.getBigDecimal("annual_income");
                String occupation = r.getString("occupation");
                String address = r.getString("address");
                boolean complete = age != null && occupation != null && !occupation.isBlank()
                        && address != null && !address.isBlank() && income != null;
                return new Profile(r.getInt("id"), r.getString("full_name"), r.getString("email"),
                        age, occupation, address, income, complete);
            }
        }
    }
}
