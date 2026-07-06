package com.example.paisapilot.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.paisapilot.model.Resource;
import com.example.paisapilot.model.SavingsGoal;
import com.example.paisapilot.model.ValidationResult;
import com.example.paisapilot.repository.SavingsRepository;
import com.google.firebase.Timestamp;

import java.util.List;

public class SavingsViewModel extends AndroidViewModel {

    private final SavingsRepository repository;
    private final MediatorLiveData<Resource<List<SavingsGoal>>> goalsState = new MediatorLiveData<>();
    private final MutableLiveData<Resource<Boolean>> goalActionState = new MutableLiveData<>();

    public SavingsViewModel(@NonNull Application application) {
        super(application);
        this.repository = new SavingsRepository(application);
        
        goalsState.addSource(repository.getAllGoals(), goals -> {
            goalsState.setValue(Resource.success(goals));
        });
    }

    public LiveData<Resource<List<SavingsGoal>>> getGoalsState() {
        return goalsState;
    }

    public LiveData<Resource<Boolean>> getGoalActionState() {
        return goalActionState;
    }

    public ValidationResult validateGoalInput(String title, String targetAmount, String savedAmount, Timestamp targetDate) {
        if (title == null || title.trim().isEmpty()) return ValidationResult.error("Goal title is required");
        
        double target;
        try {
            target = Double.parseDouble(targetAmount);
            if (target <= 0) return ValidationResult.error("Target amount must be greater than 0");
        } catch (Exception e) {
            return ValidationResult.error("Invalid target amount");
        }

        try {
            double saved = Double.parseDouble(savedAmount);
            if (saved < 0) return ValidationResult.error("Saved amount cannot be negative");
        } catch (Exception e) {
            return ValidationResult.error("Invalid saved amount");
        }

        if (targetDate == null) return ValidationResult.error("Target date is required");

        return ValidationResult.success();
    }

    public void addGoal(String title, String targetAmount, String savedAmount, Timestamp targetDate) {
        ValidationResult validation = validateGoalInput(title, targetAmount, savedAmount, targetDate);
        if (!validation.isSuccess()) {
            goalActionState.setValue(Resource.error(validation.getErrorMessage()));
            return;
        }

        double target = Double.parseDouble(targetAmount);
        double saved = Double.parseDouble(savedAmount);

        SavingsGoal goal = new SavingsGoal(null, null, title, target, saved, targetDate, null, false);
        
        goalActionState.setValue(Resource.loading());
        repository.addGoal(goal, new SavingsRepository.SavingsCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
                goalActionState.postValue(Resource.success(true));
            }

            @Override
            public void onError(@NonNull String message) {
                goalActionState.postValue(Resource.error(message));
            }
        });
    }

    public void loadGoals() {
        // Automatically observed
    }

    public void addSavings(String goalId, String amountStr) {
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                goalActionState.setValue(Resource.error("Amount must be greater than 0"));
                return;
            }
        } catch (Exception e) {
            goalActionState.setValue(Resource.error("Invalid amount"));
            return;
        }

        goalActionState.setValue(Resource.loading());
        repository.addSavings(goalId, amount, new SavingsRepository.SavingsCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
                goalActionState.postValue(Resource.success(true));
            }

            @Override
            public void onError(@NonNull String message) {
                goalActionState.postValue(Resource.error(message));
            }
        });
    }

    public void deleteGoal(String goalId) {
        goalActionState.setValue(Resource.loading());
        repository.deleteGoal(goalId, new SavingsRepository.SavingsCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
                goalActionState.postValue(Resource.success(true));
            }

            @Override
            public void onError(@NonNull String message) {
                goalActionState.postValue(Resource.error(message));
            }
        });
    }
}
