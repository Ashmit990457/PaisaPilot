package com.example.paisapilot.utils;

/**
 * AppConstants holds all application-level constants.
 *
 * This class serves as a single source of truth for:
 * - API endpoints and timeouts
 * - Cache duration and sizes
 * - Default settings and preferences
 * - Error codes and messages
 * - Feature flags and build configuration constants
 *
 * Best practices:
 * - Use meaningful constant names
 * - Group related constants together
 * - Add comments explaining why each constant exists
 * - Avoid using magic numbers or strings throughout the codebase
 */
public class AppConstants {

    // ===== Application Metadata =====
    public static final String APP_NAME = "PaisaPilot";
    public static final String APP_VERSION = "1.0";

    // ===== API Configuration =====
    public static final int API_TIMEOUT_SECONDS = 30;
    public static final int CONNECTION_TIMEOUT_SECONDS = 10;

    // ===== Cache Configuration =====
    public static final int CACHE_DURATION_HOURS = 24;
    public static final int MAX_CACHE_SIZE_MB = 50;

    // ===== Database Configuration =====
    public static final String DATABASE_NAME = "paisapilot.db";
    public static final int DATABASE_VERSION = 1;

    // ===== SharedPreferences Keys =====
    public static final String PREF_FILE_NAME = "paisapilot_prefs";
    public static final String PREF_USER_ID = "user_id";
    public static final String PREF_IS_FIRST_LAUNCH = "is_first_launch";

    // ===== Error Codes =====
    public static final int ERROR_NETWORK = 1000;
    public static final int ERROR_INVALID_INPUT = 1001;
    public static final int ERROR_UNKNOWN = 9999;

    // ===== Feature Flags =====
    public static final boolean ENABLE_ANALYTICS = true;
    public static final boolean ENABLE_CRASH_REPORTING = true;

    // ===== UI Configuration =====
    public static final int DEFAULT_ANIMATION_DURATION_MS = 300;
    public static final int FRAGMENT_TRANSITION_DURATION_MS = 500;

    // ===== Constraints and Limits =====
    public static final int MAX_TRANSACTION_DESCRIPTION_LENGTH = 500;
    public static final int MAX_CATEGORY_NAME_LENGTH = 50;
    public static final int MIN_PASSWORD_LENGTH = 6;

    // Prevent instantiation
    private AppConstants() {
        throw new AssertionError("No instances of AppConstants");
    }
}

