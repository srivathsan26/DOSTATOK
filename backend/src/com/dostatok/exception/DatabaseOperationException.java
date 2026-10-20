package com.dostatok.exception;

/**
 * Thrown when an underlying JDBC/SQL database operation fails.
 */
public class DatabaseOperationException extends DostatokException {
    public DatabaseOperationException(String message) {
        super(message, 500);
    }

    public DatabaseOperationException(String message, Throwable cause) {
        super(message, cause, 500);
    }
}

