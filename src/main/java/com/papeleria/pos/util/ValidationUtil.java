package com.papeleria.pos.util;

import java.math.BigDecimal;

/**
 * Input validation utility.
 */
public final class ValidationUtil {
    
    private ValidationUtil() {}

    public static boolean isNullOrEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static boolean isValidBarcode(String barcode) {
        if (isNullOrEmpty(barcode)) return false;
        String trimmed = barcode.trim();
        return trimmed.length() >= 4 && trimmed.length() <= 50 && trimmed.matches("^[a-zA-Z0-9]+$");
    }

    public static boolean isPositiveInteger(String s) {
        if (isNullOrEmpty(s)) return false;
        try {
            int val = Integer.parseInt(s.trim());
            return val > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isNonNegativeInteger(String s) {
        if (isNullOrEmpty(s)) return false;
        try {
            int val = Integer.parseInt(s.trim());
            return val >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidPrice(String s) {
        if (isNullOrEmpty(s)) return false;
        try {
            BigDecimal val = new BigDecimal(s.trim());
            return val.compareTo(BigDecimal.ZERO) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isNonNegativePrice(String s) {
        if (isNullOrEmpty(s)) return false;
        try {
            BigDecimal val = new BigDecimal(s.trim());
            return val.compareTo(BigDecimal.ZERO) >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String sanitize(String s) {
        if (s == null) return null;
        return s.replaceAll("\\p{C}", "").trim();
    }

    public static int parseIntSafe(String s, int defaultVal) {
        if (isNullOrEmpty(s)) return defaultVal;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    public static BigDecimal parseBigDecimalSafe(String s, BigDecimal defaultVal) {
        if (isNullOrEmpty(s)) return defaultVal;
        try {
            return new BigDecimal(s.trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }
}
