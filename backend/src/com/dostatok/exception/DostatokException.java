package com.dostatok.exception;

/**
 * Base custom exception for DOSTATOK application.
 * All domain-specific exceptions inherit from this class.
 */
public class DostatokException extends Exception {
    private final int statusCode;

    public DostatokException(String message) {
        super(message);
        this.statusCode = 400;
    }

    public DostatokException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public DostatokException(String message, Throwable cause, int statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}

