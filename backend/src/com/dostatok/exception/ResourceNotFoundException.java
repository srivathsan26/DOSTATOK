package com.dostatok.exception;

/**
 * Thrown when an entity (account, transaction, goal, etc.) is not found by ID.
 */
public class ResourceNotFoundException extends DostatokException {
    public ResourceNotFoundException(String message) {
        super(message, 404);
    }
}

