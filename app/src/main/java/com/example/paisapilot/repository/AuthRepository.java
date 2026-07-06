package com.example.paisapilot.repository;

import android.app.Application;
import androidx.annotation.NonNull;

// Prepare for Firebase Authentication (no Firebase logic implemented here)
import android.util.Log;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

/**
 * AuthRepository
 *
 * Responsibility: Acts as the data layer for authentication operations. It is the
 * single place to interact with authentication providers (e.g., FirebaseAuth).
 * Methods are placeholders prepared for future implementation and currently do
 * not perform network or authentication actions.
 */
public class AuthRepository {
    private final Application application;
    // FirebaseAuth instance prepared for use.
    private final FirebaseAuth firebaseAuth;

    public AuthRepository(@NonNull Application application) {
        this.application = application;
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    /**
     * Callback interface to return login results asynchronously.
     */
    public interface AuthCallback {
        void onSuccess();
        void onFailure(@NonNull String errorMessage);
    }

    /**
     * Sign in with email and password using Firebase Authentication.
     * Results are delivered via the provided callback on the main thread.
     */
    public void login(@NonNull String email, @NonNull String password, @NonNull final AuthCallback callback) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
                        // Debug logging for successful login
                        try {
                            Log.d("LOGIN_AUTH", "Login successful");
                            if (firebaseAuth.getCurrentUser() != null) {
                                Log.d("LOGIN_AUTH", "UID: " + firebaseAuth.getCurrentUser().getUid());
                            }
                        } catch (Exception ignored) {}
                        callback.onSuccess();
                    } else {
                        // Extract a useful error message if possible and map common Firebase exceptions
                        String message = "Authentication failed";
                        Exception ex = task.getException();
                        if (ex != null) {
                            if (ex instanceof FirebaseAuthInvalidUserException) {
                                message = "Account does not exist";
                            } else if (ex instanceof FirebaseAuthInvalidCredentialsException) {
                                message = "Invalid email or password";
                            } else if (ex instanceof FirebaseNetworkException) {
                                message = "Network error. Check your connection.";
                            } else if (ex.getMessage() != null) {
                                message = ex.getMessage();
                            }
                        }
                        callback.onFailure(message);
                    }
                });
    }

    /**
     * Registers a new user with email and password, sets the display name and
     * reports the result through the provided callback.
     */
    public void register(@NonNull String name, @NonNull String email, @NonNull String password, @NonNull final AuthCallback callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
                        // Update display name
                        com.google.firebase.auth.FirebaseUser user = task.getResult().getUser();
                        com.google.firebase.auth.UserProfileChangeRequest request = new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build();
                        user.updateProfile(request).addOnCompleteListener(updateTask -> {
                            if (updateTask.isSuccessful()) {
                                callback.onSuccess();
                            } else {
                                String msg = "Registration succeeded but failed to set display name";
                                if (updateTask.getException() != null && updateTask.getException().getMessage() != null) {
                                    msg = updateTask.getException().getMessage();
                                }
                                callback.onFailure(msg);
                            }
                        });
                    } else {
                        Exception ex = task.getException();
                        String message = "Registration failed";
                        if (ex != null) {
                            if (ex instanceof com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                                message = "Email is already registered";
                            } else if (ex instanceof com.google.firebase.auth.FirebaseAuthWeakPasswordException) {
                                message = "Password is too weak";
                            } else if (ex instanceof com.google.firebase.FirebaseNetworkException) {
                                message = "Network error. Check your connection.";
                            } else if (ex.getMessage() != null) {
                                message = ex.getMessage();
                            }
                        }
                        callback.onFailure(message);
                    }
                });
    }

    /**
     * Sign out the current user.
     */
    public void logout() {
        firebaseAuth.signOut();
    }
}
