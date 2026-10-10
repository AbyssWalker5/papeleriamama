package com.papeleria.pos.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for date conversions.
 */
public final class DateUtil {
    private static final DateTimeFormatter DB_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private DateUtil() {}

    public static String toDbString(LocalDateTime dt) {
        if (dt == null) return null;
        return dt.format(DB_FORMAT);
    }

    public static LocalDateTime fromDbString(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        String clean = s.trim().replace(" ", "T");
        try {
            return LocalDateTime.parse(clean);
        } catch (Exception e) {
            try {
                if (clean.length() >= 19) {
                    return LocalDateTime.parse(clean.substring(0, 19));
                }
                return java.time.LocalDate.parse(clean).atStartOfDay();
            } catch (Exception ex) {
                return LocalDateTime.now();
            }
        }
    }

    public static String formatDisplay(LocalDateTime dt) {
        if (dt == null) return "";
        return dt.format(DISPLAY_FORMAT);
    }

    public static String formatDate(LocalDateTime dt) {
        if (dt == null) return "";
        return dt.format(DATE_FORMAT);
    }

    public static String formatTime(LocalDateTime dt) {
        if (dt == null) return "";
        return dt.format(TIME_FORMAT);
    }

    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    public static LocalDateTime startOfDay(LocalDateTime dt) {
        if (dt == null) return null;
        return dt.toLocalDate().atStartOfDay();
    }

    public static LocalDateTime endOfDay(LocalDateTime dt) {
        if (dt == null) return null;
        return dt.toLocalDate().atTime(23, 59, 59);
    }
}
