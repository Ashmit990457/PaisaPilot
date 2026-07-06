package com.example.paisapilot.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import android.util.Patterns;

import com.example.paisapilot.repository.AuthRepository;
import com.example.paisapilot.model.ValidationResult;
import com.example.paisapilot.model.LoginResult;
import com.example.paisapilot.model.Resource;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * LoginViewModel
 *
 * Responsibility: ViewModel for the login screen. Orchestrates validation and
 * authentication calls and exposes a single LiveData<Resource<LoginResult>> that
 * represents loading, success and error states for the UI to observe.
 */
public class LoginViewModel extends AndroidViewModel {
    private final AuthRepository authRepository;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        this.authRepository = new AuthRepository(application);
    }

    private final MutableLiveData<Resource<LoginResult>> authState = new MutableLiveData<>();

    /**
     * Exposed LiveData for the UI to observe. Emits Resource.loading(), Resource.success(data)
     * or Resource.error(message) depending on the flow.
     */
    public LiveData<Resource<LoginResult>> getAuthState() { return authState; }

    /**
     * Initiates login flow: validates input and, if valid, delegates to AuthRepository.
     * The result is posted to authState LiveData. Emits loading then success/error.
     */
    public void login(@NonNull String email, @NonNull String password) {
        ValidationResult validation = validateInput(email, password);
        if (!validation.isSuccess()) {
            // Immediately report validation failure (no loading state needed)
            authState.setValue(Resource.error(validation.getErrorMessage() != null ? validation.getErrorMessage() : "Invalid input"));
            return;
        }

        // Emit loading first
        authState.setValue(Resource.loading());

        authRepository.login(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                authState.postValue(Resource.success(LoginResult.success()));
            }

            @Override
            public void onFailure(@NonNull String errorMessage) {
                authState.postValue(Resource.error(errorMessage != null ? errorMessage : "Authentication failed"));
            }
        });
    }

    /**
     * Validates the provided email and password according to simple rules:
     * - Email must be non-empty and match Patterns.EMAIL_ADDRESS
     * - Password must be non-empty and at least 6 characters
     *
     * Returns a ValidationResult describing success or the first validation error.
     */
    public ValidationResult validateInput(@NonNull String email, @NonNull String password) {
        if (email == null || email.trim().isEmpty()) {
            return ValidationResult.error("Email cannot be empty");
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return ValidationResult.error("Please enter a valid email address");
        }
        if (password == null || password.isEmpty()) {
            return ValidationResult.error("Password cannot be empty");
        }
        if (password.length() < 6) {
            return ValidationResult.error("Password must be at least 6 characters");
        }
        return ValidationResult.success();
    }
}
