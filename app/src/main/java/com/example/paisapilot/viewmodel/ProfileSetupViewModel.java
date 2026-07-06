package com.example.paisapilot.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.paisapilot.model.Resource;
import com.example.paisapilot.model.UserProfile;
import com.example.paisapilot.repository.UserProfileRepository;
import com.example.paisapilot.model.ValidationResult;

/**
 * ProfileSetupViewModel
 *
 * Responsibility: Validate profile input and coordinate saving the profile via
 * UserProfileRepository. Exposes LiveData<Resource<UserProfile>> for the UI to
 * observe loading, success and error states.
 */
public class ProfileSetupViewModel extends AndroidViewModel {
    private final UserProfileRepository repository;
    private final MutableLiveData<Resource<UserProfile>> state = new MutableLiveData<>();

    public ProfileSetupViewModel(@NonNull Application application) {
        super(application);
        this.repository = new UserProfileRepository(application);
    }

    public LiveData<Resource<UserProfile>> getState() { return state; }

    /**
     * Validates the inputs for the profile according to the rules:
     * - Name required
     * - Income > 0
     * - Saving goal <= Income
     */
    public ValidationResult validateInputs(@NonNull String name, @NonNull String occupation, @NonNull String city,
                                           double income, double savingGoal, @NonNull String currency, int salaryDate) {
        if (name.trim().isEmpty()) return ValidationResult.error("Full name is required");
        if (income <= 0) return ValidationResult.error("Monthly income must be greater than 0");
        if (savingGoal < 0) return ValidationResult.error("Saving goal cannot be negative");
        if (savingGoal > income) return ValidationResult.error("Saving goal must be less than or equal to income");
        if (salaryDate < 1 || salaryDate > 31) return ValidationResult.error("Salary credit date must be between 1 and 31");
        return ValidationResult.success();
    }

    /**
     * Saves the profile after validation. Emits Resource.loading(), then success or error.
     */
    public void saveProfile(@NonNull UserProfile profile) {
        // Emit loading
        state.setValue(Resource.loading());
        repository.saveUserProfile(profile, new UserProfileRepository.ProfileCallback() {
            @Override
            public void onSuccess() {
                state.postValue(Resource.success(profile));
            }

            @Override
            public void onFailure(@NonNull String message) {
                state.postValue(Resource.error(message));
            }
        });
    }
}
