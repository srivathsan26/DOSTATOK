package com.dostatok.exception;

/**
 * Thrown when client input fails business or structural validation.
 */
public class ValidationException extends DostatokException {
    public ValidationException(String message) {
        super(message, 422);
    }
}

