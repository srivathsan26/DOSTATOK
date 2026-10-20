package com.dostatok.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Currency and financial number utilities adhering to Indian financial conventions.
 */
public final class CurrencyUtil {
    private static final Locale INDIA_LOCALE = Locale.of("en", "IN");

    private CurrencyUtil() {
        // Prevent instantiation of utility class
    }

    public static BigDecimal round(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static String formatInr(BigDecimal amount) {
        if (amount == null) {
            return "₹0.00";
        }
        NumberFormat formatter = NumberFormat.getCurrencyInstance(INDIA_LOCALE);
        return formatter.format(round(amount));
    }

    public static BigDecimal parseAmount(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return round((BigDecimal) value);
        }
        if (value instanceof Number) {
            return round(BigDecimal.valueOf(((Number) value).doubleValue()));
        }
        String str = value.toString().trim().replace("₹", "").replace(",", "");
        if (str.isEmpty()) {
            return null;
        }
        return round(new BigDecimal(str));
    }
}

