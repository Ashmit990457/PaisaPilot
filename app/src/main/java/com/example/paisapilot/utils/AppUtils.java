package com.example.paisapilot.utils;

import android.content.Context;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * AppUtils provides common utility methods used throughout the application.
 *
 * This class contains static helper methods for:
 * - String and data formatting
 * - Date/time utilities
 * - Logging with consistent tags
 * - Context-aware operations
 *
 * Best practices:
 * - Keep methods focused and reusable
 * - Use consistent naming conventions
 * - Add clear documentation for each method
 * - Avoid mixing unrelated utilities (consider separation)
 */
public class AppUtils {

    private static final String LOG_TAG = "PaisaPilot";

    // Prevent instantiation
    private AppUtils() {
        throw new AssertionError("No instances of AppUtils");
    }

    // ===== Logging Utilities =====

    /**
     * Log an informational message.
     */
    public static void logInfo(String message) {
        Log.i(LOG_TAG, message);
    }

    /**
     * Log a debug message.
     */
    public static void logDebug(String message) {
        Log.d(LOG_TAG, message);
    }

    /**
     * Log a warning message.
     */
    public static void logWarning(String message) {
        Log.w(LOG_TAG, message);
    }

    /**
     * Log an error message with exception.
     */
    public static void logError(String message, Throwable throwable) {
        Log.e(LOG_TAG, message, throwable);
    }

    // ===== String Utilities =====

    /**
     * Check if a string is null or empty.
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Check if a string is not null and not empty.
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * Get a safe string representation of an object.
     */
    public static String safeString(Object obj) {
        return obj != null ? obj.toString() : "";
    }

    // ===== Date/Time Utilities =====

    /**
     * Format a timestamp as a readable date string.
     */
    public static String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    /**
     * Format a timestamp as a readable date and time string.
     */
    public static String formatDateTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    /**
     * Get current timestamp in milliseconds.
     */
    public static long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    // ===== Currency/Money Utilities =====

    /**
     * Format an amount as currency.
     */
    public static String formatCurrency(double amount) {
        return String.format(Locale.getDefault(), "%.2f", amount);
    }

    /**
     * Format an amount as currency with symbol.
     */
    public static String formatCurrencyWithSymbol(double amount, String symbol) {
        return symbol + " " + formatCurrency(amount);
    }

    // ===== Validation Utilities =====

    /**
     * Validate if a string is a valid email format.
     * Note: This is a simple check; use more robust validation for production.
     */
    public static boolean isValidEmail(String email) {
        if (isEmpty(email)) {
            return false;
        }
        return email.contains("@") && email.contains(".");
    }

    /**
     * Check if a number is within a range (inclusive).
     */
    public static boolean isInRange(double value, double min, double max) {
        return value >= min && value <= max;
    }
}

