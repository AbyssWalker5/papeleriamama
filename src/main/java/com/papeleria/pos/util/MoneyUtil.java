package com.papeleria.pos.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utility class for monetary conversions and formatting.
 */
public final class MoneyUtil {
    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final Locale MEXICO_LOCALE = new Locale("es", "MX");

    private MoneyUtil() {}

    public static int toCents(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        return amount.setScale(SCALE, ROUNDING_MODE).multiply(new BigDecimal("100")).intValueExact();
    }

    public static BigDecimal fromCents(int cents) {
        return new BigDecimal(cents).divide(new BigDecimal("100"), SCALE, ROUNDING_MODE);
    }

    public static BigDecimal round(BigDecimal amount) {
        if (amount == null) return null;
        return amount.setScale(SCALE, ROUNDING_MODE);
    }

    public static String format(BigDecimal amount) {
        if (amount == null) return "";
        NumberFormat format = NumberFormat.getCurrencyInstance(MEXICO_LOCALE);
        return format.format(amount);
    }

    public static BigDecimal multiply(BigDecimal price, int quantity) {
        if (price == null) return null;
        return round(price.multiply(new BigDecimal(quantity)));
    }

    public static boolean isPositive(BigDecimal amount) {
        if (amount == null) return false;
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public static boolean isNonNegative(BigDecimal amount) {
        if (amount == null) return false;
        return amount.compareTo(BigDecimal.ZERO) >= 0;
    }
}
