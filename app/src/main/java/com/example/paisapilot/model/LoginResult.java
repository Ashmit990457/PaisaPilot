package com.example.paisapilot.model;

/**
 * LoginResult
 *
 * Represents the outcome of an authentication attempt.
 * Fields are private and exposed via getters.
 */
public class LoginResult {
    private final boolean success;
    private final String message;

    public LoginResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public static LoginResult success() {
        return new LoginResult(true, "Login successful");
    }

    public static LoginResult failure(String message) {
        return new LoginResult(false, message);
    }
}
