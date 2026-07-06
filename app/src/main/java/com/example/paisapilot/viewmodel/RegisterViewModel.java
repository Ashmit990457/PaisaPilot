package com.example.paisapilot.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.util.Patterns;

import com.example.paisapilot.model.Resource;
import com.example.paisapilot.model.LoginResult;
import com.example.paisapilot.model.ValidationResult;
import com.example.paisapilot.repository.AuthRepository;

/**
 * RegisterViewModel
 *
 * Validates registration input and coordinates user creation via AuthRepository.
 * Exposes LiveData<Resource<LoginResult>> to represent loading, success and error states.
 */
public class RegisterViewModel extends AndroidViewModel {
    private final AuthRepository authRepository;
    private final MutableLiveData<Resource<LoginResult>> authState = new MutableLiveData<>();

    public RegisterViewModel(@NonNull Application application) {
        super(application);
        this.authRepository = new AuthRepository(application);
    }

    public LiveData<Resource<LoginResult>> getAuthState() { return authState; }

    /**
     * Validates registration inputs. Returns ValidationResult with an error message
     * describing the first validation failure.
     */
    public ValidationResult validateInputs(@NonNull String name, @NonNull String email, @NonNull String password, @NonNull String confirmPassword) {
        if (name.trim().isEmpty()) return ValidationResult.error("Name cannot be empty");
        if (email.trim().isEmpty()) return ValidationResult.error("Email cannot be empty");
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return ValidationResult.error("Please enter a valid email address");
        if (password == null || password.length() < 6) return ValidationResult.error("Password must be at least 6 characters");
        if (!password.equals(confirmPassword)) return ValidationResult.error("Passwords do not match");
        return ValidationResult.success();
    }

    /**
     * Initiates registration flow. Emits loading and then success or error based on repository callback.
     */
    public void register(@NonNull String name, @NonNull String email, @NonNull String password, @NonNull String confirmPassword) {
        ValidationResult validation = validateInputs(name, email, password, confirmPassword);
        if (!validation.isSuccess()) {
            authState.setValue(Resource.error(validation.getErrorMessage() != null ? validation.getErrorMessage() : "Invalid input"));
            return;
        }

        authState.setValue(Resource.loading());
        authRepository.register(name, email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess() {
                authState.postValue(Resource.success(LoginResult.success()));
            }

            @Override
            public void onFailure(@NonNull String message) {
                authState.postValue(Resource.error(message));
            }
        });
    }
}
