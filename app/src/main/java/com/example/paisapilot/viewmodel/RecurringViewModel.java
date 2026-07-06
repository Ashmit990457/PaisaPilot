package com.example.paisapilot.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.paisapilot.model.RecurringExpense;
import com.example.paisapilot.model.Resource;
import com.example.paisapilot.model.ValidationResult;
import com.example.paisapilot.repository.RecurringRepository;
import com.google.firebase.Timestamp;

import java.util.List;

public class RecurringViewModel extends AndroidViewModel {

    private final RecurringRepository repository;
    private final MediatorLiveData<Resource<List<RecurringExpense>>> recurringListState = new MediatorLiveData<>();
    private final MutableLiveData<Resource<Boolean>> recurringActionState = new MutableLiveData<>();

    public RecurringViewModel(@NonNull Application application) {
        super(application);
        this.repository = new RecurringRepository(application);
        
        recurringListState.addSource(repository.getAllRecurring(), bills -> {
            recurringListState.setValue(Resource.success(bills));
        });
    }

    public LiveData<Resource<List<RecurringExpense>>> getRecurringListState() {
        return recurringListState;
    }

    public LiveData<Resource<Boolean>> getRecurringActionState() {
        return recurringActionState;
    }

    public ValidationResult validateRecurringInput(String title, String amountStr, RecurringExpense.Frequency frequency, Timestamp nextDueDate) {
        if (title == null || title.trim().isEmpty()) return ValidationResult.error("Title is required");
        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) return ValidationResult.error("Amount must be greater than 0");
        } catch (Exception e) {
            return ValidationResult.error("Invalid amount");
        }
        if (frequency == null) return ValidationResult.error("Frequency is required");
        if (nextDueDate == null) return ValidationResult.error("Next due date is required");
        return ValidationResult.success();
    }

    public void createRecurring(String title, String category, String amountStr, RecurringExpense.Frequency frequency, Timestamp nextDueDate, boolean reminder, boolean autoAdd) {
        ValidationResult validation = validateRecurringInput(title, amountStr, frequency, nextDueDate);
        if (!validation.isSuccess()) {
            recurringActionState.setValue(Resource.error(validation.getErrorMessage()));
            return;
        }

        double amount = Double.parseDouble(amountStr);
        RecurringExpense recurring = new RecurringExpense(null, null, title, category, amount, frequency, nextDueDate, reminder, autoAdd);

        recurringActionState.setValue(Resource.loading());
        repository.createRecurring(recurring, new RecurringRepository.RecurringCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
                recurringActionState.postValue(Resource.success(true));
            }

            @Override
            public void onError(@NonNull String message) {
                recurringActionState.postValue(Resource.error(message));
            }
        });
    }

    public void loadRecurringExpenses() {
        // Automatically observed
    }

    public void deleteRecurring(String id) {
        recurringActionState.setValue(Resource.loading());
        repository.deleteRecurring(id, new RecurringRepository.RecurringCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
                recurringActionState.postValue(Resource.success(true));
            }

            @Override
            public void onError(@NonNull String message) {
                recurringActionState.postValue(Resource.error(message));
            }
        });
    }
}
