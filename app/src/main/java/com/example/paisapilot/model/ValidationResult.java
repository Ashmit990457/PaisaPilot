package com.example.paisapilot.model;

import androidx.annotation.Nullable;

/**
 * ValidationResult
 *
 * Encapsulates the outcome of input validation.
 * Fields are private and immutable; access via getters.
 */
public class ValidationResult {
    private final boolean success;
    @Nullable
    private final String errorMessage;

    public ValidationResult(boolean success, @Nullable String errorMessage) {
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    @Nullable
    public String getErrorMessage() {
        return errorMessage;
    }

    public static ValidationResult success() {
        return new ValidationResult(true, null);
    }

    public static ValidationResult error(String message) {
        return new ValidationResult(false, message);
    }
}
