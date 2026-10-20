package com.dostatok.exception;

import java.math.BigDecimal;

/**
 * Thrown when a financial amount is zero, negative, null, or has invalid scale/precision.
 */
public class InvalidAmountException extends DostatokException {
    private final BigDecimal invalidAmount;

    public InvalidAmountException(String message) {
        super(message, 400);
        this.invalidAmount = null;
    }

    public InvalidAmountException(String message, BigDecimal invalidAmount) {
        super(message, 400);
        this.invalidAmount = invalidAmount;
    }

    public BigDecimal getInvalidAmount() {
        return invalidAmount;
    }
}

