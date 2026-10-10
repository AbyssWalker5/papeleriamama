package com.papeleria.pos.print;

/**
 * Immutable result of a print operation.
 */
public final class PrintResult {
    private final boolean success;
    private final String errorMessage;

    private PrintResult(boolean success, String errorMessage) {
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public static PrintResult success() {
        return new PrintResult(true, null);
    }

    public static PrintResult failure(String message) {
        return new PrintResult(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
